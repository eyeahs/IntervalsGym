package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarComponentsArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot

    @Test
    fun trainingCalendarListComponentsStayOutOfMainCalendarScreen() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val pagerContent = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarPagerContent.kt")
        )
        val listComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarListComponents.kt")
        )
        val renderComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarListRenderComponents.kt")
        )
        val dragGeometry = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarListDragGeometry.kt")
        )
        val headerState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarListHeaderState.kt")
        )
        val headerConnection = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarListHeaderConnection.kt")
        )
        val itemDragState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarListItemDragState.kt")
        )
        val daySection = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarListDaySection.kt")
        )
        val movedDefinitions = listOf(
            "internal fun TrainingList"
        )
        val daySectionDefinitions = listOf(
            "internal fun TrainingCalendarDaySection",
            "private fun TrainingCalendarDayItemRow"
        )
        val renderDefinitions = listOf(
            "internal fun TrainingCalendarScrollableDayList",
            "internal fun TrainingCalendarFloatingHeader",
            "internal fun BoxScope.TrainingCalendarLocalDragOverlayHost"
        )
        val geometryDefinitions = listOf(
            "internal data class CalendarRoutineDragTarget",
            "internal fun calendarRoutineDragTargetAt",
            "internal fun calendarRoutineDragActionAt",
            "internal fun calendarRoutineDropDateAt",
            "internal fun calendarRoutineDragWeekShiftDirection",
            "internal fun calendarRoutineAutoScrollDelta"
        )
        val headerStateDefinitions = listOf(
            "internal data class TrainingCalendarHeaderScrollResult",
            "internal fun coerceTrainingCalendarHeaderOffset",
            "internal fun trainingCalendarHeaderOffsetAfterScroll",
            "internal fun trainingCalendarHeaderFlingTargetOffset",
            "internal fun trainingCalendarHeaderOffsetAfterListScrollabilityChanged"
        )
        val headerConnectionDefinitions = listOf(
            "internal fun rememberTrainingCalendarHeaderScrollConnection"
        )
        val itemDragStateDefinitions = listOf(
            "internal data class TrainingCalendarItemDragState",
            "internal fun TrainingItem.trainingCalendarItemDragState",
            "internal fun trainingCalendarItemRowAlpha"
        )
        val movedGestureState = listOf(
            "rememberLazyListState(",
            "awaitLongPressOrCancellation(",
            "listState.scrollBy("
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarListComponents.kt", calendarScreen.contains(definition))
            assertTrue("$definition missing from TrainingCalendarListComponents.kt", listComponents.contains(definition))
        }
        daySectionDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarListDaySection.kt", listComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarListDaySection.kt", daySection.contains(definition))
        }
        renderDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarListRenderComponents.kt", calendarScreen.contains(definition))
            assertFalse("$definition belongs in TrainingCalendarListRenderComponents.kt", listComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarListRenderComponents.kt", renderComponents.contains(definition))
        }
        geometryDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarScreen.kt", calendarScreen.contains(definition))
            assertFalse("$definition belongs in TrainingCalendarListComponents.kt", listComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarListDragGeometry.kt", dragGeometry.contains(definition))
        }
        headerStateDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarListHeaderState.kt", listComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarListHeaderState.kt", headerState.contains(definition))
        }
        headerConnectionDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarListHeaderConnection.kt", listComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarListHeaderConnection.kt", headerConnection.contains(definition))
        }
        itemDragStateDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarListItemDragState.kt", listComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarListItemDragState.kt", itemDragState.contains(definition))
        }
        movedGestureState.forEach { call ->
            assertFalse("$call belongs in TrainingCalendarListComponents.kt", calendarScreen.contains(call))
            assertTrue("$call missing from TrainingCalendarListComponents.kt", listComponents.contains(call))
        }
        assertFalse(listComponents.contains("fun dragTargetAt("))
        assertFalse(listComponents.contains("fun dragActionAt("))
        assertFalse(listComponents.contains("fun dropDateAt("))
        assertTrue(listComponents.contains("calendarRoutineDragTargetAt("))
        assertTrue(listComponents.contains("calendarRoutineDragActionAt("))
        assertTrue(listComponents.contains("calendarRoutineDropDateAt("))
        assertTrue(listComponents.contains("rememberTrainingCalendarHeaderScrollConnection("))
        assertTrue(headerConnection.contains("trainingCalendarHeaderOffsetAfterScroll("))
        assertTrue(headerConnection.contains("trainingCalendarHeaderFlingTargetOffset("))
        assertTrue(listComponents.contains("trainingCalendarHeaderOffsetAfterListScrollabilityChanged("))
        assertTrue(listComponents.contains("TrainingCalendarScrollableDayList("))
        assertTrue(listComponents.contains("TrainingCalendarFloatingHeader("))
        assertTrue(listComponents.contains("TrainingCalendarLocalDragOverlayHost("))
        assertTrue(renderComponents.contains("TrainingCalendarDaySection("))
        assertTrue(daySection.contains("trainingCalendarItemDragState("))
        assertFalse(listComponents.contains("(previousOffset + delta).coerceIn"))
        assertFalse(listComponents.contains("available.y < 0f && !listState.canScrollForward"))
        assertFalse(listComponents.contains("calendarRoutineForMove() ?: item"))
        assertFalse(listComponents.contains("item.canDragCalendarRoutine("))
        assertFalse(listComponents.contains("item.isApiPendingMove("))
        assertFalse(listComponents.contains("isDragging -> 0.2f"))
        assertFalse(listComponents.contains("TrainingItemRow("))
        assertFalse(listComponents.contains("DayHeader("))
        assertFalse(listComponents.contains("LazyColumn("))
        assertFalse(listComponents.contains("AnimatedVisibility("))
        assertFalse(listComponents.contains("CalendarRoutineDragActionButtons("))
        assertFalse(listComponents.contains("CalendarRoutineDragOverlay("))
        assertFalse(listComponents.contains("object : NestedScrollConnection"))
        assertFalse(listComponents.contains("Animatable("))
        assertFalse(calendarScreen.contains("TrainingList("))
        assertTrue(pagerContent.contains("TrainingList("))
    }

    @Test
    fun trainingCalendarItemComponentsStayOutOfMainCalendarScreen() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val monthComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarMonthComponents.kt")
        )
        val listItemComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarListItemComponents.kt")
        )
        val statusComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarStatusComponents.kt")
        )
        val monthDefinitions = listOf(
            "internal fun MonthlyTrainingCalendar",
            "internal fun MonthlyCalendarDayCell",
            "internal fun MonthlyCalendarItemChip"
        )
        val listItemDefinitions = listOf(
            "internal fun DayHeader",
            "internal fun TrainingItemRow",
            "internal fun LocalRunningResultSummary",
            "internal fun StrengthMatchSummary"
        )
        val statusDefinitions = listOf(
            "internal fun TrainingStatusIcons",
            "internal fun TrainingStatusIconContainer",
            "internal fun ResultCheckIcon"
        )

        monthDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarMonthComponents.kt", calendarScreen.contains(definition))
            assertFalse("$definition belongs in TrainingCalendarMonthComponents.kt", listItemComponents.contains(definition))
            assertFalse("$definition belongs in TrainingCalendarMonthComponents.kt", statusComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarMonthComponents.kt", monthComponents.contains(definition))
        }
        listItemDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarListItemComponents.kt", calendarScreen.contains(definition))
            assertFalse("$definition belongs in TrainingCalendarListItemComponents.kt", monthComponents.contains(definition))
            assertFalse("$definition belongs in TrainingCalendarListItemComponents.kt", statusComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarListItemComponents.kt", listItemComponents.contains(definition))
        }
        statusDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarStatusComponents.kt", calendarScreen.contains(definition))
            assertFalse("$definition belongs in TrainingCalendarStatusComponents.kt", monthComponents.contains(definition))
            assertFalse("$definition belongs in TrainingCalendarStatusComponents.kt", listItemComponents.contains(definition))
            assertTrue("$definition missing from TrainingCalendarStatusComponents.kt", statusComponents.contains(definition))
        }
    }

    @Test
    fun trainingCalendarSummaryComponentsStayOutOfMainCalendarScreen() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val summaryComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarSummaryComponents.kt")
        )
        val movedDefinitions = listOf(
            "internal data class SummaryDetail",
            "internal fun WeekSummary",
            "internal fun SummaryMetricColumn",
            "internal fun CalendarModeIcon"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarSummaryComponents.kt", calendarScreen.contains(definition))
            assertTrue("$definition missing from TrainingCalendarSummaryComponents.kt", summaryComponents.contains(definition))
        }
    }

    @Test
    fun trainingCalendarActionComponentsStayOutOfMainCalendarScreen() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val oldActionBucket = mainSourceRoot.resolve(
            "com/lighthousepark/intervalsgym/training/ui/TrainingCalendarActionComponents.kt"
        )
        val fabComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarFabComponents.kt")
        )
        val workoutActionSheet = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingWorkoutActionSheet.kt")
        )
        val strengthRoutineSaveSheet = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingStrengthRoutineSaveSheet.kt")
        )
        val movedDefinitionsByOwner = mapOf(
            fabComponents to listOf(
                "internal fun WeeklyTrainingFabMenu",
                "internal fun FabActionButton"
            ),
            workoutActionSheet to listOf("internal fun WorkoutActionBottomSheet"),
            strengthRoutineSaveSheet to listOf(
                "internal fun StrengthRoutineSaveBottomSheet",
                "internal fun StrengthRoutineSaveRow"
            )
        )

        assertFalse(
            "Training calendar actions should stay split into FAB, workout action sheet, and strength routine save sheet files.",
            Files.exists(oldActionBucket)
        )
        movedDefinitionsByOwner.forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertFalse("$definition belongs in a focused training action component file", calendarScreen.contains(definition))
                assertTrue("$definition missing from its focused training action component file", owner.contains(definition))
            }
        }
    }
}
