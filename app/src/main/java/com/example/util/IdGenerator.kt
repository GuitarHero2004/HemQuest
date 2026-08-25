package com.example.util

import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

object IdGenerator {

    /**
     * Converts raw text (e.g. "Cà Phê Vợt & Hẻm Di Sản Sài Gòn") into a clean, distinguishable slug:
     * "ca_phe_vot_hem_di_san_sai_gon"
     */
    fun toSlug(input: String, maxLength: Int = 32): String {
        if (input.isBlank()) return "quest"
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        val withoutAccents = pattern.matcher(nfdNormalizedString)
            .replaceAll("")
            .replace("đ", "d")
            .replace("Đ", "d")
            .lowercase(Locale.ROOT)

        val slug = withoutAccents
            .replace("[^a-z0-9]+".toRegex(), "_")
            .trim('_')

        val clean = if (slug.isBlank()) "item" else slug
        return if (clean.length > maxLength) clean.take(maxLength).trimEnd('_') else clean
    }

    /**
     * Generates a readable, unique, distinguishable Quest ID for Firestore & Room:
     * e.g. "quest_phuong_sai_gon_ca_phe_vot_1724501234"
     */
    fun generateQuestId(
        locationName: String? = null,
        title: String? = null,
        category: String? = null
    ): String {
        val locSlug = if (!locationName.isNullOrBlank()) toSlug(locationName, 18) else null
        val titleSlug = if (!title.isNullOrBlank()) toSlug(title, 22) else null
        val catSlug = if (!category.isNullOrBlank()) toSlug(category, 14) else null

        val baseParts = listOfNotNull(locSlug, titleSlug ?: catSlug).filter { it.isNotBlank() }
        val descriptor = if (baseParts.isNotEmpty()) baseParts.joinToString("_") else "custom_route"

        val timestampShort = (System.currentTimeMillis() / 1000).toString()
        return "quest_${descriptor}_$timestampShort"
    }

    /**
     * Generates a readable, distinguishable Stop ID:
     * e.g. "stop_01_chua_ba_thien_hau"
     */
    fun generateStopId(index: Int, stopName: String): String {
        val nameSlug = toSlug(stopName, 24)
        val padIdx = String.format(Locale.US, "%02d", index + 1)
        return "stop_${padIdx}_$nameSlug"
    }

    /**
     * Generates a readable, distinguishable badge ID from a completed quest title:
     * e.g. "quest_badge_ca_phe_vot_sai_gon"
     */
    fun generateQuestBadgeId(questTitle: String): String {
        val titleSlug = toSlug(questTitle, 30)
        return "quest_badge_$titleSlug"
    }

    /**
     * Returns a unique, representative icon Emoji for a cultural walking quest
     */
    fun getBadgeIconForQuest(questTitle: String, category: String? = null): String {
        val lower = questTitle.lowercase(Locale.ROOT)
        return when {
            lower.contains("ca phe") || lower.contains("cà phê") || lower.contains("coffee") || lower.contains("che") || lower.contains("chè") -> "☕"
            lower.contains("long den") || lower.contains("lồng đèn") || lower.contains("phu binh") || lower.contains("chua") || lower.contains("chùa") || lower.contains("hoi quan") || lower.contains("hội quán") -> "🏮"
            lower.contains("biet dong") || lower.contains("biệt động") || lower.contains("ham") || lower.contains("hầm") || lower.contains("do phu") || lower.contains("đỗ phủ") || lower.contains("lich su") || lower.contains("lịch sử") -> "🎖️"
            lower.contains("am thuc") || lower.contains("ẩm thực") || lower.contains("hu tieu") || lower.contains("hủ tiếu") || lower.contains("banh mi") || lower.contains("bánh mì") || lower.contains("food") -> "🍜"
            lower.contains("kien truc") || lower.contains("kiến trúc") || lower.contains("art deco") || lower.contains("di san") || lower.contains("di sản") || lower.contains("dinh") || lower.contains("đình") -> "🏛️"
            lower.contains("thuoc bac") || lower.contains("thuốc bắc") || lower.contains("lang nghe") || lower.contains("làng nghề") || lower.contains("gom") || lower.contains("gốm") -> "🏺"
            lower.contains("xanh") || lower.contains("green") || lower.contains("eco") || lower.contains("cay") || lower.contains("cây") -> "🌿"
            category.equals("FOOD", ignoreCase = true) -> "🍜"
            category.equals("HERITAGE", ignoreCase = true) || category.equals("HISTORY", ignoreCase = true) -> "🏛️"
            category.equals("ART", ignoreCase = true) -> "🎨"
            category.equals("NATURE", ignoreCase = true) -> "🌿"
            else -> "🧭"
        }
    }

    /**
     * Deduce rarity based on quest difficulty or steps count
     */
    fun getBadgeRarityForQuest(stopsCount: Int, distanceMeters: Int): String {
        return when {
            stopsCount >= 5 || distanceMeters >= 2000 -> "LEGENDARY"
            stopsCount >= 3 || distanceMeters >= 1200 -> "EPIC"
            else -> "RARE"
        }
    }
}
