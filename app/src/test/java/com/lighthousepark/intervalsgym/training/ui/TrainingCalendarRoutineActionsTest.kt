package com.lighthousepark.intervalsgym.training.ui

import com.lighthousepark.intervalsgym.data.CalendarRoutineDeleteScope
import com.lighthousepark.intervalsgym.data.CalendarRoutineSyncUseCase
import com.lighthousepark.intervalsgym.data.MemorySharedPreferences
import com.lighthousepark.intervalsgym.data.RecordingCalendarRoutineRemoteDataSource
import com.lighthousepark.intervalsgym.data.intervalsRoutineExternalId
import com.lighthousepark.intervalsgym.data.loadScheduledStrengthRoutines
import com.lighthousepark.intervalsgym.data.scheduledStrengthRoutineId
import com.lighthousepark.intervalsgym.data.upsertScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.training.PendingCalendarRoutineMove
import com.lighthousepark.intervalsgym.training.calendarIdentityKeys
import com.lighthousepark.intervalsgym.training.trainingItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarRoutineActionsTest {
    @Test
    fun savePlanBlocksInvalidTimeAndExecutesLocalSaveAndUpload() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingCalendarRoutineRemoteDataSource()
        val syncUseCase = CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )
        val routine = defaultStrengthRoutines().first().copy(id = 301, name = "저녁 웨이트")
        val targetDate = LocalDate.of(2026, 7, 8)
        val targetTime = LocalTime.of(19, 30)

        assertEquals(
            TrainingCalendarRoutineSaveDecision.InvalidTime,
            planTrainingCalendarRoutineSave(
                routine = routine,
                targetDate = targetDate,
                targetTime = null,
                isRemoteConnected = true
            )
        )

        val plan = requireSavePlan(
            planTrainingCalendarRoutineSave(
                routine = routine,
                targetDate = targetDate,
                targetTime = targetTime,
                isRemoteConnected = true
            )
        )
        val localRoutine = plan.saveLocally(syncUseCase)
        val uploadedRoutine = plan.upload(syncUseCase, localRoutine)

        assertTrue(plan.requiresRemoteUpload)
        assertEquals(301, plan.routineId)
        assertFalse(localRoutine.uploadedToIntervals)
        assertTrue(uploadedRoutine.uploadedToIntervals)
        assertTrue(loadScheduledStrengthRoutines(prefs).single().uploadedToIntervals)
        assertEquals(listOf(com.lighthousepark.intervalsgym.data.RecordedStrengthUpload(routine, targetDate, targetTime)), remote.strengthUploads)
    }

    @Test
    fun movePlanIgnoresNonRoutineAndSameDate() {
        val date = LocalDate.of(2026, 7, 8)
        val nonRoutine = trainingItem(isRoutine = false, date = date)
        val routine = trainingItem(isRoutine = true, date = date)

        assertEquals(
            TrainingCalendarRoutineMoveDecision.Ignore,
            planTrainingCalendarRoutineMove(
                item = nonRoutine,
                targetDate = date.plusDays(1),
                pendingCalendarRoutineMoves = emptyMap(),
                isRemoteConnected = true
            )
        )
        assertEquals(
            TrainingCalendarRoutineMoveDecision.Ignore,
            planTrainingCalendarRoutineMove(
                item = routine,
                targetDate = date,
                pendingCalendarRoutineMoves = emptyMap(),
                isRemoteConnected = true
            )
        )
    }

    @Test
    fun movePlanBlocksWhenMatchingMoveIsAlreadyPending() {
        val sourceDate = LocalDate.of(2026, 7, 8)
        val targetDate = sourceDate.plusDays(1)
        val routine = trainingItem(
            id = "routine-source",
            remoteId = "remote-source",
            externalId = "external-source",
            isRoutine = true,
            date = sourceDate
        )
        val pendingMove = PendingCalendarRoutineMove(routine, targetDate)

        val decision = planTrainingCalendarRoutineMove(
            item = routine,
            targetDate = targetDate.plusDays(1),
            pendingCalendarRoutineMoves = mapOf(pendingMove.key to pendingMove),
            isRemoteConnected = true
        )

        assertEquals(
            TrainingCalendarRoutineMoveDecision.Blocked(TRAINING_CALENDAR_PENDING_MOVE_MESSAGE),
            decision
        )
    }

    @Test
    fun movePlanStoresPendingMoveOnlyWhenRemoteConnectedAndBuildsFeedbackMessages() {
        val sourceDate = LocalDate.of(2026, 7, 8)
        val targetDate = sourceDate.plusDays(1)
        val routine = trainingItem(
            id = "routine-source",
            name = "퇴근 후",
            isRoutine = true,
            date = sourceDate
        )

        val disconnected = requireMovePlan(
            planTrainingCalendarRoutineMove(
                item = routine,
                targetDate = targetDate,
                pendingCalendarRoutineMoves = emptyMap(),
                isRemoteConnected = false
            )
        )
        val connected = requireMovePlan(
            planTrainingCalendarRoutineMove(
                item = routine,
                targetDate = targetDate,
                pendingCalendarRoutineMoves = emptyMap(),
                isRemoteConnected = true
            )
        )

        assertTrue(disconnected.pendingCalendarRoutineMoves.isEmpty())
        assertTrue(connected.pendingCalendarRoutineMoves.containsKey(connected.pendingMove.key))
        assertEquals("퇴근 후 7/9로 이동됨", connected.startedMessage(movedLocally = true))
        assertEquals("퇴근 후 7/9로 이동 중...", connected.startedMessage(movedLocally = false))
        assertEquals("로컬 일정은 이동됐지만 Intervals.icu 반영은 실패했습니다.", connected.failureMessage(movedLocally = true))
        assertEquals("Intervals.icu Routine 이동에 실패했습니다.", connected.failureMessage(movedLocally = false))
        assertTrue(connected.rollbackPendingMove(connected.pendingCalendarRoutineMoves).isEmpty())
    }

    @Test
    fun movePlanExecutesLocalMoveAndRemoteSync() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingCalendarRoutineRemoteDataSource()
        val syncUseCase = CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )
        val sourceDate = LocalDate.of(2026, 7, 8)
        val targetDate = sourceDate.plusDays(1)
        val targetTime = LocalTime.of(19, 30)
        val routine = defaultStrengthRoutines().first().copy(id = 302, name = "이동 루틴")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(sourceDate, targetTime),
            date = sourceDate,
            time = targetTime,
            routine = routine,
            uploadedToIntervals = true,
            externalId = routine.intervalsRoutineExternalId(sourceDate, targetTime)
        )
        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        val sourceItem = trainingItem(
            id = "routine-source",
            remoteId = "remote-source",
            externalId = scheduledRoutine.externalId,
            name = routine.name,
            type = "Weight Training",
            isRoutine = true,
            date = sourceDate,
            startedAt = LocalDateTime.of(sourceDate, targetTime),
            matchedStrengthRoutine = routine
        )
        val plan = requireMovePlan(
            planTrainingCalendarRoutineMove(
                item = sourceItem,
                targetDate = targetDate,
                pendingCalendarRoutineMoves = emptyMap(),
                isRemoteConnected = true
            )
        )

        val movedRoutine = plan.moveLocally(syncUseCase)
        plan.syncRemote(syncUseCase, movedRoutine)

        requireNotNull(movedRoutine)
        assertEquals(targetDate, loadScheduledStrengthRoutines(prefs).single().date)
        assertEquals(listOf(com.lighthousepark.intervalsgym.data.RecordedStrengthUpload(routine, targetDate, targetTime)), remote.strengthUploads)
        assertEquals(listOf("remote-source"), remote.deletedEventIds)
    }

    @Test
    fun deletePlanUpdatesPendingAndOptimisticStateForRemoteDelete() {
        val date = LocalDate.of(2026, 7, 8)
        val routine = trainingItem(
            id = "routine-source",
            remoteId = "remote-source",
            externalId = "external-source",
            name = "아침 루틴",
            isRoutine = true,
            date = date
        )
        val pendingMove = PendingCalendarRoutineMove(routine, date.plusDays(1))

        val plan = requireDeletePlan(
            planTrainingCalendarRoutineDelete(
                item = routine,
                pendingCalendarRoutineMoves = mapOf("unrelated" to PendingCalendarRoutineMove(
                    trainingItem(id = "other", remoteId = "other", isRoutine = true, date = date),
                    date.plusDays(1)
                )),
                optimisticallyDeletedCalendarRoutineKeys = setOf("already-hidden"),
                deleteScopeFor = { CalendarRoutineDeleteScope.REMOTE }
            )
        )
        val blocked = planTrainingCalendarRoutineDelete(
            item = routine,
            pendingCalendarRoutineMoves = mapOf(pendingMove.key to pendingMove),
            optimisticallyDeletedCalendarRoutineKeys = emptySet(),
            deleteScopeFor = { CalendarRoutineDeleteScope.REMOTE }
        )

        assertTrue(plan.requiresRemoteDelete)
        assertTrue(plan.deleteKeys.containsAll(routine.calendarIdentityKeys()))
        assertTrue(plan.optimisticallyDeletedCalendarRoutineKeys.contains("already-hidden"))
        assertTrue(plan.optimisticallyDeletedCalendarRoutineKeys.containsAll(routine.calendarIdentityKeys()))
        assertFalse(plan.pendingCalendarRoutineMoves.values.any { move -> move.sourceRoutine.id == routine.id })
        assertEquals("아침 루틴 삭제됨", plan.deletedMessage())
        assertEquals(
            setOf("already-hidden"),
            plan.clearOptimisticDeleteKeys(plan.optimisticallyDeletedCalendarRoutineKeys)
        )
        assertEquals(
            TrainingCalendarRoutineDeleteDecision.Blocked(TRAINING_CALENDAR_PENDING_MOVE_MESSAGE),
            blocked
        )
    }

    @Test
    fun deletePlanExecutesDeleteThroughSyncUseCase() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingCalendarRoutineRemoteDataSource()
        val syncUseCase = CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )
        val date = LocalDate.of(2026, 7, 8)
        val routine = defaultStrengthRoutines().first().copy(id = 303, name = "삭제 루틴")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(date),
            date = date,
            routine = routine,
            uploadedToIntervals = true,
            externalId = routine.intervalsRoutineExternalId(date)
        )
        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        val targetItem = trainingItem(
            id = "routine-delete",
            remoteId = "remote-delete",
            externalId = scheduledRoutine.externalId,
            type = "Weight Training",
            isRoutine = true,
            date = date,
            matchedStrengthRoutine = routine
        )
        val plan = requireDeletePlan(
            planTrainingCalendarRoutineDelete(
                item = targetItem,
                pendingCalendarRoutineMoves = emptyMap(),
                optimisticallyDeletedCalendarRoutineKeys = emptySet(),
                deleteScopeFor = { CalendarRoutineDeleteScope.REMOTE }
            )
        )

        val deleteScope = plan.delete(syncUseCase)

        assertEquals(CalendarRoutineDeleteScope.REMOTE, deleteScope)
        assertTrue(loadScheduledStrengthRoutines(prefs).isEmpty())
        assertEquals(listOf("remote-delete"), remote.deletedEventIds)
    }

    @Test
    fun deletePlanDoesNotOptimisticallyHideLocalDelete() {
        val routine = trainingItem(isRoutine = true, date = LocalDate.of(2026, 7, 8))

        val plan = requireDeletePlan(
            planTrainingCalendarRoutineDelete(
                item = routine,
                pendingCalendarRoutineMoves = emptyMap(),
                optimisticallyDeletedCalendarRoutineKeys = emptySet(),
                deleteScopeFor = { CalendarRoutineDeleteScope.LOCAL }
            )
        )

        assertFalse(plan.requiresRemoteDelete)
        assertTrue(plan.optimisticallyDeletedCalendarRoutineKeys.isEmpty())
    }

    private fun requireSavePlan(
        decision: TrainingCalendarRoutineSaveDecision,
    ): TrainingCalendarRoutineSavePlan {
        return (decision as TrainingCalendarRoutineSaveDecision.Save).plan
    }

    private fun requireMovePlan(
        decision: TrainingCalendarRoutineMoveDecision,
    ): TrainingCalendarRoutineMovePlan {
        return (decision as TrainingCalendarRoutineMoveDecision.Move).plan
    }

    private fun requireDeletePlan(
        decision: TrainingCalendarRoutineDeleteDecision,
    ): TrainingCalendarRoutineDeletePlan {
        return (decision as TrainingCalendarRoutineDeleteDecision.Delete).plan
    }
}
