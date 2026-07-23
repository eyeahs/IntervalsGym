package com.lighthousepark.intervalsgym.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppTextLocalizationTest {
    @Test
    fun languageTag_selectsSupportedLanguageAndFallsBackToKorean() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTag("en-US"))
        assertEquals(AppLanguage.JAPANESE, AppLanguage.fromLanguageTag("ja-JP"))
        assertEquals(AppLanguage.CHINESE_SIMPLIFIED, AppLanguage.fromLanguageTag("zh-CN"))
        assertEquals(AppLanguage.GERMAN, AppLanguage.fromLanguageTag("de-DE"))
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromLanguageTag("fr-FR"))
        assertEquals(AppLanguage.ITALIAN, AppLanguage.fromLanguageTag("it-IT"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromLanguageTag("es-ES"))
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromLanguageTag("pt-BR"))
        assertEquals(AppLanguage.KOREAN, AppLanguage.fromLanguageTag("ko-KR"))
        assertEquals(AppLanguage.KOREAN, AppLanguage.fromLanguageTag("nl-NL"))
    }

    @Test
    fun exactUiLabels_areLocalizedForEverySupportedLanguage() {
        assertEquals("Start workout", localizeAppText("운동 시작", AppLanguage.ENGLISH))
        assertEquals("ワークアウト開始", localizeAppText("운동 시작", AppLanguage.JAPANESE))
        assertEquals("开始训练", localizeAppText("운동 시작", AppLanguage.CHINESE_SIMPLIFIED))
        assertEquals("Training starten", localizeAppText("운동 시작", AppLanguage.GERMAN))
        assertEquals("Démarrer l’entraînement", localizeAppText("운동 시작", AppLanguage.FRENCH))
        assertEquals("Avvia allenamento", localizeAppText("운동 시작", AppLanguage.ITALIAN))
        assertEquals("Iniciar entrenamiento", localizeAppText("운동 시작", AppLanguage.SPANISH))
        assertEquals("Iniciar treino", localizeAppText("운동 시작", AppLanguage.PORTUGUESE))
    }

    @Test
    fun exerciseAndMetricSummary_localizesComposedRuntimeText() {
        val source = "덤벨 불가리안 스플릿 스쿼트 · 3세트 · 볼륨 120 kg · 실제 휴식 1분"

        listOf(
            AppLanguage.ENGLISH,
            AppLanguage.JAPANESE,
            AppLanguage.CHINESE_SIMPLIFIED,
            AppLanguage.GERMAN,
            AppLanguage.FRENCH,
            AppLanguage.ITALIAN,
            AppLanguage.SPANISH,
            AppLanguage.PORTUGUESE
        ).forEach { language ->
            val localized = localizeAppText(source, language)
            assertFalse(
                "Hangul remained in $language translation: $localized",
                HANGUL.containsMatchIn(localized)
            )
        }
    }

    @Test
    fun calendarDateAndWeekday_followSelectedLanguage() {
        assertEquals(
            "7/23 Thu",
            localizeAppText("7월 23일 목", AppLanguage.ENGLISH)
        )
        assertEquals(
            "2026年7月",
            localizeAppText("2026년 7월", AppLanguage.JAPANESE)
        )
        assertEquals(
            "2026年7月",
            localizeAppText("2026년 7월", AppLanguage.CHINESE_SIMPLIFIED)
        )
        assertEquals(
            "23.7. Do.",
            localizeAppText("7월 23일 목", AppLanguage.GERMAN)
        )
        assertEquals(
            "23/7 jeu.",
            localizeAppText("7월 23일 목", AppLanguage.FRENCH)
        )
        assertEquals(
            "23/7 gio",
            localizeAppText("7월 23일 목", AppLanguage.ITALIAN)
        )
        assertEquals(
            "23/7 jue",
            localizeAppText("7월 23일 목", AppLanguage.SPANISH)
        )
        assertEquals(
            "23/7 qui",
            localizeAppText("7월 23일 목", AppLanguage.PORTUGUESE)
        )
    }

    private companion object {
        val HANGUL = Regex("[가-힣]")
    }
}
