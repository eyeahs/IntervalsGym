package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.core.localizedContentDescription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.app.ROUTE_STRENGTH_MANAGE
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine

/**
 * Route owner for [ROUTE_STRENGTH_MANAGE].
 * Use this screen for routine management list actions; editing itself belongs to StrengthRoutineEditScreen.
 * UI tests: StrengthRoutineScreensUiTest.routineManagement_exposesAddAndEditActions,
 * routineManagement_emptyStateStillAllowsAddRoutine, routineManagement_backButtonInvokesBackCallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthRoutineManagementScreen(
    routines: List<StrengthWorkoutRoutine>,
    onAddRoutine: () -> Unit,
    onEditRoutine: (StrengthWorkoutRoutine) -> Unit,
    onCloneRoutine: (StrengthWorkoutRoutine) -> Unit = {},
    onBack: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRoutine,
                modifier = Modifier
                    .navigationBarsPadding()
                    .debugContentDescription(TestContentDescriptions.StrengthRoutineManagementAdd)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = localizedContentDescription("Routine 추가"))
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("웨이트 Routine 관리") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineManagementBack)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = localizedContentDescription("뒤로"))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (routines.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .debugContentDescription(TestContentDescriptions.StrengthRoutineManagementEmpty),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "저장된 웨이트 Routine이 없습니다.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(routines, key = { it.id }) { routine ->
                    StrengthRoutineRow(
                        routine = routine,
                        onClick = { onEditRoutine(routine) },
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.strengthRoutineManagementEdit(routine.id)),
                        trailing = {
                            Row {
                                IconButton(
                                    onClick = { onCloneRoutine(routine) },
                                    modifier = Modifier.debugContentDescription(
                                        TestContentDescriptions.strengthRoutineManagementClone(routine.id)
                                    )
                                ) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = localizedContentDescription("Routine 복제"))
                                }
                                IconButton(onClick = { onEditRoutine(routine) }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = localizedContentDescription("수정"))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
