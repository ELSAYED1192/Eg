package com.example

import com.example.data.model.RadioBand
import com.example.data.model.StationCategory
import com.example.data.repository.CuratedStations
import com.example.util.ArabicTextNormalizer
import com.example.util.VoiceIntentType
import com.example.util.VoiceSearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testArabicNormalizer() {
        val original = "إِذَاعَةُ القُرْآنِ الكَرِيمِ"
        val normalized = ArabicTextNormalizer.normalize(original)
        assertTrue(normalized.contains("قران"))
        assertTrue(normalized.contains("اذاعه"))
    }

    @Test
    fun testVoiceSearchMusicGenre() {
        val stations = CuratedStations.initialStations

        // Test 1: Classical music query
        val resultClassical = VoiceSearchEngine.executeSearch("موسيقى كلاسيكية", stations)
        assertEquals(VoiceIntentType.GENRE, resultClassical.intentType)
        assertTrue(resultClassical.matchedStations.any { it.category == StationCategory.CLASSICAL_MUSIC })

        // Test 2: Sports news query
        val resultSports = VoiceSearchEngine.executeSearch("أخبار رياضية", stations)
        assertEquals(VoiceIntentType.GENRE, resultSports.intentType)
        assertTrue(resultSports.matchedStations.any { it.category == StationCategory.SPORTS })

        // Test 3: Tarab query
        val resultTarab = VoiceSearchEngine.executeSearch("طرب وأغاني زمان", stations)
        assertEquals(VoiceIntentType.GENRE, resultTarab.intentType)
        assertTrue(resultTarab.matchedStations.any { it.category == StationCategory.TARAB })
    }

    @Test
    fun testVoiceSearchProgramName() {
        val stations = CuratedStations.initialStations

        // Test famous program "كلمتين وبس"
        val resultProg = VoiceSearchEngine.executeSearch("برنامج كلمتين وبس", stations)
        assertEquals(VoiceIntentType.PROGRAM_NAME, resultProg.intentType)
        assertTrue(resultProg.matchedStations.isNotEmpty())
        assertTrue(resultProg.matchedStations.first().popularPrograms.any { it.contains("كلمتين وبس") })
    }

    @Test
    fun testVoiceSearchStationName() {
        val stations = CuratedStations.initialStations

        val resultStation = VoiceSearchEngine.executeSearch("إذاعة القرآن الكريم", stations)
        assertEquals(VoiceIntentType.STATION_NAME, resultStation.intentType)
        assertTrue(resultStation.matchedStations.any { it.name.contains("القرآن الكريم") })
    }

    @Test
    fun testVoiceSearchFrequency() {
        val stations = CuratedStations.initialStations

        val resultFreq = VoiceSearchEngine.executeSearch("تردد 9.755", stations)
        assertEquals(VoiceIntentType.FREQUENCY, resultFreq.intentType)
        assertTrue(resultFreq.matchedStations.any { it.frequency == 9.755 })
    }

    @Test
    fun testVoiceSearchWaveband() {
        val stations = CuratedStations.initialStations

        val resultSW = VoiceSearchEngine.executeSearch("موجة قصيرة", stations)
        assertEquals(VoiceIntentType.WAVEBAND, resultSW.intentType)
        assertTrue(resultSW.matchedStations.all { it.band == RadioBand.SW })
    }

    @Test
    fun testVolumeBoostMetrics() {
        // PlayerState volume range is 0.0f .. 2.0f
        val normalState = com.example.player.PlayerState(volume = 1.0f, isVolumeBoosted = false)
        assertEquals(1.0f, normalState.volume, 0.001f)
        assertTrue(!normalState.isVolumeBoosted)

        val boostedState = com.example.player.PlayerState(volume = 2.0f, isVolumeBoosted = true)
        assertEquals(2.0f, boostedState.volume, 0.001f)
        assertTrue(boostedState.isVolumeBoosted)
    }

    @Test
    fun testThemeAndPaletteConfig() {
        val defaultTheme = com.example.ui.theme.ThemeConfig()
        assertEquals(com.example.ui.theme.AppThemeMode.DARK, defaultTheme.mode)
        assertEquals(com.example.ui.theme.ColorPaletteType.NEON_CYBER, defaultTheme.paletteType)

        // Custom wallpaper configuration
        val customConfig = defaultTheme.copy(
            customWallpaperUri = "content://media/external/images/media/42",
            wallpaperBlurDp = 8f,
            wallpaperDimAlpha = 0.65f
        )
        assertNotNull(customConfig.customWallpaperUri)
        assertEquals(8f, customConfig.wallpaperBlurDp, 0.001f)
        assertEquals(0.65f, customConfig.wallpaperDimAlpha, 0.001f)
    }
}
