package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PassportPhotoDao {
    @Query("SELECT * FROM passport_photos ORDER BY timestamp DESC")
    fun getAllPassportPhotos(): Flow<List<PassportPhotoEntity>>

    @Query("SELECT * FROM passport_photos ORDER BY timestamp DESC")
    suspend fun getAllPassportPhotosSync(): List<PassportPhotoEntity>

    @Query("SELECT * FROM passport_photos WHERE stopId = :stopId LIMIT 1")
    suspend fun getPhotoForStop(stopId: String): PassportPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassportPhoto(photo: PassportPhotoEntity)

    @Query("DELETE FROM passport_photos WHERE id = :id")
    suspend fun deletePassportPhoto(id: String)

    @Query("DELETE FROM passport_photos")
    suspend fun deleteAllPassportPhotos()
}
