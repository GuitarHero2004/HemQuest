package com.example.util

fun l(
    lang: String,
    vi: String,
    en: String,
    zh: String,
    ja: String,
    ko: String
): String {
    return when (lang) {
        "vi" -> vi
        "zh" -> zh
        "ja" -> ja
        "ko" -> ko
        else -> en
    }
}
