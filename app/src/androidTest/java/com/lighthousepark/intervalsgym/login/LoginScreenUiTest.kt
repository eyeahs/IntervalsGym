package com.lighthousepark.intervalsgym.login

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginScreen_invokesOAuthAndSkipCallbacksWhenConfigured() {
        var oauthClicked = false
        var skipClicked = false

        composeRule.setThemedContent {
            LoginScreen(
                onOAuthLogin = { oauthClicked = true },
                onSkipLogin = { skipClicked = true },
                isOAuthConfigured = true,
                isOAuthConnecting = false
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.LoginOAuth)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.LoginSkip)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(oauthClicked)
            assertTrue(skipClicked)
        }
    }

    @Test
    fun loginScreen_disablesOAuthWhileConnectingButKeepsSkipAvailable() {
        var skipClicked = false

        composeRule.setThemedContent {
            LoginScreen(
                onOAuthLogin = {},
                onSkipLogin = { skipClicked = true },
                isOAuthConfigured = true,
                isOAuthConnecting = true
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.LoginOAuth)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.LoginSkip)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(skipClicked)
        }
    }

    @Test
    fun loginScreen_rendersSelectedEnglishLanguage() {
        composeRule.setThemedContent(languageTag = "en") {
            LoginScreen(
                onOAuthLogin = {},
                onSkipLogin = {},
                isOAuthConfigured = true,
                isOAuthConnecting = false
            )
        }

        composeRule.onNodeWithText("Intervals Weekly Training").assertExists()
        composeRule.onNodeWithText("Sign in with Intervals OAuth").assertExists()
        composeRule.onNodeWithText("Continue without signing in").assertExists()
        composeRule.onNodeWithText("로그인 없이 사용").assertDoesNotExist()
    }

    @Test
    fun loginScreen_rendersSelectedJapaneseLanguage() {
        composeRule.setThemedContent(languageTag = "ja") {
            LoginScreen(
                onOAuthLogin = {},
                onSkipLogin = {},
                isOAuthConfigured = true,
                isOAuthConnecting = false
            )
        }

        composeRule.onNodeWithText("Intervals 週間トレーニング").assertExists()
        composeRule.onNodeWithText("Intervals OAuthでログイン").assertExists()
        composeRule.onNodeWithText("ログインせずに使用").assertExists()
        composeRule.onNodeWithText("로그인 없이 사용").assertDoesNotExist()
    }

    @Test
    fun loginScreen_rendersSelectedSimplifiedChineseLanguage() {
        composeRule.setThemedContent(languageTag = "zh-CN") {
            LoginScreen(
                onOAuthLogin = {},
                onSkipLogin = {},
                isOAuthConfigured = true,
                isOAuthConnecting = false
            )
        }

        composeRule.onNodeWithText("Intervals 每周训练").assertExists()
        composeRule.onNodeWithText("使用 Intervals OAuth 登录").assertExists()
        composeRule.onNodeWithText("不登录直接使用").assertExists()
        composeRule.onNodeWithText("로그인 없이 사용").assertDoesNotExist()
    }

    @Test
    fun loginScreen_rendersSelectedGermanLanguage() {
        assertLocalizedLogin(
            languageTag = "de",
            title = "Intervals-Wochentraining",
            login = "Mit Intervals OAuth anmelden",
            skip = "Ohne Anmeldung fortfahren"
        )
    }

    @Test
    fun loginScreen_rendersSelectedFrenchLanguage() {
        assertLocalizedLogin(
            languageTag = "fr",
            title = "Entraînement hebdomadaire Intervals",
            login = "Se connecter avec Intervals OAuth",
            skip = "Continuer sans se connecter"
        )
    }

    @Test
    fun loginScreen_rendersSelectedItalianLanguage() {
        assertLocalizedLogin(
            languageTag = "it",
            title = "Allenamento settimanale Intervals",
            login = "Accedi con Intervals OAuth",
            skip = "Continua senza accedere"
        )
    }

    @Test
    fun loginScreen_rendersSelectedSpanishLanguage() {
        assertLocalizedLogin(
            languageTag = "es",
            title = "Entrenamiento semanal de Intervals",
            login = "Iniciar sesión con Intervals OAuth",
            skip = "Continuar sin iniciar sesión"
        )
    }

    @Test
    fun loginScreen_rendersSelectedPortugueseLanguage() {
        assertLocalizedLogin(
            languageTag = "pt",
            title = "Treino semanal do Intervals",
            login = "Iniciar sessão com Intervals OAuth",
            skip = "Continuar sem iniciar sessão"
        )
    }

    private fun assertLocalizedLogin(
        languageTag: String,
        title: String,
        login: String,
        skip: String,
    ) {
        composeRule.setThemedContent(languageTag = languageTag) {
            LoginScreen(
                onOAuthLogin = {},
                onSkipLogin = {},
                isOAuthConfigured = true,
                isOAuthConnecting = false
            )
        }

        composeRule.onNodeWithText(title).assertExists()
        composeRule.onNodeWithText(login).assertExists()
        composeRule.onNodeWithText(skip).assertExists()
        composeRule.onNodeWithText("로그인 없이 사용").assertDoesNotExist()
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    languageTag: String? = null,
    content: @Composable () -> Unit,
) {
    val configuration = Configuration(
        InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration
    )
    languageTag?.let {
        configuration.setLocales(LocaleList.forLanguageTags(it))
    }
    setContent {
        CompositionLocalProvider(LocalConfiguration provides configuration) {
            IntervalsGymTheme(content = content)
        }
    }
}
