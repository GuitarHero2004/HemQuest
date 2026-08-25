package com.example.model

import androidx.compose.ui.graphics.Color

enum class GlossaryCategory(
    val tag: String,
    val titleVi: String,
    val titleEn: String,
    val titleZh: String,
    val titleJa: String,
    val titleKo: String,
    val icon: String
) {
    ALL("ALL", "Tất cả", "All Terms", "全部词条", "すべて", "전체", "📚"),
    GEOGRAPHY("GEOGRAPHY", "Địa Lý & Số Nhà Hẻm", "Alleys & Addresses", "深巷地理与门牌", "路地地理と番地", "골목 지리 및 주소", "🛵"),
    CULINARY("CULINARY", "Ẩm Thực & Thức Uống", "Street Food & Drinks", "深巷美食与饮品", "路地裏グルメ・珈琲", "골목 미식과 음료", "☕"),
    HERITAGE("HERITAGE", "Lịch Sử & Bí Mật Di Sản", "Heritage & History", "历史遗迹与秘密", "歴史遺産と秘密基地", "역사 유적 및 비밀", "🏛️"),
    COMMUNITY("COMMUNITY", "Lối Sống & Làng Nghề", "Community & Crafts", "社区生活与传统工艺", "暮らしと職人街", "공동체와 전통 공예", "🏮"),
    ARCHITECTURE("ARCHITECTURE", "Kiến Trúc & Chung Cư", "Architecture & Tenements", "老建筑与古旧公寓", "レトロ建築と集合住宅", "레트로 건축과 옛 아파트", "🏢"),
    STREET_LIFE("STREET_LIFE", "Âm Thanh & Tiếng Rao", "Sounds & Street Life", "街头声音与深夜叫卖", "街角の音と夜鳴き声", "거리의 소리와 야간 외침", "📣")
}

data class CulturalGlossaryItem(
    val id: String,
    val term: String,
    val phonetic: String,
    val toneGuide: String = "",
    val category: GlossaryCategory,
    val icon: String,
    val accentColor: Color = Color(0xFF00B14F),
    val shortDefinitionVi: String,
    val shortDefinitionEn: String,
    val shortDefinitionZh: String,
    val shortDefinitionJa: String,
    val shortDefinitionKo: String,
    val fullDescriptionVi: String,
    val fullDescriptionEn: String,
    val fullDescriptionZh: String,
    val fullDescriptionJa: String,
    val fullDescriptionKo: String,
    val whyItMattersVi: String,
    val whyItMattersEn: String,
    val whyItMattersZh: String,
    val whyItMattersJa: String,
    val whyItMattersKo: String,
    val triviaVi: String = "",
    val triviaEn: String = "",
    val exampleLocationsVi: List<String>,
    val exampleLocationsEn: List<String>
)

