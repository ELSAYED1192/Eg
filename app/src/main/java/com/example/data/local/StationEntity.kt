package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.RadioBand
import com.example.data.model.RadioStation
import com.example.data.model.StationCategory

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val frequency: Double,
    val frequencyDisplay: String,
    val band: String,
    val meterBand: String? = null,
    val streamUrl: String,
    val country: String,
    val countryFlag: String,
    val category: String,
    val popularProgramsRaw: String = "", // Comma-separated or newline
    val tagsRaw: String = "",
    val description: String = "",
    val isFavorite: Boolean = false,
    val presetNumber: Int? = null,
    val bitrateKbps: Int = 128,
    val isCustom: Boolean = false
) {
    fun toDomain(): RadioStation {
        val parsedBand = try {
            RadioBand.valueOf(band)
        } catch (e: Exception) {
            RadioBand.FM
        }

        val parsedCategory = try {
            StationCategory.valueOf(category)
        } catch (e: Exception) {
            StationCategory.VARIETY
        }

        return RadioStation(
            id = id,
            name = name,
            frequency = frequency,
            frequencyDisplay = frequencyDisplay,
            band = parsedBand,
            meterBand = meterBand,
            streamUrl = streamUrl,
            country = country,
            countryFlag = countryFlag,
            category = parsedCategory,
            popularPrograms = if (popularProgramsRaw.isBlank()) emptyList() else popularProgramsRaw.split("||"),
            tags = if (tagsRaw.isBlank()) emptyList() else tagsRaw.split(",").map { it.trim() },
            description = description,
            isFavorite = isFavorite,
            presetNumber = presetNumber,
            bitrateKbps = bitrateKbps
        )
    }

    companion object {
        fun fromDomain(station: RadioStation, isCustom: Boolean = false): StationEntity {
            return StationEntity(
                id = station.id,
                name = station.name,
                frequency = station.frequency,
                frequencyDisplay = station.frequencyDisplay,
                band = station.band.name,
                meterBand = station.meterBand,
                streamUrl = station.streamUrl,
                country = station.country,
                countryFlag = station.countryFlag,
                category = station.category.name,
                popularProgramsRaw = station.popularPrograms.joinToString("||"),
                tagsRaw = station.tags.joinToString(","),
                description = station.description,
                isFavorite = station.isFavorite,
                presetNumber = station.presetNumber,
                bitrateKbps = station.bitrateKbps,
                isCustom = isCustom
            )
        }
    }
}
