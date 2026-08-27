package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passport_photos")
data class PassportPhotoEntity(
    @PrimaryKey
    val id: String,
    val stopId: String,
    val stopName: String,
    val questId: String = "",
    val questTitle: String = "",
    val photoBase64: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String = "",
    val syncedToFirebase: Boolean = false
)
