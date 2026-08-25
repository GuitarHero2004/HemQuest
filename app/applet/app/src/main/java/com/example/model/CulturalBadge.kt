package com.example.model

import com.google.firebase.firestore.PropertyName

data class CulturalBadge(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("iconEmoji") @set:PropertyName("iconEmoji")
    var iconEmoji: String = "🎖️",

    @get:PropertyName("category") @set:PropertyName("category")
    var category: String = "CULTURE",

    @get:PropertyName("questId") @set:PropertyName("questId")
    var questId: String = "",

    @get:PropertyName("questTitle") @set:PropertyName("questTitle")
    var questTitle: String = "",

    @get:PropertyName("unlockedAt") @set:PropertyName("unlockedAt")
    var unlockedAt: Long = System.currentTimeMillis(),

    @get:PropertyName("rarity") @set:PropertyName("rarity")
    var rarity: String = "RARE", // COMMON, RARE, EPIC, LEGENDARY

    @get:PropertyName("culturalPointsEarned") @set:PropertyName("culturalPointsEarned")
    var culturalPointsEarned: Int = 50
)
