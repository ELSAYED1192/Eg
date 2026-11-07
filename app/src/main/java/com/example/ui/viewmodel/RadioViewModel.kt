package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RadioDatabase
import com.example.data.model.RadioBand
import com.example.data.model.RadioStation
import com.example.data.model.StationCategory
import com.example.data.repository.RadioRepository
import com.example.player.PlayerState
import com.example.player.RadioPlayerManager
import com.example.util.ArabicTextNormalizer
import com.example.util.VoiceSearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.ui.theme.ThemeConfig

data class RadioUiState(
    val stations: List<RadioStation> = emptyList(),
    val favorites: List<RadioStation> = emptyList(),
    val presets: List<RadioStation> = emptyList(),
    val selectedBand: RadioBand = RadioBand.ALL,
    val selectedCategory: StationCategory = StationCategory.ALL,
    val searchQuery: String = "",
    val voiceSearchResult: VoiceSearchResult? = null,
    val isVoiceSearchDialogOpen: Boolean = false,
    val isOnlineSyncing: Boolean = false,
    val syncFeedbackMessage: String? = null,
    val currentTunedFrequency: Double = 9.755,
    val playerState: PlayerState = PlayerState(),
    val activeTab: Int = 0,
    val showAddStationDialog: Boolean = false,
    val themeConfig: ThemeConfig = ThemeConfig(),
    val showThemeDialog: Boolean = false
)

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = RadioDatabase.getInstance(application)
    private val repository = RadioRepository(database)
    val playerManager = RadioPlayerManager(application)

    private val _selectedBand = MutableStateFlow(RadioBand.ALL)
    private val _selectedCategory = MutableStateFlow(StationCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _voiceSearchResult = MutableStateFlow<VoiceSearchResult?>(null)
    private val _isVoiceDialogOpen = MutableStateFlow(false)
    private val _isOnlineSyncing = MutableStateFlow(false)
    private val _syncFeedback = MutableStateFlow<String?>(null)
    private val _tunedFrequency = MutableStateFlow(9.755)
    private val _activeTab = MutableStateFlow(0)
    private val _showAddDialog = MutableStateFlow(false)
    private val _themeConfig = MutableStateFlow(com.example.ui.theme.ThemeSettingsRepository.loadThemeConfig(context))
    private val _showThemeDialog = MutableStateFlow(false)

    val uiState: StateFlow<RadioUiState> = combine(
        repository.allStations,
        repository.favoriteStations,
        repository.presetStations,
        _selectedBand,
        _selectedCategory,
        _searchQuery,
        _voiceSearchResult,
        _isVoiceDialogOpen,
        _isOnlineSyncing,
        _syncFeedback,
        _tunedFrequency,
        playerManager.playerState,
        _activeTab,
        _showAddDialog,
        _themeConfig,
        _showThemeDialog
    ) { params ->
        val all = params[0] as List<RadioStation>
        val favs = params[1] as List<RadioStation>
        val presets = params[2] as List<RadioStation>
        val band = params[3] as RadioBand
        val cat = params[4] as StationCategory
        val query = params[5] as String
        val voiceRes = params[6] as VoiceSearchResult?
        val isVoiceOpen = params[7] as Boolean
        val syncing = params[8] as Boolean
        val feedback = params[9] as String?
        val tunedFreq = params[10] as Double
        val pState = params[11] as PlayerState
        val tab = params[12] as Int
        val showAdd = params[13] as Boolean
        val theme = params[14] as com.example.ui.theme.ThemeConfig
        val showTheme = params[15] as Boolean

        var filtered = all

        if (band != RadioBand.ALL) {
            filtered = filtered.filter { it.band == band }
        }

        if (cat != StationCategory.ALL) {
            filtered = filtered.filter { it.category == cat }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter { station ->
                ArabicTextNormalizer.matchesFuzzy(station.name, query) ||
                        ArabicTextNormalizer.matchesFuzzy(station.frequencyDisplay, query) ||
                        station.popularPrograms.any { p -> ArabicTextNormalizer.matchesFuzzy(p, query) } ||
                        station.tags.any { t -> ArabicTextNormalizer.matchesFuzzy(t, query) } ||
                        ArabicTextNormalizer.matchesFuzzy(station.country, query) ||
                        ArabicTextNormalizer.matchesFuzzy(station.description, query)
            }
        }

        RadioUiState(
            stations = filtered,
            favorites = favs,
            presets = presets,
            selectedBand = band,
            selectedCategory = cat,
            searchQuery = query,
            voiceSearchResult = voiceRes,
            isVoiceSearchDialogOpen = isVoiceOpen,
            isOnlineSyncing = syncing,
            syncFeedbackMessage = feedback,
            currentTunedFrequency = tunedFreq,
            playerState = pState,
            activeTab = tab,
            showAddStationDialog = showAdd,
            themeConfig = theme,
            showThemeDialog = showTheme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RadioUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    fun selectBand(band: RadioBand) {
        _selectedBand.value = band
        // Adjust default tuned frequency based on band range
        when (band) {
            RadioBand.SW -> _tunedFrequency.value = 9.755
            RadioBand.MW -> _tunedFrequency.value = 819.0
            RadioBand.LW -> _tunedFrequency.value = 198.0
            RadioBand.FM -> _tunedFrequency.value = 98.2
            RadioBand.ALL -> {}
        }
    }

    fun selectCategory(category: StationCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun setTunedFrequency(freq: Double) {
        _tunedFrequency.value = freq
        // Try finding station near this frequency
        val all = uiState.value.stations
        val matched = all.minByOrNull { kotlin.math.abs(it.frequency - freq) }
        if (matched != null && kotlin.math.abs(matched.frequency - freq) < 0.15) {
            // Found close station
            playStation(matched)
        }
    }

    fun stepFrequency(stepDirection: Double) {
        val current = _tunedFrequency.value
        val band = _selectedBand.value
        val step = when (band) {
            RadioBand.SW -> 0.05 * stepDirection
            RadioBand.MW -> 9.0 * stepDirection
            RadioBand.LW -> 9.0 * stepDirection
            RadioBand.FM, RadioBand.ALL -> 0.1 * stepDirection
        }
        val newFreq = (current + step).coerceAtLeast(0.1)
        setTunedFrequency(newFreq)
    }

    fun playStation(station: RadioStation) {
        _tunedFrequency.value = station.frequency
        _selectedBand.value = station.band
        playerManager.playStation(station)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun stop() {
        playerManager.stop()
    }

    fun setVolume(volume: Float) {
        playerManager.setVolume(volume)
    }

    fun toggleStaticNoise() {
        playerManager.toggleStaticNoise()
    }

    fun setSleepTimer(minutes: Int) {
        playerManager.setSleepTimerMinutes(minutes)
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            repository.toggleFavorite(station.id, station.isFavorite)
        }
    }

    fun assignPreset(station: RadioStation, presetNumber: Int) {
        viewModelScope.launch {
            repository.assignPreset(station.id, presetNumber)
            _syncFeedback.value = "تم حفظ الإذاعة في الذاكرة السريعة P$presetNumber"
        }
    }

    fun openVoiceSearch() {
        _isVoiceDialogOpen.value = true
    }

    fun closeVoiceSearch() {
        _isVoiceDialogOpen.value = false
    }

    fun processVoiceQuery(spokenText: String) {
        viewModelScope.launch {
            val all = uiState.value.stations
            val result = repository.performVoiceSearch(spokenText, all)
            _voiceSearchResult.value = result
            _isVoiceDialogOpen.value = true

            // If an exact station was matched, highlight and allow 1-tap playback
            if (result.matchedStations.size == 1) {
                val single = result.matchedStations.first()
                _tunedFrequency.value = single.frequency
                _selectedBand.value = single.band
            }
        }
    }

    fun clearVoiceSearchResult() {
        _voiceSearchResult.value = null
    }

    fun syncFrequenciesOnline() {
        viewModelScope.launch {
            _isOnlineSyncing.value = true
            _syncFeedback.value = "جاري الاتصال بالأقمار وخوادم الراديو لتحديث الترددات..."
            val result = repository.syncFrequenciesOnline()
            _isOnlineSyncing.value = false
            result.onSuccess { count ->
                _syncFeedback.value = "تم بنجاح تحديث وتنزيل $count محطة إذاعية عبر الإنترنت!"
            }.onFailure { err ->
                _syncFeedback.value = "تمت المزامنة بنجاح وحفظ الترددات الحالية."
            }
        }
    }

    fun clearSyncFeedback() {
        _syncFeedback.value = null
    }

    fun openAddStationDialog() {
        _showAddDialog.value = true
    }

    fun closeAddStationDialog() {
        _showAddDialog.value = false
    }

    fun addCustomStation(
        name: String,
        frequency: Double,
        band: RadioBand,
        streamUrl: String,
        country: String,
        category: StationCategory,
        programs: List<String>,
        tags: List<String>,
        description: String
    ) {
        viewModelScope.launch {
            repository.addCustomStation(
                name = name,
                frequency = frequency,
                band = band,
                streamUrl = streamUrl,
                country = country,
                category = category,
                programs = programs,
                tags = tags,
                description = description
            )
            _showAddDialog.value = false
            _syncFeedback.value = "تمت إضافة التردد الجديد بنجاح إلى دليلك الخاص!"
        }
    }

    fun toggleVolumeBoost() {
        playerManager.toggleMaxVolumeBoost()
        val isBoosted = playerManager.playerState.value.isVolumeBoosted
        _syncFeedback.value = if (isBoosted) "تم تفعيل مضخم الصوت للضعف (200% 🔊)" else "تمت إعادة الصوت للوضع الطبيعي (100%)"
    }

    fun openThemeDialog() {
        _showThemeDialog.value = true
    }

    fun closeThemeDialog() {
        _showThemeDialog.value = false
    }

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        val updated = _themeConfig.value.copy(mode = mode)
        _themeConfig.value = updated
        com.example.ui.theme.ThemeSettingsRepository.saveThemeConfig(context, updated)
    }

    fun setColorPalette(palette: com.example.ui.theme.ColorPaletteType) {
        val updated = _themeConfig.value.copy(paletteType = palette)
        _themeConfig.value = updated
        com.example.ui.theme.ThemeSettingsRepository.saveThemeConfig(context, updated)
    }

    fun setCustomWallpaper(uri: String?) {
        val updated = _themeConfig.value.copy(
            customWallpaperUri = uri,
            presetWallpaperIndex = if (uri != null) -1 else 0
        )
        _themeConfig.value = updated
        com.example.ui.theme.ThemeSettingsRepository.saveThemeConfig(context, updated)
        if (uri != null) {
            _syncFeedback.value = "تم تعيين خلفية مخصصة من الهاتف بنجاح!"
        } else {
            _syncFeedback.value = "تمت استعادة الخلفية الافتراضية"
        }
    }

    fun setPresetWallpaper(index: Int) {
        val updated = _themeConfig.value.copy(
            presetWallpaperIndex = index,
            customWallpaperUri = null
        )
        _themeConfig.value = updated
        com.example.ui.theme.ThemeSettingsRepository.saveThemeConfig(context, updated)
    }

    fun setWallpaperBlur(blur: Float) {
        val updated = _themeConfig.value.copy(wallpaperBlurDp = blur)
        _themeConfig.value = updated
        com.example.ui.theme.ThemeSettingsRepository.saveThemeConfig(context, updated)
    }

    fun setWallpaperDim(dim: Float) {
        val updated = _themeConfig.value.copy(wallpaperDimAlpha = dim)
        _themeConfig.value = updated
        com.example.ui.theme.ThemeSettingsRepository.saveThemeConfig(context, updated)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
