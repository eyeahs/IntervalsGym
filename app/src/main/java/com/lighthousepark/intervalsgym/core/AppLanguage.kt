package com.lighthousepark.intervalsgym.core

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import java.util.Locale

internal enum class AppLanguage(
    val languageTag: String?,
    val selfName: String,
) {
    SYSTEM(null, "시스템 기본"),
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    CHINESE_SIMPLIFIED("zh-CN", "简体中文"),
    GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"),
    ITALIAN("it", "Italiano"),
    SPANISH("es", "Español"),
    PORTUGUESE("pt", "Português");

    companion object {
        fun fromLanguageTag(languageTag: String?): AppLanguage {
            val locale = languageTag
                ?.takeIf { it.isNotBlank() }
                ?.let(Locale::forLanguageTag)
                ?: Locale.getDefault()
            return when (locale.language) {
                Locale.ENGLISH.language -> ENGLISH
                Locale.JAPANESE.language -> JAPANESE
                Locale.CHINESE.language -> CHINESE_SIMPLIFIED
                Locale.GERMAN.language -> GERMAN
                Locale.FRENCH.language -> FRENCH
                Locale.ITALIAN.language -> ITALIAN
                "es" -> SPANISH
                "pt" -> PORTUGUESE
                else -> KOREAN
            }
        }
    }
}

internal fun Context.currentApplicationLanguage(): AppLanguage {
    val locales = getSystemService(LocaleManager::class.java).applicationLocales
    return if (locales.isEmpty) {
        AppLanguage.SYSTEM
    } else {
        AppLanguage.fromLanguageTag(locales[0]?.toLanguageTag())
    }
}

internal fun Context.setApplicationLanguage(language: AppLanguage) {
    getSystemService(LocaleManager::class.java).applicationLocales = language.languageTag
        ?.let(LocaleList::forLanguageTags)
        ?: LocaleList.getEmptyLocaleList()
}

internal fun Context.localizedAppText(text: String): String {
    val languageTag = resources.configuration.locales[0]?.toLanguageTag()
    return localizeAppText(text, AppLanguage.fromLanguageTag(languageTag))
}
