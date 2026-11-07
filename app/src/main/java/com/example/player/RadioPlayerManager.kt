package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.os.PowerManager
import com.example.data.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Random

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

data class PlayerState(
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentStation: RadioStation? = null,
    val errorMessage: String? = null,
    val volume: Float = 0.85f,
    val isVolumeBoosted: Boolean = false,
    val signalStrengthPercent: Int = 95,
    val isStaticNoiseEnabled: Boolean = false,
    val sleepTimerRemainingSeconds: Long = 0,
    val isSleepTimerActive: Boolean = false,
    val audioAmplitudes: List<Float> = List(12) { 0.1f }
)

class RadioPlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var audioTrackStatic: AudioTrack? = null
    private var staticThread: Thread? = null
    @Volatile private var isGeneratingStatic = false

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var loudnessEnhancer: android.media.audiofx.LoudnessEnhancer? = null
    private var sleepCountDownTimer: CountDownTimer? = null
    private var amplitudeJob: Job? = null

    init {
        startAmplitudeSimulator()
    }

    fun playStation(station: RadioStation) {
        scope.launch {
            stopPlaybackInternal()

            _playerState.value = _playerState.value.copy(
                status = PlaybackStatus.BUFFERING,
                currentStation = station,
                errorMessage = null,
                signalStrengthPercent = (88..98).random()
            )

            // Play a brief tuning sweep static sound if enabled
            if (_playerState.value.isStaticNoiseEnabled) {
                playTuningChirp()
            }

            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                    setDataSource(station.streamUrl)

                    setOnPreparedListener { mp ->
                        mp.start()
                        applyLoudnessGain(_playerState.value.volume)
                        _playerState.value = _playerState.value.copy(
                            status = PlaybackStatus.PLAYING,
                            errorMessage = null
                        )
                    }

                    setOnErrorListener { _, what, extra ->
                        _playerState.value = _playerState.value.copy(
                            status = PlaybackStatus.ERROR,
                            errorMessage = "تعذر الاتصال بالبث المباشر ($what/$extra). جاري المحاولة..."
                        )
                        false
                    }

                    setOnCompletionListener {
                        _playerState.value = _playerState.value.copy(status = PlaybackStatus.IDLE)
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                _playerState.value = _playerState.value.copy(
                    status = PlaybackStatus.ERROR,
                    errorMessage = "خطأ في تشغيل الرابط: ${e.localizedMessage ?: "تأكد من اتصالك بالإنترنت"}"
                )
            }
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer
        if (mp != null) {
            if (mp.isPlaying) {
                mp.pause()
                _playerState.value = _playerState.value.copy(status = PlaybackStatus.PAUSED)
            } else {
                mp.start()
                applyLoudnessGain(_playerState.value.volume)
                _playerState.value = _playerState.value.copy(status = PlaybackStatus.PLAYING)
            }
        } else {
            val station = _playerState.value.currentStation
            if (station != null) {
                playStation(station)
            }
        }
    }

    fun stop() {
        stopPlaybackInternal()
        _playerState.value = _playerState.value.copy(status = PlaybackStatus.IDLE)
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 2.0f)
        _playerState.value = _playerState.value.copy(
            volume = clamped,
            isVolumeBoosted = clamped > 1.0f
        )
        applyLoudnessGain(clamped)
    }

    fun toggleMaxVolumeBoost() {
        val current = _playerState.value.volume
        val target = if (current >= 1.95f) 1.0f else 2.0f
        setVolume(target)
    }

    private fun applyLoudnessGain(volume: Float) {
        val mp = mediaPlayer ?: return
        try {
            if (loudnessEnhancer == null) {
                loudnessEnhancer = android.media.audiofx.LoudnessEnhancer(mp.audioSessionId)
            }
            if (volume > 1.0f) {
                mp.setVolume(1.0f, 1.0f)
                val boostGainMb = ((volume - 1.0f) * 1800).toInt()
                loudnessEnhancer?.setTargetGain(boostGainMb)
                loudnessEnhancer?.enabled = true
            } else {
                mp.setVolume(volume, volume)
                loudnessEnhancer?.setTargetGain(0)
                loudnessEnhancer?.enabled = false
            }
        } catch (e: Exception) {
            mp.setVolume(volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f))
        }
    }

    fun toggleStaticNoise() {
        val newSetting = !_playerState.value.isStaticNoiseEnabled
        _playerState.value = _playerState.value.copy(isStaticNoiseEnabled = newSetting)
        if (newSetting && _playerState.value.status != PlaybackStatus.PLAYING) {
            startContinuousStatic()
        } else {
            stopContinuousStatic()
        }
    }

    fun setSleepTimerMinutes(minutes: Int) {
        sleepCountDownTimer?.cancel()
        if (minutes <= 0) {
            _playerState.value = _playerState.value.copy(
                isSleepTimerActive = false,
                sleepTimerRemainingSeconds = 0
            )
            return
        }

        val totalMillis = minutes * 60 * 1000L
        _playerState.value = _playerState.value.copy(
            isSleepTimerActive = true,
            sleepTimerRemainingSeconds = totalMillis / 1000
        )

        sleepCountDownTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _playerState.value = _playerState.value.copy(
                    sleepTimerRemainingSeconds = millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                stop()
                _playerState.value = _playerState.value.copy(
                    isSleepTimerActive = false,
                    sleepTimerRemainingSeconds = 0
                )
            }
        }.start()
    }

    private fun playTuningChirp() {
        scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                val durationMs = 280
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)
                val random = Random()

                for (i in 0 until numSamples) {
                    val noise = (random.nextGaussian() * 0.15 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    buffer[i] = noise
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                delay(durationMs.toLong())
                track.release()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun startContinuousStatic() {
        if (isGeneratingStatic) return
        isGeneratingStatic = true

        staticThread = Thread {
            try {
                val sampleRate = 22050
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioTrackStatic = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrackStatic?.play()
                val buffer = ShortArray(minBufferSize)
                val random = Random()

                while (isGeneratingStatic) {
                    for (i in buffer.indices) {
                        buffer[i] = (random.nextGaussian() * 0.08 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    audioTrackStatic?.write(buffer, 0, buffer.size)
                }

                audioTrackStatic?.stop()
                audioTrackStatic?.release()
                audioTrackStatic = null
            } catch (e: Exception) {
                // Ignore static audio failures
            }
        }.apply { start() }
    }

    private fun stopContinuousStatic() {
        isGeneratingStatic = false
        staticThread = null
    }

    private fun startAmplitudeSimulator() {
        amplitudeJob = scope.launch(Dispatchers.Default) {
            val random = Random()
            var tick = 0
            while (isActive) {
                if (_playerState.value.status == PlaybackStatus.PLAYING) {
                    val base = 0.25f + random.nextFloat() * 0.75f
                    val newAmps = List(12) { i ->
                        (base * (0.3f + random.nextFloat() * 0.7f)).coerceIn(0.08f, 1.0f)
                    }

                    // Fluctuate RF signal strength every 4 ticks (~480ms)
                    val updatedSignal = if (tick % 4 == 0) {
                        val station = _playerState.value.currentStation
                        val bandBase = when (station?.band) {
                            com.example.data.model.RadioBand.SW -> 88 // Shortwave ionospheric fluctuation
                            com.example.data.model.RadioBand.FM -> 96 // FM strong local signal
                            com.example.data.model.RadioBand.MW -> 92 // Mediumwave
                            com.example.data.model.RadioBand.LW -> 90 // Longwave
                            else -> 93
                        }
                        (bandBase + (random.nextInt(7) - 3)).coerceIn(55, 99)
                    } else {
                        _playerState.value.signalStrengthPercent
                    }

                    _playerState.value = _playerState.value.copy(
                        audioAmplitudes = newAmps,
                        signalStrengthPercent = updatedSignal
                    )
                } else if (_playerState.value.status == PlaybackStatus.BUFFERING) {
                    _playerState.value = _playerState.value.copy(
                        audioAmplitudes = List(12) { 0.15f },
                        signalStrengthPercent = (48..65).random()
                    )
                } else {
                    _playerState.value = _playerState.value.copy(
                        audioAmplitudes = List(12) { 0.06f },
                        signalStrengthPercent = 15
                    )
                }
                tick++
                delay(120)
            }
        }
    }

    private fun stopPlaybackInternal() {
        try {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
        } catch (ignored: Exception) {}
        loudnessEnhancer = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (ignored: Exception) {
        }
        mediaPlayer = null
        stopContinuousStatic()
    }

    fun release() {
        stopPlaybackInternal()
        sleepCountDownTimer?.cancel()
        amplitudeJob?.cancel()
    }
}
