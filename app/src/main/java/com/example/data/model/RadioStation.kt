package com.example.data.model

enum class RadioBand(val code: String, val titleAr: String, val titleEn: String, val rangeDisplay: String, val defaultUnit: String) {
    ALL("ALL", "جميع الموجات", "All Bands", "LW / MW / SW / FM", ""),
    LW("LW", "موجة طويلة", "Longwave", "148.5 - 283.5 kHz", "kHz"),
    MW("MW", "موجة متوسطة (AM)", "Mediumwave", "531 - 1602 kHz", "kHz"),
    SW("SW", "موجة قصيرة", "Shortwave", "2.3 - 26.1 MHz", "MHz"),
    FM("FM", "إف إم", "FM Band", "87.5 - 108.0 MHz", "MHz")
}

enum class StationCategory(val titleAr: String, val iconName: String) {
    ALL("الكل", "Radio"),
    QURAN("قرآن ودين", "MenuBook"),
    NEWS("أخبار وحوارات", "Newspaper"),
    SPORTS("أخبار رياضية", "SportsSoccer"),
    CLASSICAL_MUSIC("موسيقى كلاسيكية", "MusicNote"),
    TARAB("طرب وزمن جميل", "LibraryMusic"),
    CULTURE("ثقافة ودوليات", "Public"),
    VARIETY("منوعات وترفيه", "Headphones")
}

data class RadioStation(
    val id: Long = 0,
    val name: String,
    val frequency: Double,
    val frequencyDisplay: String,
    val band: RadioBand,
    val meterBand: String? = null, // e.g. "31m", "49m", "19m"
    val streamUrl: String,
    val country: String,
    val countryFlag: String,
    val category: StationCategory,
    val popularPrograms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val description: String = "",
    val isFavorite: Boolean = false,
    val presetNumber: Int? = null, // 1 to 5
    val bitrateKbps: Int = 128
)
