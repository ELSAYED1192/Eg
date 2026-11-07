package com.example.data.repository

import com.example.data.local.RadioDatabase
import com.example.data.local.StationEntity
import com.example.data.model.RadioBand
import com.example.data.model.RadioStation
import com.example.data.model.StationCategory
import com.example.data.remote.RadioApiService
import com.example.util.ArabicTextNormalizer
import com.example.util.VoiceSearchEngine
import com.example.util.VoiceSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

class RadioRepository(
    private val database: RadioDatabase,
    private val apiService: RadioApiService = RadioApiService.create()
) {
    private val stationDao = database.stationDao()

    val allStations: Flow<List<RadioStation>> = stationDao.getAllStations().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoriteStations: Flow<List<RadioStation>> = stationDao.getFavoriteStations().map { entities ->
        entities.map { it.toDomain() }
    }

    val presetStations: Flow<List<RadioStation>> = stationDao.getPresetStations().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val count = stationDao.getCount()
        if (count == 0) {
            val entities = CuratedStations.initialStations.map {
                StationEntity.fromDomain(it)
            }
            stationDao.insertAll(entities)
        }
    }

    suspend fun toggleFavorite(stationId: Long, currentIsFavorite: Boolean) = withContext(Dispatchers.IO) {
        stationDao.updateFavorite(stationId, !currentIsFavorite)
    }

    suspend fun assignPreset(stationId: Long, presetNumber: Int) = withContext(Dispatchers.IO) {
        stationDao.clearPreset(presetNumber)
        stationDao.updatePreset(stationId, presetNumber)
    }

    suspend fun addCustomStation(
        name: String,
        frequency: Double,
        band: RadioBand,
        streamUrl: String,
        country: String,
        category: StationCategory,
        programs: List<String>,
        tags: List<String>,
        description: String
    ): Long = withContext(Dispatchers.IO) {
        val unit = when (band) {
            RadioBand.SW, RadioBand.FM -> "MHz"
            RadioBand.MW, RadioBand.LW -> "kHz"
            RadioBand.ALL -> "MHz"
        }
        val freqDisplay = "$frequency $unit"
        val flag = when (country.trim()) {
            "مصر" -> "🇪🇬"
            "السعودية" -> "🇸🇦"
            "الإمارات" -> "🇦🇪"
            "الكويت" -> "🇰🇼"
            "المغرب" -> "🇲🇦"
            "الجزائر" -> "🇩🇿"
            "فلسطين" -> "🇵🇸"
            "الأردن" -> "🇯🇴"
            "بريطانيا" -> "🇬🇧"
            "فرنسا" -> "🇫🇷"
            "ألمانيا" -> "🇩🇪"
            else -> "📻"
        }

        val newStation = RadioStation(
            name = name,
            frequency = frequency,
            frequencyDisplay = freqDisplay,
            band = band,
            meterBand = if (band == RadioBand.SW) "19-31m" else null,
            streamUrl = streamUrl,
            country = country,
            countryFlag = flag,
            category = category,
            popularPrograms = programs,
            tags = tags,
            description = description,
            isFavorite = false,
            presetNumber = null,
            bitrateKbps = 128
        )

        val entity = StationEntity.fromDomain(newStation, isCustom = true)
        stationDao.insert(entity)
    }

    suspend fun syncFrequenciesOnline(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val remoteDtos = apiService.searchStationsByLanguage(language = "arabic", limit = 30)
            val newEntities = mutableListOf<StationEntity>()

            for (dto in remoteDtos) {
                val streamUrl = dto.urlResolved ?: dto.url
                if (streamUrl.isBlank() || (!streamUrl.startsWith("http://") && !streamUrl.startsWith("https://"))) {
                    continue
                }

                // Determine approximate frequency or band based on tags and name
                val name = dto.name.trim()
                val tags = (dto.tags ?: "").lowercase(Locale.ROOT)
                val country = dto.country ?: "عالمي"
                val countryFlag = when (dto.countryCode?.uppercase(Locale.ROOT)) {
                    "EG" -> "🇪🇬"
                    "SA" -> "🇸🇦"
                    "AE" -> "🇦🇪"
                    "MA" -> "🇲🇦"
                    "DZ" -> "🇩🇿"
                    "PS" -> "🇵🇸"
                    "JO" -> "🇯🇴"
                    "KW" -> "🇰🇼"
                    "FR" -> "🇫🇷"
                    "GB" -> "🇬🇧"
                    else -> "📻"
                }

                val category = when {
                    tags.contains("quran") || tags.contains("islam") || name.contains("قرآن") -> StationCategory.QURAN
                    tags.contains("sport") || name.contains("رياض") -> StationCategory.SPORTS
                    tags.contains("classic") || tags.contains("symphon") -> StationCategory.CLASSICAL_MUSIC
                    tags.contains("news") || name.contains("أخبار") || name.contains("اخبار") -> StationCategory.NEWS
                    tags.contains("tarab") || tags.contains("oldies") || name.contains("طرب") -> StationCategory.TARAB
                    tags.contains("culture") -> StationCategory.CULTURE
                    else -> StationCategory.VARIETY
                }

                val band = when {
                    tags.contains("shortwave") || tags.contains("sw") || name.contains("SW") -> RadioBand.SW
                    tags.contains("am") || tags.contains("mediumwave") || name.contains("AM") || name.contains("MW") -> RadioBand.MW
                    tags.contains("lw") || tags.contains("longwave") -> RadioBand.LW
                    else -> RadioBand.FM
                }

                val defaultFreq = when (band) {
                    RadioBand.SW -> 9.0 + (dto.stationUuid.hashCode() % 1500) / 100.0
                    RadioBand.MW -> 600.0 + (dto.stationUuid.hashCode() % 900)
                    RadioBand.LW -> 150.0 + (dto.stationUuid.hashCode() % 130)
                    RadioBand.FM -> 88.0 + (dto.stationUuid.hashCode() % 200) / 10.0
                    RadioBand.ALL -> 100.0
                }

                val unit = if (band == RadioBand.SW || band == RadioBand.FM) "MHz" else "kHz"
                val freqDisplay = String.format(Locale.US, "%.1f %s", defaultFreq, unit)

                newEntities.add(
                    StationEntity(
                        name = name,
                        frequency = defaultFreq,
                        frequencyDisplay = freqDisplay,
                        band = band.name,
                        meterBand = if (band == RadioBand.SW) "31m" else null,
                        streamUrl = streamUrl,
                        country = country,
                        countryFlag = countryFlag,
                        category = category.name,
                        popularProgramsRaw = "بث مباشر عبر الإنترنت||برامج إذاعية منوعة",
                        tagsRaw = dto.tags ?: "إنترنت, إذاعة, بث مباشر",
                        description = "تم تحديثها تلقائياً عبر قاعدة بيانات محطات الراديو العالمية.",
                        isFavorite = false,
                        presetNumber = null,
                        bitrateKbps = dto.bitrate ?: 128,
                        isCustom = false
                    )
                )
            }

            if (newEntities.isNotEmpty()) {
                stationDao.insertAll(newEntities)
            }
            Result.success(newEntities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun performVoiceSearch(query: String, currentList: List<RadioStation>): VoiceSearchResult = withContext(Dispatchers.Default) {
        VoiceSearchEngine.executeSearch(query, currentList)
    }
}
