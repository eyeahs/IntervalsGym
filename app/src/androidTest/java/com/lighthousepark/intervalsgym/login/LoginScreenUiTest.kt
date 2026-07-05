package com.lighthousepark.intervalsgym.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}
