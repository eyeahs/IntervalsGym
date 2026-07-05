package com.lighthousepark.intervalsgym.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme

/**
 * Route owner for the Intervals login entry screen.
 * This screen only starts Intervals OAuth; legacy API key input is intentionally absent.
 * UI tests: LoginScreenUiTest.loginScreen_invokesOAuthAndSkipCallbacksWhenConfigured,
 * LoginScreenUiTest.loginScreen_disablesOAuthWhileConnectingButKeepsSkipAvailable.
 */
@Composable
internal fun LoginScreen(
    onOAuthLogin: () -> Unit,
    onSkipLogin: () -> Unit,
    isOAuthConfigured: Boolean,
    isOAuthConnecting: Boolean,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Intervals 주간 훈련",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intervals.icu 계정으로 로그인하면 훈련 계획과 결과를 동기화합니다.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onOAuthLogin,
                enabled = isOAuthConfigured && !isOAuthConnecting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .debugContentDescription(TestContentDescriptions.LoginOAuth),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (isOAuthConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(imageVector = Icons.Outlined.CloudUpload, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        isOAuthConnecting -> "Intervals 로그인 중"
                        isOAuthConfigured -> "Intervals OAuth 로그인"
                        else -> "Intervals OAuth 설정 없음"
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onSkipLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .debugContentDescription(TestContentDescriptions.LoginSkip),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("로그인 없이 사용")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    IntervalsGymTheme {
        LoginScreen(
            onOAuthLogin = {},
            onSkipLogin = {},
            isOAuthConfigured = true,
            isOAuthConnecting = false
        )
    }
}
