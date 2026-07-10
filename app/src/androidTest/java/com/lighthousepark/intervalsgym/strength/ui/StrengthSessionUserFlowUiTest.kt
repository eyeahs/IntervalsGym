package com.lighthousepark.intervalsgym.strength.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.overlay.RestOverlayRequests
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import com.lighthousepark.intervalsgym.strength.withRecords
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthSessionUserFlowUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var prefs: SharedPreferences
    private var storedPreferences: Map<String, *> = emptyMap<String, Any>()

    @Before
    fun clearStoredWorkoutState() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        storedPreferences = prefs.all.toMap()
        prefs.edit().clear().commit()
        instrumentation.uiAutomation
            .executeShellCommand(
                "appops set ${context.packageName} android:system_alert_window allow"
            )
            .close()
    }

    @After
    fun restoreStoredWorkoutState() {
        val editor = prefs.edit().clear()
        storedPreferences.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        editor.commit()
    }

    @Test
    fun threeExerciseSupersetMovesWithoutRestThenRestsAfterRound() {
        val routine = userFlowRoutine(setCounts = listOf(2, 2, 2), restSeconds = 90)
        var latestSession: ActiveStrengthSession? = null

        composeRule.setStrengthSessionContent(
            routine = routine,
            onSessionChange = { latestSession = it }
        )

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionBack)
            .performClick()
        composeRule.groupAsSuperset(routine.entries.map { it.id })
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(routine.entries[0].id))
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(
                TestContentDescriptions.strengthActualSetWeight(routine.entries[0].records[0].id)
            )
            .performTextReplacement("72.5")
        composeRule
            .onNodeWithContentDescription(
                TestContentDescriptions.strengthActualSetReps(routine.entries[0].records[0].id)
            )
            .performTextReplacement("6")

        composeRule.completeSetAndAssertExercise(routine.entries[1].title, setNumber = 1)
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestStop)
            .assertDoesNotExist()
        composeRule.completeSetAndAssertExercise(routine.entries[2].title, setNumber = 1)
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestStop)
            .assertDoesNotExist()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet)
            .performClick()
        composeRule.onNodeWithText("Set 2 · ${routine.entries[0].title}").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestStop)
            .performClick()
        composeRule.onNodeWithText("Set 2 · ${routine.entries[0].title}").assertExists()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            latestSession?.let { session ->
                session.setEvents.size == 3 &&
                    session.currentExerciseIndex == 0 &&
                    session.currentSetIndex == 1 &&
                    session.activeRestEventId == null
            } == true
        }

        composeRule.runOnIdle {
            val session = requireNotNull(latestSession)
            val groupIds = session.entries.map { it.supersetGroupId }
            assertNotNull(groupIds.first())
            assertTrue(groupIds.distinct().size == 1)
            assertEquals(listOf(1, 2, 3), session.setEvents.map { it.exerciseEntryId })
            assertEquals("72.5", session.setEvents.first().weightKg)
            assertEquals("6", session.setEvents.first().reps)
            assertEquals(routine.entries[0].records[0].weightKg, session.entries[0].records[0].weightKg)
            assertEquals("72.5", session.entries[0].records[0].actualWeightKg)
            assertEquals("stopped", session.restEvents.single().endReason)
        }
    }

    @Test
    fun ordinaryExerciseStartsRestBeforeMovingToNextExercise() {
        val routine = userFlowRoutine(setCounts = listOf(1, 1), restSeconds = 60)
        var latestSession: ActiveStrengthSession? = null

        composeRule.setStrengthSessionContent(
            routine = routine,
            onSessionChange = { latestSession = it }
        )

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestStop)
            .assertExists()
            .performClick()
        composeRule.onNodeWithText("Set 1 · ${routine.entries[1].title}").assertExists()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            latestSession?.let { session ->
                session.currentExerciseIndex == 1 &&
                    session.currentSetIndex == 0 &&
                    session.activeRestEventId == null
            } == true
        }

        composeRule.runOnIdle {
            val session = requireNotNull(latestSession)
            assertEquals(1, session.setEvents.size)
            assertEquals(routine.entries[0].id, session.setEvents.single().exerciseEntryId)
            assertEquals("stopped", session.restEvents.single().endReason)
        }
    }

    @Test
    fun finishAfterWorkoutEditsAppliesOnlyCheckedRoutineChanges() {
        val routine = userFlowRoutine(setCounts = listOf(1, 1, 1), restSeconds = 0)
        var finishedSession: CompletedStrengthSession? = null
        var shouldApplyToRoutine = false

        composeRule.setStrengthSessionContent(
            routine = routine,
            onSessionFinished = { session, applyToRoutine ->
                finishedSession = session
                shouldApplyToRoutine = applyToRoutine
            }
        )

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSetExecutionAddSet)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSetExecutionExercise)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailChangeExercise)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseSearch)
            .performTextInput("레그컬")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthExerciseSearchResult("leg_curl"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseConfigDone)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionBack)
            .performClick()
        composeRule.groupAsSuperset(listOf(routine.entries[0].id, routine.entries[2].id))

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishUpdateOrder)
            .assertIsOn()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishUpdateSupersets)
            .assertIsOn()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishUpdateExerciseTypes)
            .assertIsOn()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishUpdateExerciseDetails)
            .assertIsOn()
            .performClick()
            .assertIsOff()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishSave)
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) { finishedSession != null }

        composeRule.runOnIdle {
            val result = requireNotNull(finishedSession)
            val routineUpdate = requireNotNull(result.routineUpdateEntries)
            assertTrue(shouldApplyToRoutine)
            assertEquals(listOf(1, 3, 2), routineUpdate.map { it.id })
            assertEquals("leg_curl", routineUpdate.first { it.id == 1 }.exercise.id)
            assertEquals(
                routine.entries.first().records.size,
                routineUpdate.first { it.id == 1 }.records.size
            )
            assertEquals(2, result.entries.first { it.id == 1 }.records.size)
            assertEquals(
                routineUpdate.first { it.id == 1 }.supersetGroupId,
                routineUpdate.first { it.id == 3 }.supersetGroupId
            )
            assertNotNull(routineUpdate.first { it.id == 1 }.supersetGroupId)
            assertNull(routineUpdate.first { it.id == 2 }.supersetGroupId)
        }
    }

    @Test
    fun actualPerformanceOnlyDoesNotOfferRoutineDetailUpdate() {
        val routine = userFlowRoutine(setCounts = listOf(1), restSeconds = 0)
        var finishedSession: CompletedStrengthSession? = null
        var shouldApplyToRoutine = true

        composeRule.setStrengthSessionContent(
            routine = routine,
            onSessionFinished = { session, applyToRoutine ->
                finishedSession = session
                shouldApplyToRoutine = applyToRoutine
            }
        )

        val recordId = routine.entries.single().records.single().id
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetWeight(recordId))
            .performTextReplacement("100")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetReps(recordId))
            .performTextReplacement("3")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishUpdateExerciseDetails)
            .assertIsNotEnabled()
        composeRule.onNodeWithText("운동 중 변경된 routine 항목이 없습니다.").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishSave)
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) { finishedSession != null }

        composeRule.runOnIdle {
            val result = requireNotNull(finishedSession)
            assertFalse(shouldApplyToRoutine)
            assertNull(result.routineUpdateEntries)
            assertEquals("100", result.setEvents.single().weightKg)
            assertEquals("3", result.setEvents.single().reps)
        }
    }

    @Test
    fun restSheetRequestWhileForegroundShowsStableSheet() {
        val routine = userFlowRoutine(setCounts = listOf(1, 1), restSeconds = 60)
        val nowMillis = System.currentTimeMillis()
        val restingEntries = routine.entries.mapIndexed { index, entry ->
            if (index == 0) {
                entry.copy(records = entry.records.map { it.copy(completed = true) })
            } else {
                entry
            }
        }
        val restEvent = StrengthRestEvent(
            id = 1,
            afterSetSequence = 1,
            exerciseEntryId = restingEntries[0].id,
            exerciseTitle = restingEntries[0].title,
            setRecordId = restingEntries[0].records[0].id,
            setIndex = 0,
            startedAtMillis = nowMillis,
            plannedSeconds = 60,
            targetEndAtMillis = nowMillis + 60_000L,
            endedAtMillis = null,
            endReason = null
        )
        val activeSession = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = restingEntries,
            hasStarted = true,
            sessionStartedAtMillis = nowMillis - 60_000L,
            isSetScreenVisible = true,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = 1,
            pendingSetIndex = 0,
            restEndAtMillis = restEvent.targetEndAtMillis,
            isRestSheetVisible = false,
            restTitle = restEvent.exerciseTitle,
            setEvents = emptyList(),
            restEvents = listOf(restEvent),
            activeRestEventId = restEvent.id
        )
        var latestSession: ActiveStrengthSession? = null

        composeRule.setStrengthSessionContent(
            routine = routine,
            activeSession = activeSession,
            onSessionChange = { latestSession = it }
        )
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestStop)
            .assertDoesNotExist()

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { RestOverlayRequests.requestShowSheet() }

        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule
                .onAllNodesWithContentDescription(TestContentDescriptions.StrengthRestStop)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestStop)
            .assertExists()
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestStop)
            .assertExists()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            latestSession?.isRestSheetVisible == true
        }
    }
}

private fun ComposeContentTestRule.setStrengthSessionContent(
    routine: StrengthWorkoutRoutine,
    activeSession: ActiveStrengthSession? = null,
    onSessionChange: (ActiveStrengthSession?) -> Unit = {},
    onSessionFinished: (CompletedStrengthSession?, Boolean) -> Unit = { _, _ -> },
) {
    setContent {
        IntervalsGymTheme {
            StrengthSessionScreen(
                apiKey = "",
                routine = routine,
                calendarRoutineItem = null,
                isRoutineEditable = true,
                activeSession = activeSession,
                startImmediately = activeSession == null,
                onImmediateStartConsumed = {},
                onSessionChange = onSessionChange,
                onSessionFinished = onSessionFinished,
                onHistoryClick = {},
                onEditRoutine = {},
                onCalendarRoutineDeleted = {},
                onBack = {}
            )
        }
    }
}

private fun ComposeContentTestRule.groupAsSuperset(entryIds: List<Int>) {
    onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
        .performScrollTo()
        .performClick()
    entryIds.forEach { entryId ->
        onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(entryId))
            .performScrollTo()
            .performClick()
    }
    onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
        .performClick()
}

private fun ComposeContentTestRule.completeSetAndAssertExercise(
    exerciseTitle: String,
    setNumber: Int,
) {
    onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet).performClick()
    onNodeWithText("Set $setNumber · $exerciseTitle").assertExists()
}

private fun userFlowRoutine(
    setCounts: List<Int>,
    restSeconds: Int,
): StrengthWorkoutRoutine {
    val exerciseIds = listOf("squat", "bench_press", "row")
    val entries = setCounts.mapIndexed { index, setCount ->
        val exercise = strengthExerciseCatalog.first { it.id == exerciseIds[index] }
        val entry = defaultStrengthRoutineEntry(id = index + 1, exercise = exercise)
        entry.withRecords(
            entry.records.take(setCount).map { record ->
                record.copy(restSeconds = restSeconds.toString())
            }
        )
    }
    return defaultStrengthRoutines().first().copy(
        id = 901,
        name = "사용자 흐름 테스트",
        entries = entries
    )
}
