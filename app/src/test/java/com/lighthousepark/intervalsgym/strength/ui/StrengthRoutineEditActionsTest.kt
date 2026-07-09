package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StrengthRoutineEditActionsTest {
    @Test
    fun editableStrengthRoutineTrimsNameFiltersPendingDeleteAndNormalizesSupersets() {
        val routine = defaultStrengthRoutines().first()
        val entries = routine.entries.take(2).map { it.copy(supersetGroupId = 7) }

        val editable = editableStrengthRoutine(
            routine = routine,
            routineName = "  새 루틴  ",
            entries = entries,
            pendingDeleteEntryIds = setOf(entries.last().id)
        )

        assertEquals(routine.id, editable.id)
        assertEquals("새 루틴", editable.name)
        assertEquals(listOf(entries.first().id), editable.entries.map { it.id })
        assertNull(editable.entries.single().supersetGroupId)
    }

    @Test
    fun originalEditSnapshotTrimsAndNormalizesRoutine() {
        val routine = defaultStrengthRoutines().first().copy(
            name = "  원본  ",
            entries = defaultStrengthRoutines().first().entries.take(1).map { it.copy(supersetGroupId = 9) }
        )

        val snapshot = originalStrengthRoutineEditSnapshot(routine)

        assertEquals("원본", snapshot.name)
        assertNull(snapshot.entries.single().supersetGroupId)
    }

    @Test
    fun selectedEntriesGroupedAsSupersetUsesOneSharedRuleForEditAndSessionLists() {
        val entries = defaultStrengthRoutines().first().entries

        val grouped = entries.withSelectedEntriesGroupedAsSuperset(selectedEntryIds = setOf(1, 3))

        assertEquals(listOf(1, 3, 2), grouped.map { it.id })
        assertEquals(listOf(1, 1), grouped.take(2).map { it.supersetGroupId })
        assertNull(grouped.last().supersetGroupId)
    }

    @Test
    fun selectedSupersetGroupsClearedClearsWholeSelectedGroupsOnly() {
        val catalog = strengthExerciseCatalog
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = catalog[0]).copy(supersetGroupId = 4),
            defaultStrengthRoutineEntry(id = 2, exercise = catalog[1]).copy(supersetGroupId = 4),
            defaultStrengthRoutineEntry(id = 3, exercise = catalog[2]).copy(supersetGroupId = 8),
            defaultStrengthRoutineEntry(id = 4, exercise = catalog[3]).copy(supersetGroupId = 8)
        )

        val cleared = entries.withSelectedSupersetGroupsCleared(selectedEntryIds = setOf(1))

        assertEquals(listOf(null, null, 8, 8), cleared.map { it.supersetGroupId })
    }

    @Test
    fun withoutRoutineEntryNormalizesRemainingSupersets() {
        val entries = defaultStrengthRoutines().first().entries.take(2).map { it.copy(supersetGroupId = 5) }

        val remaining = entries.withoutRoutineEntry(entries.first().id)

        assertEquals(listOf(entries.last().id), remaining.map { it.id })
        assertNull(remaining.single().supersetGroupId)
    }

    @Test
    fun entryDeleteStateRequestsRestoresAndCommitsPendingDeletesTogether() {
        val entries = defaultStrengthRoutines().first().entries.take(2).map { it.copy(supersetGroupId = 9) }
        val initial = StrengthRoutineEntryDeleteState(
            entries = entries,
            pendingDeleteEntryIds = emptySet(),
            selectedSupersetEntryIds = setOf(entries.first().id, entries.last().id),
            selectedEntryId = entries.first().id
        )

        val requested = initial.withDeleteRequested(entries.first().id)
        val restored = requested.withDeleteRestored(entries.first().id)
        val committed = requested.withDeleteCommitted(entries.first().id)

        assertEquals(setOf(entries.first().id), requested.pendingDeleteEntryIds)
        assertEquals(setOf(entries.last().id), requested.selectedSupersetEntryIds)
        assertEquals(emptySet<Int>(), restored.pendingDeleteEntryIds)
        assertEquals(listOf(entries.last().id), committed.entries.map { it.id })
        assertNull(committed.entries.single().supersetGroupId)
        assertNull(committed.selectedEntryId)
        assertEquals(emptySet<Int>(), committed.pendingDeleteEntryIds)
    }

    @Test
    fun addedStrengthRoutineEntryUsesNextIdAndRequestedEquipmentDefaults() {
        val entries = defaultStrengthRoutines().first().entries
        val pushUp = strengthExerciseCatalog.first { it.id == "push_up" }

        val added = addedStrengthRoutineEntry(
            entries = entries,
            completedStrengthHistory = emptyList(),
            exercise = pushUp,
            equipment = "맨몸",
            variation = pushUp.variationOptions.first()
        )

        assertEquals((entries.maxOf { it.id }) + 1, added.id)
        assertEquals("맨몸", added.equipment)
        assertEquals("", added.targetWeightKg)
        assertEquals(listOf("", "", ""), added.records.map { it.weightKg })
    }
}
