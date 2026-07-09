package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingArchitectureGuardTest {
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun trainingArchitectureGuardsStaySplitByConcern() {
        val guardRoot = testSourceRoot.resolve("com/lighthousepark/intervalsgym")
        val coreGuard = Files.readString(guardRoot.resolve("TrainingArchitectureGuardTest.kt"))
        val focusedGuards = listOf(
            "TrainingCalendarRouteArchitectureGuardTest.kt",
            "TrainingCalendarComponentsArchitectureGuardTest.kt",
            "TrainingDomainArchitectureGuardTest.kt"
        )

        focusedGuards.forEach { fileName ->
            assertTrue("$fileName should own its focused training architecture rules.", Files.exists(guardRoot.resolve(fileName)))
        }
        listOf(
            "trainingCalendarScreenUsesDataUseCaseForWeekLoading",
            "trainingCalendarListComponentsStayOutOfMainCalendarScreen",
            "workoutGraphRulesStaySplitByConcern"
        ).forEach { movedRuleName ->
            assertFalse(
                "$movedRuleName belongs in a focused training guard file.",
                Regex("""fun\s+$movedRuleName\(""").containsMatchIn(coreGuard)
            )
        }
    }
}
