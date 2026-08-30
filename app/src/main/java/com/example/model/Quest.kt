package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class StopStatus {
    UPCOMING,
    CURRENT,
    COMPLETED,
    SKIPPED
}

enum class VerificationStatus {
    LIKELY_MATCH,
    UNCERTAIN,
    NOT_ENOUGH_INFORMATION,
    REJECTED
}

@JsonClass(generateAdapter = true)
data class GreenFactor(
    @Json(name = "label") val label: String,
    @Json(name = "explanation") val explanation: String
)

@JsonClass(generateAdapter = true)
data class GreenScore(
    @Json(name = "score") val score: Int,
    @Json(name = "factors") val factors: List<GreenFactor> = emptyList()
)

enum class QuestDifficulty(val label: String, val emoji: String) {
    EASY("Easy", "🟢"),
    MODERATE("Moderate", "🟡"),
    CHALLENGING("Challenging", "🔴");

    fun localizedName(language: String): String {
        return when (this) {
            EASY -> when (language) {
                "vi" -> "Dễ"
                "zh" -> "轻松"
                "ja" -> "初級"
                "ko" -> "쉬움"
                else -> "Easy"
            }
            MODERATE -> when (language) {
                "vi" -> "Vừa phải"
                "zh" -> "中等"
                "ja" -> "中級"
                "ko" -> "보통"
                else -> "Moderate"
            }
            CHALLENGING -> when (language) {
                "vi" -> "Thử thách"
                "zh" -> "进阶"
                "ja" -> "上級"
                "ko" -> "도전"
                else -> "Challenging"
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class Challenge(
    @Json(name = "type") val type: String = "PHOTO_OR_SKIP",
    @Json(name = "prompt") val prompt: String,
    @Json(name = "successGuidance") val successGuidance: String? = null
)

@JsonClass(generateAdapter = true)
data class QuestStop(
    @Json(name = "id") val id: String,
    @Json(name = "placeId") val placeId: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String = "Cultural Landmark",
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "whySelected") val whySelected: String,
    @Json(name = "story") val story: String,
    @Json(name = "factReference") val factReference: String = "Verified Saigon Heritage Inventory",
    @Json(name = "challenge") val challenge: Challenge,
    @Json(name = "photos") val photos: List<String> = emptyList(),
    val status: StopStatus = StopStatus.UPCOMING,
    val photoUri: String? = null,
    val verificationResult: PhotoVerificationResult? = null
)

@JsonClass(generateAdapter = true)
data class Quest(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "theme") val theme: String,
    @Json(name = "summary") val summary: String,
    @Json(name = "estimatedMinutes") val estimatedMinutes: Int,
    @Json(name = "estimatedDistanceMetres") val estimatedDistanceMetres: Int,
    @Json(name = "greenScore") val greenScore: GreenScore,
    @Json(name = "stops") val stops: List<QuestStop>,
    @Json(name = "difficulty") val difficulty: String? = null
) {
    val difficultyLevel: QuestDifficulty
        get() {
            if (!difficulty.isNullOrBlank()) {
                when (difficulty.trim().uppercase()) {
                    "EASY", "DỄ", "轻松", "初級", "쉬움" -> return QuestDifficulty.EASY
                    "CHALLENGING", "HARD", "THỬ THÁCH", "KHÓ", "进阶", "困难", "上級", "도전", "어려움" -> return QuestDifficulty.CHALLENGING
                    "MODERATE", "MEDIUM", "VỪA PHẢI", "TRUNG BÌNH", "中等", "中級", "보통" -> return QuestDifficulty.MODERATE
                }
            }
            return when {
                estimatedDistanceMetres >= 2000 || estimatedMinutes >= 55 || stops.size >= 5 -> QuestDifficulty.CHALLENGING
                estimatedDistanceMetres <= 1300 && estimatedMinutes <= 35 && stops.size <= 3 -> QuestDifficulty.EASY
                else -> QuestDifficulty.MODERATE
            }
        }
}

@JsonClass(generateAdapter = true)
data class PhotoVerificationResult(
    @Json(name = "status") val status: VerificationStatus = VerificationStatus.LIKELY_MATCH,
    @Json(name = "observation") val observation: String = "",
    @Json(name = "detailNotes") val detailNotes: String = ""
)

data class QuestRequest(
    val startingLocationName: String = "Phường Sài Gòn, TP. Hồ Chí Minh",
    val latitude: Double = 10.7764,
    val longitude: Double = 106.7011,
    val durationMinutes: Int = 60,
    val interests: List<String> = listOf("Architecture", "Hidden History", "Local Food"),
    val travelMode: String = "WALK",
    val freeTextNotes: String = "",
    val language: String = "en"
)

data class CulturalBadge(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var iconEmoji: String = "🎖️",
    var category: String = "CULTURE",
    var questId: String = "",
    var questTitle: String = "",
    var unlockedAt: Long = System.currentTimeMillis(),
    var rarity: String = "RARE",
    var culturalPointsEarned: Int = 50
)
