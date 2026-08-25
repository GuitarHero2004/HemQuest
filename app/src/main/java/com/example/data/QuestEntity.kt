package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey
    val id: String,
    val questJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
