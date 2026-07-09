package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription

/**
 * Modal action sheet launched from the training calendar FAB.
 * This is not a route screen; keep running/strength launch choices here.
 * UI tests: TrainingCalendarUiTest.workoutActionBottomSheet_invokesRunningAndStrengthCallbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutActionBottomSheet(
    onDismiss: () -> Unit,
    onRunningClick: () -> Unit,
    onStrengthClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "운동 실행",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = onRunningClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .debugContentDescription(TestContentDescriptions.TrainingActionRunning),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.DirectionsRun, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("런닝")
            }
            Button(
                onClick = onStrengthClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .debugContentDescription(TestContentDescriptions.TrainingActionStrength),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("웨이트")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
