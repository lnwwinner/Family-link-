package com.example.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {
    fun setLocale(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getSupportedLanguages(): Map<String, String> {
        return mapOf(
            "th" to "ไทย",
            "en" to "English",
            "zh" to "中文",
            "ja" to "日本語"
        )
    }
}
