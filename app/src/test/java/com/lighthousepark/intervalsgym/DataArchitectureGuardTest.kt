package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataArchitectureGuardTest {
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun dataArchitectureGuardsStaySplitByConcern() {
        val guardRoot = testSourceRoot.resolve("com/lighthousepark/intervalsgym")
        val coreGuard = Files.readString(guardRoot.resolve("DataArchitectureGuardTest.kt"))
        val focusedGuards = listOf(
            "DataStorageTestArchitectureGuardTest.kt",
            "DataLayerBoundaryArchitectureGuardTest.kt"
        )

        focusedGuards.forEach { fileName ->
            assertTrue("$fileName should own its focused data architecture rules.", Files.exists(guardRoot.resolve(fileName)))
        }
        listOf(
            "strengthRoutineStorageTestsStayFocused",
            "completedHistoryStorageStaysSplitByWorkoutType",
            "uiScreensUseIntervalsUseCaseFactoryInsteadOfRemoteWiring"
        ).forEach { movedRuleName ->
            assertFalse(
                "$movedRuleName belongs in a focused data guard file.",
                Regex("""fun\s+$movedRuleName\(""").containsMatchIn(coreGuard)
            )
        }
    }
}
