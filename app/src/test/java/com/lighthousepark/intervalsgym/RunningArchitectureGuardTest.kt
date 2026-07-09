package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningArchitectureGuardTest {
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun runningArchitectureGuardsStaySplitByConcern() {
        val guardRoot = testSourceRoot.resolve("com/lighthousepark/intervalsgym")
        val coreGuard = Files.readString(guardRoot.resolve("RunningArchitectureGuardTest.kt"))
        val focusedGuards = listOf(
            "RunningSessionUiArchitectureGuardTest.kt",
            "RunningSessionStateArchitectureGuardTest.kt",
            "RunningRoutineArchitectureGuardTest.kt",
            "RunningDomainArchitectureGuardTest.kt"
        )

        focusedGuards.forEach { fileName ->
            assertTrue("$fileName should own its focused running architecture rules.", Files.exists(guardRoot.resolve(fileName)))
        }
        listOf(
            "runningSessionComponentsStayOutOfSessionScreen",
            "runningSessionResultSnapshotsOwnFinishSessionAssemblyAndSyncCalls",
            "runningRoutineComponentsChromeAndEffectsStayOutOfRouteOwners",
            "runningSessionProgressionRulesStayOutOfGenericRunningDomain"
        ).forEach { movedRuleName ->
            assertFalse(
                "$movedRuleName belongs in a focused running guard file.",
                Regex("""fun\s+$movedRuleName\(""").containsMatchIn(coreGuard)
            )
        }
    }
}
