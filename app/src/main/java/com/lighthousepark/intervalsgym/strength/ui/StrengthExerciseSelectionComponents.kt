package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.UNILATERAL_MODE_OPTIONS
import com.lighthousepark.intervalsgym.strength.baseVariationOptions
import com.lighthousepark.intervalsgym.strength.combineVariationAndUnilateral
import com.lighthousepark.intervalsgym.strength.equipmentOptionsWithBodyweight
import com.lighthousepark.intervalsgym.strength.forcedUnilateralModeForVariation
import com.lighthousepark.intervalsgym.strength.matchesSearch
import com.lighthousepark.intervalsgym.strength.searchResultTitle
import com.lighthousepark.intervalsgym.strength.splitVariationAndUnilateral
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog

@Composable
internal fun StrengthExerciseListScreen(
    modifier: Modifier = Modifier,
    onAddCustomExercise: () -> Unit,
    onExerciseSelected: (StrengthExercise, String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val candidates = remember(searchQuery) {
        strengthExerciseCatalog
            .asSequence()
            .filter { exercise -> exercise.matchesSearch(searchQuery) }
            .toList()
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(shadowElevation = 3.dp) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .debugContentDescription(TestContentDescriptions.StrengthExerciseSearch),
                label = { Text("운동 검색") },
                singleLine = true
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "custom-exercise") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(TestContentDescriptions.StrengthCreateExercise)
                        .clickable(onClick = onAddCustomExercise),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text(
                            text = "운동 생성",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            items(candidates, key = { it.id }) { exercise ->
                ExerciseSearchRow(
                    exercise = exercise,
                    title = exercise.searchResultTitle(searchQuery),
                    selected = false,
                    onClick = { onExerciseSelected(exercise, searchQuery) }
                )
            }
        }
    }
}

@Composable
internal fun StrengthExercisePickerScreen(
    entry: StrengthRoutineEntry,
    onEntryChange: (StrengthRoutineEntry) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember(entry.id) { mutableStateOf("") }
    val variationParts = remember(entry.exercise.id, entry.variation) {
        splitVariationAndUnilateral(entry.exercise, entry.variation)
    }
    val forcedUnilateral = entry.exercise.forcedUnilateralModeForVariation(variationParts.first)
    val effectiveUnilateral = forcedUnilateral ?: variationParts.second
    val candidates = remember(searchQuery) {
        strengthExerciseCatalog
            .asSequence()
            .filter { exercise -> exercise.matchesSearch(searchQuery) }
            .take(12)
            .toList()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .debugContentDescription(TestContentDescriptions.StrengthExerciseSearch),
                label = { Text("운동 검색") },
                singleLine = true
            )
        }
        items(candidates, key = { it.id }) { exercise ->
            ExerciseSearchRow(
                exercise = exercise,
                title = exercise.searchResultTitle(searchQuery),
                selected = exercise.id == entry.exercise.id,
                onClick = {
                    onEntryChange(
                        entry.copy(
                            exercise = exercise,
                            equipment = exercise.equipmentOptions.first(),
                            variation = exercise.baseVariationOptions().first()
                        )
                    )
                }
            )
        }
        item {
            ChoiceGrid(
                title = "기구",
                options = entry.exercise.equipmentOptionsWithBodyweight(),
                selected = entry.equipment,
                onSelected = { onEntryChange(entry.copy(equipment = if (entry.equipment == it) "" else it)) }
            )
        }
        item {
            ChoiceGrid(
                title = "세부 타입",
                options = entry.exercise.baseVariationOptions(),
                selected = variationParts.first,
                onSelected = {
                    val nextUnilateral = entry.exercise.forcedUnilateralModeForVariation(it) ?: variationParts.second
                    onEntryChange(
                        entry.copy(variation = combineVariationAndUnilateral(it, nextUnilateral))
                    )
                }
            )
        }
        item {
            ChoiceGrid(
                title = "좌우 방식",
                options = UNILATERAL_MODE_OPTIONS,
                selected = effectiveUnilateral,
                onSelected = {
                    onEntryChange(
                        entry.copy(variation = combineVariationAndUnilateral(variationParts.first, it))
                    )
                },
                isOptionEnabled = { forcedUnilateral == null || it == forcedUnilateral }
            )
        }
        item {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("완료")
            }
        }
    }
}
