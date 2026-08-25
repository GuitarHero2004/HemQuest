package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuestCategory(
    val id: String,
    val defaultTitleEn: String,
    val defaultTitleVi: String,
    val interestTag: String,
    val iconName: String
) {
    ALL(
        id = "all",
        defaultTitleEn = "All",
        defaultTitleVi = "Tất cả",
        interestTag = "All",
        iconName = "Category"
    ),
    STREET_FOOD(
        id = "street_food",
        defaultTitleEn = "Street Food",
        defaultTitleVi = "Ẩm thực đường phố",
        interestTag = "Street Food",
        iconName = "Restaurant"
    ),
    HISTORY(
        id = "history",
        defaultTitleEn = "History",
        defaultTitleVi = "Lịch sử & Di tích",
        interestTag = "History",
        iconName = "History"
    ),
    HIDDEN_ALLEYS(
        id = "hidden_alleys",
        defaultTitleEn = "Hidden Alleys",
        defaultTitleVi = "Hẻm giấu & Tranh tường",
        interestTag = "Hidden Alleys",
        iconName = "Explore"
    ),
    LOCAL_CRAFTS(
        id = "local_crafts",
        defaultTitleEn = "Local Crafts",
        defaultTitleVi = "Làng nghề & Thủ công",
        interestTag = "Local Crafts",
        iconName = "Brush"
    ),
    ARCHITECTURE(
        id = "architecture",
        defaultTitleEn = "Architecture",
        defaultTitleVi = "Kiến trúc cổ",
        interestTag = "Architecture",
        iconName = "Architecture"
    );

    fun getDisplayName(language: String): String {
        return when (language) {
            "vi" -> defaultTitleVi
            "zh" -> when (this) {
                ALL -> "全部"
                STREET_FOOD -> "街头美食"
                HISTORY -> "历史古迹"
                HIDDEN_ALLEYS -> "隐秘胡同"
                LOCAL_CRAFTS -> "传统手艺"
                ARCHITECTURE -> "地标建筑"
            }
            "ja" -> when (this) {
                ALL -> "すべて"
                STREET_FOOD -> "屋台グルメ"
                HISTORY -> "歴史・文化"
                HIDDEN_ALLEYS -> "隠れ路地"
                LOCAL_CRAFTS -> "伝統工芸"
                ARCHITECTURE -> "建築美"
            }
            "ko" -> when (this) {
                ALL -> "전체"
                STREET_FOOD -> "길거리 음식"
                HISTORY -> "역사"
                HIDDEN_ALLEYS -> "숨은 골목"
                LOCAL_CRAFTS -> "전통 공예"
                ARCHITECTURE -> "건축"
            }
            else -> defaultTitleEn
        }
    }

    fun getIcon(): ImageVector {
        return when (this) {
            ALL -> Icons.Default.Category
            STREET_FOOD -> Icons.Default.Restaurant
            HISTORY -> Icons.Default.History
            HIDDEN_ALLEYS -> Icons.Default.Explore
            LOCAL_CRAFTS -> Icons.Default.Brush
            ARCHITECTURE -> Icons.Default.Architecture
        }
    }
}
