package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.core.localizedContentDescription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.app.ROUTE_STRENGTH_ROUTINES
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine

/**
 * Route owner for [ROUTE_STRENGTH_ROUTINES].
 * This is the only weight routine picker/launcher screen; add selection or quick-start behavior here.
 * UI tests: StrengthRoutineScreensUiTest.routineList_exposesSelectStartAndManageActions,
 * routineList_emptyStateStillAllowsManagement, routineList_backButtonInvokesBackCallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthRoutineListScreen(
    routines: List<StrengthWorkoutRoutine>,
    onRoutineSelected: (StrengthWorkoutRoutine) -> Unit,
    onStartRoutine: (StrengthWorkoutRoutine) -> Unit,
    onManageRoutines: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("웨이트 routine 선택") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineListBack)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = localizedContentDescription("뒤로"))
                    }
                },
                actions = {
                    IconButton(
                        onClick = onManageRoutines,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineListManage)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = localizedContentDescription("Routine 관리"))
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
                            .debugContentDescription(TestContentDescriptions.StrengthRoutineListEmpty),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "수행할 웨이트 Routine이 없습니다. 우측 상단 관리에서 Routine을 추가하세요.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(routines, key = { it.id }) { routine ->
                    StrengthRoutineRow(
                        routine = routine,
                        onClick = { onRoutineSelected(routine) },
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.strengthRoutineListRow(routine.id)),
                        trailing = {
                            IconButton(
                                onClick = { onStartRoutine(routine) },
                                modifier = Modifier.debugContentDescription(TestContentDescriptions.strengthRoutineListStart(routine.id))
                            ) {
                                Icon(Icons.Outlined.PlayArrow, contentDescription = localizedContentDescription("바로 운동 시작"))
                            }
                        }
                    )
                }
            }
        }
    }
}
