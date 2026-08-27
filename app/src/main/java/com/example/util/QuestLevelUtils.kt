package com.example.util

data class LevelInfo(
    val level: Int,
    val titleVi: String,
    val titleEn: String,
    val iconEmoji: String,
    val currentLevelXp: Int,
    val requiredLevelXp: Int,
    val progressFraction: Float,
    val totalXp: Int,
    val perkVi: String,
    val perkEn: String
)

object QuestLevelUtils {
    // Level thresholds for levels 1 to 5+
    private val LEVEL_THRESHOLDS = listOf(
        0,    // Level 1: 0 - 249 XP
        250,  // Level 2: 250 - 599 XP
        600,  // Level 3: 600 - 1099 XP
        1100, // Level 4: 1100 - 1799 XP
        1800  // Level 5: 1800+ XP
    )

    fun calculateLevelInfo(totalXp: Int, lang: String = "vi"): LevelInfo {
        var level = 1
        for (i in LEVEL_THRESHOLDS.indices) {
            if (totalXp >= LEVEL_THRESHOLDS[i]) {
                level = i + 1
            } else {
                break
            }
        }

        val minXpForLevel = if (level <= LEVEL_THRESHOLDS.size) {
            LEVEL_THRESHOLDS[level - 1]
        } else {
            LEVEL_THRESHOLDS.last() + (level - LEVEL_THRESHOLDS.size) * 1000
        }

        val maxXpForNextLevel = if (level < LEVEL_THRESHOLDS.size) {
            LEVEL_THRESHOLDS[level]
        } else {
            minXpForLevel + 1000
        }

        val currentLevelXp = (totalXp - minXpForLevel).coerceAtLeast(0)
        val requiredLevelXp = (maxXpForNextLevel - minXpForLevel).coerceAtLeast(100)
        val progressFraction = (currentLevelXp.toFloat() / requiredLevelXp.toFloat()).coerceIn(0.04f, 1.0f)

        val (titleVi, titleEn, iconEmoji, perkVi, perkEn) = when (level) {
            1 -> LevelData(
                "Tập Sự Hẻm Sài Gòn",
                "Alley Apprentice",
                "🔰",
                "Mở khóa bản đồ di sản hẻm cơ bản & nhận huy hiệu bước đầu",
                "Unlock basic alley map & first-step badges"
            )
            2 -> LevelData(
                "Người Khám Phá Hẻm",
                "Alley Explorer",
                "⚡",
                "Tùy chỉnh hành trình theo sở thích & theo dõi chỉ số Sinh Thái",
                "Custom route tweaks & Green Eco score tracking"
            )
            3 -> LevelData(
                "Sứ Giả Văn Hóa Hẻm",
                "Alley Cultural Envoy",
                "🏮",
                "Ghi danh bảng vàng văn hóa & mở khóa tem di sản độc quyền",
                "Cultural honor roll & exclusive heritage stamp seals"
            )
            4 -> LevelData(
                "Bậc Thầy Di Sản Hẻm",
                "Alley Heritage Master",
                "🏛️",
                "Khung đại diện Vàng & ưu tiên tạo đề xuất điểm dừng văn hóa",
                "Golden avatar border & priority stop suggestions"
            )
            else -> LevelData(
                "Tôn Giả Hẻm Sài Gòn",
                "Saigon Alley Legend",
                "👑",
                "Danh hiệu huyền thoại & huy hiệu tôn vinh vĩnh viễn trên Bảng Xếp Hạng",
                "Legendary title & permanent hall of fame status on Leaderboards"
            )
        }

        return LevelInfo(
            level = level,
            titleVi = titleVi,
            titleEn = titleEn,
            iconEmoji = iconEmoji,
            currentLevelXp = currentLevelXp,
            requiredLevelXp = requiredLevelXp,
            progressFraction = progressFraction,
            totalXp = totalXp,
            perkVi = perkVi,
            perkEn = perkEn
        )
    }

    private data class LevelData(
        val titleVi: String,
        val titleEn: String,
        val iconEmoji: String,
        val perkVi: String,
        val perkEn: String
    )

    fun getAllLevels(): List<LevelInfo> {
        return listOf(0, 250, 600, 1100, 1800).map { xp ->
            calculateLevelInfo(xp)
        }
    }
}
