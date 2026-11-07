package com.example.util

import com.example.data.model.RadioBand
import com.example.data.model.RadioStation
import com.example.data.model.StationCategory

enum class VoiceIntentType(val titleAr: String) {
    STATION_NAME("اسم محطة إذاعية"),
    GENRE("نوع المحتوى / الموسيقى"),
    PROGRAM_NAME("اسم برنامج إذاعي"),
    FREQUENCY("تردد إذاعي مباشر"),
    WAVEBAND("نطاق موجة محدد"),
    GENERAL("بحث عام")
}

data class VoiceSearchResult(
    val spokenQuery: String,
    val intentType: VoiceIntentType,
    val intentDescription: String,
    val matchedStations: List<RadioStation>,
    val matchedDetail: String? = null
)

object VoiceSearchEngine {

    fun executeSearch(spokenQuery: String, allStations: List<RadioStation>): VoiceSearchResult {
        val cleanQuery = spokenQuery.trim()
        val normalizedQuery = ArabicTextNormalizer.normalize(cleanQuery)

        // 1. Detect Frequency Intent (e.g., "98.2", "تسعة فاصلة سبعة", "تردد 819", "621")
        val extractedFrequency = extractFrequencyNumber(cleanQuery)
        if (extractedFrequency != null) {
            val freqMatches = allStations.filter { station ->
                kotlin.math.abs(station.frequency - extractedFrequency) < 0.15 ||
                        station.frequencyDisplay.contains(extractedFrequency.toString()) ||
                        ArabicTextNormalizer.normalize(station.frequencyDisplay).contains(ArabicTextNormalizer.normalize(extractedFrequency.toString()))
            }
            if (freqMatches.isNotEmpty()) {
                return VoiceSearchResult(
                    spokenQuery = cleanQuery,
                    intentType = VoiceIntentType.FREQUENCY,
                    intentDescription = "تردد إذاعي: $extractedFrequency",
                    matchedStations = freqMatches,
                    matchedDetail = "محطات تبث بالقرب من التردد $extractedFrequency"
                )
            }
        }

        // 2. Detect Waveband Intent (e.g. "موجة قصيرة", "شورت ويف", "موجة متوسطة", "طويلة", "إف إم")
        val detectedBand = detectWaveband(normalizedQuery)
        if (detectedBand != null && (normalizedQuery.contains("موج") || normalizedQuery.contains("باند") || normalizedQuery.contains("نطاق") || normalizedQuery.length <= 8)) {
            val bandStations = allStations.filter { it.band == detectedBand }
            return VoiceSearchResult(
                spokenQuery = cleanQuery,
                intentType = VoiceIntentType.WAVEBAND,
                intentDescription = "نطاق: ${detectedBand.titleAr} (${detectedBand.code})",
                matchedStations = bandStations,
                matchedDetail = "محطات ${detectedBand.titleAr} (${detectedBand.rangeDisplay})"
            )
        }

        // 3. Detect Program Name IF query explicitly mentions "برنامج"
        val cleanProgQuery = cleanQuery.replace("برنامج", "").trim()
        if (cleanQuery.contains("برنامج") && cleanProgQuery.length > 2) {
            for (station in allStations) {
                for (program in station.popularPrograms) {
                    val cleanProg = program.replace("برنامج", "").trim()
                    if (ArabicTextNormalizer.normalize(program).contains(ArabicTextNormalizer.normalize(cleanProgQuery)) ||
                        ArabicTextNormalizer.normalize(cleanProgQuery).contains(ArabicTextNormalizer.normalize(cleanProg)) ||
                        ArabicTextNormalizer.matchesFuzzy(program, cleanProgQuery)
                    ) {
                        val matchingStations = allStations.filter { it.popularPrograms.any { p -> ArabicTextNormalizer.matchesFuzzy(p, program) } }
                        return VoiceSearchResult(
                            spokenQuery = cleanQuery,
                            intentType = VoiceIntentType.PROGRAM_NAME,
                            intentDescription = "برنامج: $program",
                            matchedStations = matchingStations.ifEmpty { listOf(station) },
                            matchedDetail = "البرنامج الشهير: \"$program\" على ${station.name}"
                        )
                    }
                }
            }
        }

        // 4. Detect Specific Station Name when query begins with "إذاعة" or "راديو", or names a prominent station
        if (normalizedQuery.startsWith("اذاع") || normalizedQuery.startsWith("راديو") ||
            normalizedQuery.contains("صوت العرب") || normalizedQuery.contains("بي بي سي") ||
            normalizedQuery.contains("مونت كارلو") || normalizedQuery.contains("البرنامج العام") ||
            normalizedQuery.contains("الشرق الاوسط") || normalizedQuery.contains("نجوم") ||
            normalizedQuery.contains("سوا") || normalizedQuery.contains("سكاي نيوز")
        ) {
            val stationNameMatches = allStations.filter { station ->
                val normStation = ArabicTextNormalizer.normalize(station.name)
                normStation.contains(normalizedQuery) || normalizedQuery.contains(normStation) ||
                        ArabicTextNormalizer.matchesFuzzy(station.name, cleanQuery)
            }
            if (stationNameMatches.isNotEmpty()) {
                return VoiceSearchResult(
                    spokenQuery = cleanQuery,
                    intentType = VoiceIntentType.STATION_NAME,
                    intentDescription = "محطة إذاعية: ${stationNameMatches.first().name}",
                    matchedStations = stationNameMatches,
                    matchedDetail = "تم العثور على ${stationNameMatches.size} إذاعة متطابقة"
                )
            }
        }

        // 5. Detect Genre / Music / Content Category Intent (e.g. "موسيقى كلاسيكية", "أخبار رياضية", "طرب", "أخبار")
        val detectedGenreResult = detectGenreOrMusic(normalizedQuery, allStations)
        if (detectedGenreResult != null) {
            return detectedGenreResult.copy(spokenQuery = cleanQuery)
        }

        // 6. Detect Specific Radio Program Name (when user didn't say the word "برنامج")
        for (station in allStations) {
            for (program in station.popularPrograms) {
                val cleanProg = program.replace("برنامج", "").trim()
                if (ArabicTextNormalizer.normalize(program).contains(ArabicTextNormalizer.normalize(cleanProgQuery)) ||
                    ArabicTextNormalizer.normalize(cleanProgQuery).contains(ArabicTextNormalizer.normalize(cleanProg))
                ) {
                    val matchingStations = allStations.filter { it.popularPrograms.any { p -> ArabicTextNormalizer.matchesFuzzy(p, program) } }
                    return VoiceSearchResult(
                        spokenQuery = cleanQuery,
                        intentType = VoiceIntentType.PROGRAM_NAME,
                        intentDescription = "برنامج: $program",
                        matchedStations = matchingStations.ifEmpty { listOf(station) },
                        matchedDetail = "البرنامج الشهير: \"$program\" على ${station.name}"
                    )
                }
            }
        }

        // 6. General Station Name Fallback
        val stationNameMatches = allStations.filter { station ->
            ArabicTextNormalizer.matchesFuzzy(station.name, cleanQuery) ||
                    ArabicTextNormalizer.matchesFuzzy(cleanQuery, station.name) ||
                    station.tags.any { tag -> ArabicTextNormalizer.matchesFuzzy(tag, cleanQuery) }
        }

        if (stationNameMatches.isNotEmpty()) {
            return VoiceSearchResult(
                spokenQuery = cleanQuery,
                intentType = VoiceIntentType.STATION_NAME,
                intentDescription = "محطة إذاعية: ${stationNameMatches.first().name}",
                matchedStations = stationNameMatches,
                matchedDetail = "تم العثور على ${stationNameMatches.size} إذاعة متطابقة"
            )
        }

        // 6. Broad search across description, country, tags, and category
        val broadMatches = allStations.filter { station ->
            ArabicTextNormalizer.matchesFuzzy(station.country, cleanQuery) ||
                    ArabicTextNormalizer.matchesFuzzy(station.description, cleanQuery) ||
                    station.popularPrograms.any { p -> ArabicTextNormalizer.matchesFuzzy(p, cleanQuery) } ||
                    station.tags.any { t -> ArabicTextNormalizer.matchesFuzzy(t, cleanQuery) }
        }

        return VoiceSearchResult(
            spokenQuery = cleanQuery,
            intentType = VoiceIntentType.GENERAL,
            intentDescription = "بحث صوتي: \"$cleanQuery\"",
            matchedStations = broadMatches,
            matchedDetail = if (broadMatches.isNotEmpty()) "تم العثور على ${broadMatches.size} محطة" else "لم يتم العثور على تطابق مباشر، جرب نطق اسم المحطة أو نوعها"
        )
    }

    private fun detectWaveband(normalizedQuery: String): RadioBand? {
        return when {
            normalizedQuery.contains("قصير") || normalizedQuery.contains("شورت") || normalizedQuery.contains("sw") -> RadioBand.SW
            normalizedQuery.contains("متوسط") || normalizedQuery.contains("ميديم") || normalizedQuery.contains("am") || normalizedQuery.contains("mw") -> RadioBand.MW
            normalizedQuery.contains("طويل") || normalizedQuery.contains("لونغ") || normalizedQuery.contains("lw") -> RadioBand.LW
            normalizedQuery.contains("اف ام") || normalizedQuery.contains("fm") -> RadioBand.FM
            else -> null
        }
    }

    private fun detectGenreOrMusic(normalizedQuery: String, allStations: List<RadioStation>): VoiceSearchResult? {
        // Classical Music (موسيقى كلاسيكية / سيمفونيات)
        if (normalizedQuery.contains("كلاسيك") || normalizedQuery.contains("سمفوني") ||
            (normalizedQuery.contains("موسيق") && normalizedQuery.contains("هادئ")) ||
            (normalizedQuery.contains("موسيق") && normalizedQuery.contains("عالمي"))
        ) {
            val matches = allStations.filter { it.category == StationCategory.CLASSICAL_MUSIC || it.tags.any { t -> t.contains("كلاسيك") } }
            return VoiceSearchResult(
                spokenQuery = "",
                intentType = VoiceIntentType.GENRE,
                intentDescription = "نوع المحتوى: موسيقى كلاسيكية وسيمفونيات",
                matchedStations = matches,
                matchedDetail = "محطات الموسيقى العالمية والكلاسيكية الراقية"
            )
        }

        // Sports News & Matches (أخبار رياضية / رياضة / مباريات)
        if (normalizedQuery.contains("رياض") || normalizedQuery.contains("مبار") || normalizedQuery.contains("كوره") || normalizedQuery.contains("دوري") || normalizedQuery.contains("ملعب")) {
            val matches = allStations.filter { it.category == StationCategory.SPORTS || it.tags.any { t -> t.contains("رياض") } }
            return VoiceSearchResult(
                spokenQuery = "",
                intentType = VoiceIntentType.GENRE,
                intentDescription = "نوع المحتوى: أخبار رياضية ومباريات",
                matchedStations = matches,
                matchedDetail = "محطات البث والتحليل الرياضي المباشر"
            )
        }

        // Tarab & Vintage Music (طرب / زمن جميل / أم كلثوم / عبد الوهاب / فيروز)
        if (normalizedQuery.contains("طرب") || normalizedQuery.contains("زمان") || normalizedQuery.contains("كلثوم") ||
            normalizedQuery.contains("وهاب") || normalizedQuery.contains("فيروز") || normalizedQuery.contains("اصيل")
        ) {
            val matches = allStations.filter { it.category == StationCategory.TARAB || it.tags.any { t -> t.contains("طرب") || t.contains("كلثوم") } }
            return VoiceSearchResult(
                spokenQuery = "",
                intentType = VoiceIntentType.GENRE,
                intentDescription = "نوع المحتوى: طرب وزمن الفن الجميل",
                matchedStations = matches,
                matchedDetail = "إذاعات روائع الطرب العربي الخالد"
            )
        }

        // Holy Quran & Religious (قرآن كريم / تلاوات / دين / إسلاميات)
        if (normalizedQuery.contains("قران") || normalizedQuery.contains("تلاو") || normalizedQuery.contains("اسلام") || normalizedQuery.contains("دين") || normalizedQuery.contains("اذكار")) {
            val matches = allStations.filter { it.category == StationCategory.QURAN || it.tags.any { t -> t.contains("قران") } }
            return VoiceSearchResult(
                spokenQuery = "",
                intentType = VoiceIntentType.GENRE,
                intentDescription = "نوع المحتوى: قرآن كريم وتلاوات خاشعة",
                matchedStations = matches,
                matchedDetail = "محطات القرآن الكريم والدعوة والتلاوات"
            )
        }

        // News & Talks (أخبار / سياسة / تحليلات / نشرة)
        if (normalizedQuery.contains("اخبار") || normalizedQuery.contains("نشر") || normalizedQuery.contains("سياس") || normalizedQuery.contains("تحليل") || normalizedQuery.contains("حوار")) {
            val matches = allStations.filter { it.category == StationCategory.NEWS || it.tags.any { t -> t.contains("أخبار") || t.contains("اخبار") } }
            return VoiceSearchResult(
                spokenQuery = "",
                intentType = VoiceIntentType.GENRE,
                intentDescription = "نوع المحتوى: أخبار وحوارات وتحليلات",
                matchedStations = matches,
                matchedDetail = "محطات الأخبار والتقارير الإقليمية والعالمية"
            )
        }

        // Culture & Documentaries (ثقافة / دوليات / وثائقي)
        if (normalizedQuery.contains("ثقاف") || normalizedQuery.contains("وثائق") || normalizedQuery.contains("ادب") || normalizedQuery.contains("معرف")) {
            val matches = allStations.filter { it.category == StationCategory.CULTURE || it.tags.any { t -> t.contains("ثقافة") } }
            return VoiceSearchResult(
                spokenQuery = "",
                intentType = VoiceIntentType.GENRE,
                intentDescription = "نوع المحتوى: ثقافة وفكر وبرامج دولية",
                matchedStations = matches,
                matchedDetail = "إذاعات برامج الفكر والمعرفة والتراث"
            )
        }

        return null
    }

    private fun extractFrequencyNumber(text: String): Double? {
        val converted = ArabicTextNormalizer.convertArabicDigitsToLatin(text)
        val regex = Regex("""\b(\d+(\.\d+)?)\b""")
        val match = regex.find(converted)
        return match?.value?.toDoubleOrNull()
    }
}
