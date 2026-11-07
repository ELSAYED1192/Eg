package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RadioBand
import com.example.data.model.RadioStation
import com.example.player.PlaybackStatus
import com.example.player.PlayerState
import com.example.ui.theme.RadioAmberBright
import com.example.ui.theme.RadioAmberGlow
import com.example.ui.theme.RadioAmberSubtle
import com.example.ui.theme.RadioBezel
import com.example.ui.theme.RadioChassis
import com.example.ui.theme.RadioChassisCard
import com.example.ui.theme.RadioCyanBright
import com.example.ui.theme.RadioCyanGlow
import com.example.ui.theme.RadioCyanSubtle
import com.example.ui.theme.RadioEmeraldGreen
import com.example.ui.theme.RadioObsidian
import com.example.ui.theme.RadioRubyRed
import com.example.ui.theme.RadioSignalNeedle
import com.example.ui.theme.RadioTextMuted
import com.example.ui.theme.RadioTextPrimary
import com.example.ui.theme.RadioTextSecondary

@Composable
fun RetroFrequencyDisplay(
    currentFrequency: Double,
    selectedBand: RadioBand,
    currentStation: RadioStation?,
    playerState: PlayerState,
    onFrequencyDrag: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine min/max range for the active band scale
    val (minFreq, maxFreq, unit) = when (selectedBand) {
        RadioBand.LW -> Triple(148.5, 283.5, "kHz")
        RadioBand.MW -> Triple(531.0, 1602.0, "kHz")
        RadioBand.SW -> Triple(5.9, 18.0, "MHz")
        RadioBand.FM, RadioBand.ALL -> Triple(87.5, 108.0, "MHz")
    }

    val normalizedTunerPos = ((currentFrequency - minFreq) / (maxFreq - minFreq)).coerceIn(0.0, 1.0).toFloat()
    val animatedNeedlePos by animateFloatAsState(
        targetValue = normalizedTunerPos,
        animationSpec = tween(durationMillis = 200),
        label = "tunerNeedle"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RadioChassis),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(RadioBezel, RadioObsidian)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Bar: Brand & Indicator LEDs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Retro Brand Logo Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Podcasts,
                        contentDescription = null,
                        tint = RadioAmberBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ATHEER 1970",
                        color = RadioAmberGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // LED Indicators: STEREO, TUNED, DX
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LedIndicator(
                        label = "TUNED",
                        isActive = playerState.status == PlaybackStatus.PLAYING || playerState.status == PlaybackStatus.BUFFERING,
                        activeColor = RadioEmeraldGreen
                    )
                    LedIndicator(
                        label = "STEREO",
                        isActive = playerState.status == PlaybackStatus.PLAYING,
                        activeColor = RadioRubyRed
                    )
                    LedIndicator(
                        label = "ONLINE",
                        isActive = true,
                        activeColor = RadioCyanGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MAIN GLOWING FREQUENCY SCREEN (Glass Panel)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF090D14),
                                Color(0xFF0F1722),
                                Color(0xFF080C13)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(listOf(RadioCyanSubtle, RadioBezel)),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: Active Band & S-Meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Band Selector Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(RadioBand.LW, RadioBand.MW, RadioBand.SW, RadioBand.FM).forEach { band ->
                                val isSelected = selectedBand == band || (selectedBand == RadioBand.ALL && currentStation?.band == band)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) RadioAmberGlow else Color(0xFF151C26))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = band.code,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) RadioObsidian else RadioTextMuted
                                    )
                                }
                            }
                        }

                        // Signal Reception Tower & Bars Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val sigPercent = if (playerState.status == PlaybackStatus.PLAYING) playerState.signalStrengthPercent else if (playerState.status == PlaybackStatus.BUFFERING) 50 else 20
                            BroadcastTowerSignalIcon(
                                strengthPercent = sigPercent,
                                isReceiving = playerState.status == PlaybackStatus.PLAYING,
                                iconSize = 18.dp
                            )
                            CellularTowerBars(
                                strengthPercent = sigPercent,
                                barCount = 5,
                                barSpacing = 2.dp
                            )
                            Text(
                                text = "$sigPercent%",
                                fontSize = 11.sp,
                                color = getSignalColor(sigPercent),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: DIGITAL FREQUENCY READOUT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            // Frequency Number Display
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (unit == "MHz") {
                                        String.format(java.util.Locale.US, "%.3f", currentFrequency)
                                    } else {
                                        String.format(java.util.Locale.US, "%.0f", currentFrequency)
                                    },
                                    color = RadioCyanBright,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = unit,
                                    color = RadioAmberGlow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                if (currentStation?.meterBand != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "[${currentStation.meterBand}]",
                                        color = RadioTextSecondary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 7.dp)
                                    )
                                }
                            }

                            // Current Station Name or Tuning Status
                            val displayName = when {
                                currentStation != null -> "${currentStation.countryFlag} ${currentStation.name}"
                                playerState.status == PlaybackStatus.BUFFERING -> "⏳ جاري ضبط واستقبال التردد..."
                                else -> "تردد حر • قم بالتدوير لضبط المحطة"
                            }
                            Text(
                                text = displayName,
                                color = if (currentStation != null) RadioAmberBright else RadioTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Audio Spectrum Equalizer (Pulsing graphic bars)
                        EqualizerVisualizer(
                            amplitudes = playerState.audioAmplitudes,
                            isPlaying = playerState.status == PlaybackStatus.PLAYING,
                            modifier = Modifier
                                .width(70.dp)
                                .height(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 3: ANALOG TUNING DIAL SCALE (Interactive slider needle)
                    TuningScaleRuler(
                        minFreq = minFreq,
                        maxFreq = maxFreq,
                        unit = unit,
                        needlePosition = animatedNeedlePos,
                        onDragPosition = { fraction ->
                            val newFreq = minFreq + fraction * (maxFreq - minFreq)
                            onFrequencyDrag(newFreq)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // DEDICATED VISUAL SIGNAL STRENGTH INDICATOR (Graduated Bar + Tower Metrics)
            val signalPercent = if (playerState.status == PlaybackStatus.PLAYING) {
                playerState.signalStrengthPercent
            } else if (playerState.status == PlaybackStatus.BUFFERING) {
                55
            } else {
                15
            }
            SignalReceptionPanel(
                strengthPercent = signalPercent,
                isLive = playerState.status == PlaybackStatus.PLAYING,
                frequencyDisplay = currentStation?.frequencyDisplay,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Bar: Programs & Quick Details
            if (currentStation?.popularPrograms?.isNotEmpty() == true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = RadioAmberGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "البرنامج: ${currentStation.popularPrograms.first()}",
                        color = RadioTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LedIndicator(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color(0xFF263243))
                .border(
                    0.5.dp,
                    if (isActive) activeColor.copy(alpha = 0.8f) else Color.Transparent,
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isActive) RadioTextPrimary else RadioTextMuted,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SignalStrengthMeter(
    strengthPercent: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val totalBars = 8
        val barWidth = size.width / totalBars - 2.dp.toPx()
        val activeBars = (strengthPercent / 100f * totalBars).toInt().coerceIn(1, totalBars)

        for (i in 0 until totalBars) {
            val left = i * (barWidth + 2.dp.toPx())
            val barHeight = size.height * ((i + 1f) / totalBars)
            val top = size.height - barHeight

            val color = when {
                i >= activeBars -> Color(0xFF202A38)
                i >= 6 -> RadioRubyRed
                i >= 4 -> RadioAmberBright
                else -> RadioEmeraldGreen
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

@Composable
fun EqualizerVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val count = amplitudes.size
        val barWidth = (size.width / count) - 2.dp.toPx()

        for (i in 0 until count) {
            val amp = if (isPlaying) amplitudes[i] else 0.08f
            val barHeight = (size.height * amp).coerceIn(3.dp.toPx(), size.height)
            val left = i * (barWidth + 2.dp.toPx())
            val top = size.height - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(RadioCyanBright, RadioAmberBright),
                    startY = top,
                    endY = size.height
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

@Composable
fun TuningScaleRuler(
    minFreq: Double,
    maxFreq: Double,
    unit: String,
    needlePosition: Float,
    onDragPosition: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF070B10))
            .border(1.dp, Color(0xFF1B2636), RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    onDragPosition(fraction)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val ticks = 30
            val rulerHeight = size.height
            val tickStep = size.width / ticks

            // Draw background tick marks (like vintage radio glass scale)
            for (i in 0..ticks) {
                val x = i * tickStep
                val isMajor = i % 5 == 0
                val tickHeight = if (isMajor) rulerHeight * 0.45f else rulerHeight * 0.25f

                drawLine(
                    color = if (isMajor) RadioAmberBright.copy(alpha = 0.8f) else Color(0xFF33465E),
                    start = Offset(x, rulerHeight - tickHeight),
                    end = Offset(x, rulerHeight),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // Draw glowing RED tuning needle
            val needleX = size.width * needlePosition.coerceIn(0f, 1f)

            // Needle glow shadow
            drawLine(
                color = RadioSignalNeedle.copy(alpha = 0.35f),
                start = Offset(needleX, 0f),
                end = Offset(needleX, rulerHeight),
                strokeWidth = 6.dp.toPx()
            )

            // Sharp needle line
            drawLine(
                color = RadioSignalNeedle,
                start = Offset(needleX, 0f),
                end = Offset(needleX, rulerHeight),
                strokeWidth = 2.5.dp.toPx()
            )

            // Needle pointer triangle at top
            val path = Path().apply {
                moveTo(needleX - 4.dp.toPx(), 0f)
                lineTo(needleX + 4.dp.toPx(), 0f)
                lineTo(needleX, 6.dp.toPx())
                close()
            }
            drawPath(path, RadioSignalNeedle)
        }
    }
}
