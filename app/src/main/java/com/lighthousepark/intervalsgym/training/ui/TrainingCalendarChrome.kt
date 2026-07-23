package com.lighthousepark.intervalsgym.training.ui

import com.lighthousepark.intervalsgym.core.localizedContentDescription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.AppLanguage
import com.lighthousepark.intervalsgym.core.currentApplicationLanguage
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.setApplicationLanguage
import com.lighthousepark.intervalsgym.training.TrainingCalendarMode
import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.dateLabel
import com.lighthousepark.intervalsgym.training.toEpochMillis
import com.lighthousepark.intervalsgym.training.toLocalDateFromMillis
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrainingCalendarTopBar(
    calendarMode: TrainingCalendarMode,
    selectedRange: TrainingDateRange,
    today: LocalDate,
    showBackButton: Boolean,
    showCalendarModeButton: Boolean,
    showSettingsMenu: Boolean,
    isIntervalsOAuthConnecting: Boolean,
    apiKey: String,
    intervalsOAuthConnectedLabel: String?,
    isIntervalsOAuthConfigured: Boolean,
    onTitleClick: () -> Unit,
    onTodayClick: () -> Unit,
    onCalendarModeClick: () -> Unit,
    onSettingsMenuExpandedChange: (Boolean) -> Unit,
    onRefreshClick: () -> Unit,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onTitleClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(calendarMode.title)
                Text(
                    text = calendarMode.dateLabel(selectedRange),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            if (today < selectedRange.start || today > selectedRange.end) {
                IconButton(onClick = onTodayClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_today_word),
                        contentDescription = localizedContentDescription("오늘로 이동"),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (showCalendarModeButton) {
                IconButton(
                    onClick = onCalendarModeClick,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.TrainingCalendarMode)
                ) {
                    CalendarModeIcon(
                        mode = calendarMode,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Box {
                IconButton(
                    onClick = { onSettingsMenuExpandedChange(true) },
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.TrainingCalendarSettings)
                ) {
                    Icon(imageVector = Icons.Outlined.Settings, contentDescription = localizedContentDescription("설정"))
                }
                DropdownMenu(
                    expanded = showSettingsMenu,
                    onDismissRequest = { onSettingsMenuExpandedChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("새로고침") },
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.TrainingCalendarRefresh),
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                        },
                        onClick = {
                            onSettingsMenuExpandedChange(false)
                            onRefreshClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("언어") },
                        modifier = Modifier.debugContentDescription(
                            TestContentDescriptions.TrainingCalendarLanguage
                        ),
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Language, contentDescription = null)
                        },
                        onClick = {
                            onSettingsMenuExpandedChange(false)
                            showLanguageDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                when {
                                    isIntervalsOAuthConnecting -> "Intervals 로그인 중"
                                    apiKey.isNotBlank() && intervalsOAuthConnectedLabel != null ->
                                        "Intervals 로그아웃 · $intervalsOAuthConnectedLabel"
                                    apiKey.isNotBlank() -> "Intervals 로그아웃"
                                    isIntervalsOAuthConfigured -> "Intervals 로그인"
                                    else -> "Intervals OAuth 설정 없음"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (apiKey.isBlank()) {
                                    Icons.Outlined.CloudUpload
                                } else {
                                    Icons.AutoMirrored.Outlined.Logout
                                },
                                contentDescription = null
                            )
                        },
                        enabled = !isIntervalsOAuthConnecting &&
                            (apiKey.isNotBlank() || isIntervalsOAuthConfigured),
                        modifier = Modifier.debugContentDescription(
                            TestContentDescriptions.TrainingCalendarIntervalsAuth
                        ),
                        onClick = {
                            onSettingsMenuExpandedChange(false)
                            if (apiKey.isBlank()) {
                                onLoginClick()
                            } else {
                                onLogout()
                            }
                        }
                    )
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.TrainingCalendarBack)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = localizedContentDescription("뒤로")
                    )
                }
            }
        }
    )

    if (showLanguageDialog) {
        AppLanguageDialog(
            selectedLanguage = context.currentApplicationLanguage(),
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { language ->
                showLanguageDialog = false
                context.setApplicationLanguage(language)
            }
        )
    }
}

@Composable
internal fun AppLanguageDialog(
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("앱 언어") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onLanguageSelected(language) }
                            .debugContentDescription(
                                TestContentDescriptions.appLanguageOption(language.languageTag)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == selectedLanguage,
                            onClick = { onLanguageSelected(language) }
                        )
                        Text(language.selfName)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrainingCalendarDatePickerDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val confirmedDate = datePickerState.selectedDateMillis?.toLocalDateFromMillis()
                    if (confirmedDate == null) {
                        onDismiss()
                    } else {
                        onDateSelected(confirmedDate)
                    }
                }
            ) {
                Text("이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
