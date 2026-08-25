package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val totalSteps: Int = 0,
    val completedCheckpoints: Int = 0,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val completedQuestsCount: Int = 0,
    val unlockedBadgeIds: String = ""
)
