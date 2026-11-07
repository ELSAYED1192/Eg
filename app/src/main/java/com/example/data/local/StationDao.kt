package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {

    @Query("SELECT * FROM stations ORDER BY id ASC")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE presetNumber IS NOT NULL ORDER BY presetNumber ASC")
    fun getPresetStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE band = :band ORDER BY frequency ASC")
    fun getStationsByBand(band: String): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE category = :category ORDER BY name ASC")
    fun getStationsByCategory(category: String): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE id = :id LIMIT 1")
    suspend fun getStationById(id: Long): StationEntity?

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<StationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: StationEntity): Long

    @Update
    suspend fun update(station: StationEntity)

    @Delete
    suspend fun delete(station: StationEntity)

    @Query("UPDATE stations SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE stations SET presetNumber = :presetNumber WHERE id = :id")
    suspend fun updatePreset(id: Long, presetNumber: Int?)

    @Query("UPDATE stations SET presetNumber = NULL WHERE presetNumber = :presetNumber")
    suspend fun clearPreset(presetNumber: Int)

    @Query("SELECT * FROM stations WHERE name LIKE '%' || :query || '%' OR frequencyDisplay LIKE '%' || :query || '%' OR popularProgramsRaw LIKE '%' || :query || '%' OR tagsRaw LIKE '%' || :query || '%' OR country LIKE '%' || :query || '%'")
    suspend fun searchStations(query: String): List<StationEntity>
}
