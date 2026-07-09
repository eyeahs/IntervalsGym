package com.lighthousepark.intervalsgym.strength.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.core.formatWeight
import com.lighthousepark.intervalsgym.data.IntervalsUseCaseFactory
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.UNILATERAL_MODE_OPTIONS
import com.lighthousepark.intervalsgym.strength.baseVariationOptions
import com.lighthousepark.intervalsgym.strength.combineVariationAndUnilateral
import com.lighthousepark.intervalsgym.strength.equipmentOptionsWithBodyweight
import com.lighthousepark.intervalsgym.strength.forcedUnilateralModeForVariation
import com.lighthousepark.intervalsgym.strength.matchesSearch
import com.lighthousepark.intervalsgym.strength.searchResultTitle
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import com.lighthousepark.intervalsgym.strength.strengthTrainingLoad
import com.lighthousepark.intervalsgym.strength.totalDurationSeconds
import com.lighthousepark.intervalsgym.strength.totalVolumeKg
import com.lighthousepark.intervalsgym.workout.ui.EmptyView
import java.time.LocalDateTime
import kotlinx.coroutines.launch

/**
 * Legacy/manual strength workout surface kept for older entry points.
 * Prefer the routed StrengthSessionScreen overload in StrengthSessionScreen.kt for current workout flows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthManualSessionScreen(
    apiKey: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val intervalsUseCaseFactory = remember(apiKey) { IntervalsUseCaseFactory(apiKey) }
    val strengthSessionSync = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.strengthSessionSync(prefs)
    }
    var workoutName by remember { mutableStateOf("웨이트 트레이닝") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf(strengthExerciseCatalog.first()) }
    var selectedEquipment by remember { mutableStateOf(selectedExercise.equipmentOptions.first()) }
    var selectedVariation by remember { mutableStateOf(selectedExercise.baseVariationOptions().first()) }
    var selectedUnilateral by remember { mutableStateOf("양쪽") }
    var targetSets by remember { mutableStateOf("3") }
    var targetReps by remember { mutableStateOf("8") }
    var restSeconds by remember { mutableStateOf("120") }
    var targetWeight by remember { mutableStateOf("") }
    var nextRoutineId by remember { mutableIntStateOf(1) }
    var routineEntries by remember { mutableStateOf<List<StrengthRoutineEntry>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val forcedUnilateral = selectedExercise.forcedUnilateralModeForVariation(selectedVariation)
    val effectiveUnilateral = forcedUnilateral ?: selectedUnilateral

    val candidates = remember(searchQuery) {
        strengthExerciseCatalog
            .filter { exercise -> exercise.matchesSearch(searchQuery) }
            .take(12)
    }

    fun selectExercise(exercise: StrengthExercise) {
        selectedExercise = exercise
        selectedEquipment = exercise.equipmentOptions.first()
        selectedVariation = exercise.baseVariationOptions().first()
        selectedUnilateral = exercise.forcedUnilateralModeForVariation(selectedVariation) ?: "양쪽"
    }

    fun updateEntry(entry: StrengthRoutineEntry) {
        routineEntries = routineEntries.map { if (it.id == entry.id) entry else it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("웨이트 Routine & 기록") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = workoutName,
                            onValueChange = { workoutName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Workout 이름") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("운동 검색") },
                            singleLine = true
                        )
                        Text(
                            text = "운동 선택",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        candidates.forEach { exercise ->
                            ExerciseSearchRow(
                                exercise = exercise,
                                title = exercise.searchResultTitle(searchQuery),
                                selected = exercise.id == selectedExercise.id,
                                onClick = { selectExercise(exercise) }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = selectedExercise.nameKo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedExercise.nameEn} · ${selectedExercise.group}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ChoiceGrid(
                            title = "기구",
                            options = selectedExercise.equipmentOptionsWithBodyweight(),
                            selected = selectedEquipment,
                            onSelected = { selectedEquipment = if (selectedEquipment == it) "" else it }
                        )
                        ChoiceGrid(
                            title = "세부 타입",
                            options = selectedExercise.baseVariationOptions(),
                            selected = selectedVariation,
                            onSelected = {
                                selectedVariation = it
                                selectedUnilateral = selectedExercise.forcedUnilateralModeForVariation(it) ?: selectedUnilateral
                            }
                        )
                        ChoiceGrid(
                            title = "좌우 방식",
                            options = UNILATERAL_MODE_OPTIONS,
                            selected = effectiveUnilateral,
                            onSelected = { if (forcedUnilateral == null) selectedUnilateral = it },
                            isOptionEnabled = { forcedUnilateral == null || it == forcedUnilateral }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NumberField(
                                value = targetSets,
                                onValueChange = { targetSets = it },
                                label = "세트",
                                modifier = Modifier.weight(1f)
                            )
                            NumberField(
                                value = targetReps,
                                onValueChange = { targetReps = it },
                                label = "횟수",
                                modifier = Modifier.weight(1f)
                            )
                            NumberField(
                                value = restSeconds,
                                onValueChange = { restSeconds = it },
                                label = "휴식초",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        NumberField(
                            value = targetWeight,
                            onValueChange = { targetWeight = it },
                            label = "목표 무게 kg",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val sets = targetSets.toIntOrNull()?.coerceIn(1, 20) ?: 1
                                val reps = targetReps.toIntOrNull()?.coerceAtLeast(0) ?: 0
                                val rest = restSeconds.toIntOrNull()?.coerceAtLeast(0) ?: 0
                                val records = List(sets) { index ->
                                    StrengthSetRecord(
                                        id = index + 1,
                                        weightKg = targetWeight,
                                        reps = reps.takeIf { it > 0 }?.toString().orEmpty(),
                                        durationSeconds = "",
                                        restSeconds = rest.toString(),
                                        completed = false
                                    )
                                }
                                routineEntries = routineEntries + StrengthRoutineEntry(
                                    id = nextRoutineId,
                                    exercise = selectedExercise,
                                    equipment = selectedEquipment,
                                    variation = combineVariationAndUnilateral(selectedVariation, effectiveUnilateral),
                                    supersetGroupId = null,
                                    targetSets = sets,
                                    targetReps = reps,
                                    restSeconds = rest,
                                    targetWeightKg = targetWeight,
                                    records = records
                                )
                                nextRoutineId += 1
                                uploadMessage = null
                                uploadError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Routine에 추가")
                        }
                    }
                }
            }

            if (routineEntries.isEmpty()) {
                item {
                    EmptyView(message = "운동을 선택하고 Routine에 추가하세요.")
                }
            } else {
                items(routineEntries, key = { it.id }) { entry ->
                    StrengthRoutineEntryCard(
                        entry = entry,
                        onEntryChange = ::updateEntry,
                        onDelete = {
                            routineEntries = routineEntries.filterNot { it.id == entry.id }
                        }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val completedSets = routineEntries.sumOf { entry -> entry.records.count { it.completed } }
                        val totalSets = routineEntries.sumOf { it.records.size }
                        val volume = routineEntries.totalVolumeKg()
                        Text(
                            text = "업로드 준비",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$completedSets/$totalSets 세트 완료 · 볼륨 ${formatWeight(volume)} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        uploadMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }
                        uploadError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = {
                                if (apiKey.isBlank()) {
                                    uploadMessage = null
                                    uploadError = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다."
                                    return@Button
                                }
                                scope.launch {
                                    isUploading = true
                                    uploadMessage = null
                                    uploadError = null
                                    try {
                                        strengthSessionSync.uploadStrengthSession(
                                            StrengthSession(
                                                name = workoutName.ifBlank { "웨이트 트레이닝" },
                                                startedAt = LocalDateTime.now().minusSeconds(
                                                    routineEntries.totalDurationSeconds().toLong()
                                                ),
                                                entries = routineEntries,
                                                rpe = 7,
                                                trainingLoad = routineEntries.strengthTrainingLoad(7)
                                            )
                                        )
                                        uploadMessage = "Intervals.icu에 업로드했습니다."
                                    } catch (error: Exception) {
                                        uploadError = error.message ?: "업로드하지 못했습니다."
                                    } finally {
                                        isUploading = false
                                    }
                                }
                            },
                            enabled = routineEntries.isNotEmpty() && !isUploading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isUploading) "업로드 중" else "Intervals.icu 업데이트")
                        }
                    }
                }
            }
        }
    }
}
