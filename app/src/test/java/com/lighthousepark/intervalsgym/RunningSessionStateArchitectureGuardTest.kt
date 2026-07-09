package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionStateArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot

    @Test
    fun runningSessionProgressUiStateOwnsPhaseIndexAndTimingFields() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val progressUiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionProgressUiState.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var phase by",
            "var currentBlockIndex by",
            "var warmupStartedAtMillis by",
            "var blockEndAtMillis by",
            "var blockStartedAtMillis by"
        )
        val requiredDefinitions = listOf(
            "internal data class RunningSessionProgressUiState",
            "fun withStartedBlock(",
            "fun withCurrentBlockRecorded(",
            "fun withCatchUp(",
            "fun withFinished(",
            "internal fun runningSessionProgressUiStateSaver("
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in RunningSessionProgressUiState", sessionScreen.contains(declaration))
        }
        requiredDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningSessionProgressUiState.kt", sessionScreen.contains(definition))
            assertTrue("$definition missing from RunningSessionProgressUiState.kt", progressUiState.contains(definition))
        }
        assertTrue(sessionScreen.contains("var progressUiState"))
        assertTrue(sessionScreen.contains("RunningSessionProgressUiState.initial("))
    }

    @Test
    fun runningFinishUiStateOwnsFinishAndUploadFields() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val finishUiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionFinishUiState.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var finishedAtMillis",
            "var showFinishDialog",
            "var showStopSaveDialog",
            "var isUploadingRunningSession",
            "var finishError",
            "var localRunningSessionId"
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in RunningSessionFinishUiState.kt", sessionScreen.contains(declaration))
        }
        assertTrue(finishUiState.contains("data class RunningSessionFinishUiState"))
        assertTrue(finishUiState.contains("runningSessionFinishUiStateSaver"))
        assertTrue(sessionScreen.contains("var finishUiState"))
        assertTrue(sessionScreen.contains("finishUiState.withFinishedLocalSession("))
        assertTrue(sessionScreen.contains("finishUiState.withUploadStarted("))
    }

    @Test
    fun runningSessionResultSnapshotsOwnFinishSessionAssemblyAndSyncCalls() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val resultSnapshots = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionResultSnapshots.kt")
        )
        val uploadActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionUploadActions.kt")
        )
        val movedCalls = listOf(
            "buildRunningSessionForFinish(",
            "saveRunningSessionLocally(",
            "uploadRunningSession("
        )

        movedCalls.forEach { call ->
            assertFalse("$call belongs behind RunningSessionResultSnapshot", sessionScreen.contains(call))
            assertTrue("$call missing from RunningSessionResultSnapshots.kt", resultSnapshots.contains(call))
        }
        assertFalse(
            "RunningSessionScreen.kt should not import RunningSession directly for finish/upload assembly.",
            sessionScreen.contains("import com.lighthousepark.intervalsgym.running.RunningSession\n")
        )
        assertTrue(resultSnapshots.contains("internal data class RunningSessionResultSnapshot"))
        assertTrue(sessionScreen.contains("RunningSessionResultSnapshot("))
        assertTrue(sessionScreen.contains("saveLocalResult("))
        assertTrue(uploadActions.contains("uploadResult("))
    }

    @Test
    fun runningSessionUploadActionsOwnUploadPlanningAndStartDiagnostics() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val uploadActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionUploadActions.kt")
        )
        val movedDefinitions = listOf(
            "internal sealed interface RunningSessionUploadAction",
            "internal data object RunningSessionUploadLoginRequired",
            "internal data class RunningSessionUploadReady",
            "internal fun RunningSessionResultSnapshot.planRunningSessionUpload("
        )
        val movedCalls = listOf(
            "runningUploadStartedDiagnosticDetails(",
            "toRunningSession(endedAtMillis)"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningSessionUploadActions.kt", sessionScreen.contains(definition))
            assertTrue("$definition missing from RunningSessionUploadActions.kt", uploadActions.contains(definition))
        }
        movedCalls.forEach { call ->
            assertFalse("$call belongs behind RunningSessionUploadActions.kt", sessionScreen.contains(call))
            assertTrue("$call missing from RunningSessionUploadActions.kt", uploadActions.contains(call))
        }
        assertFalse(
            "RunningSessionScreen.kt should not compute upload endedAtMillis directly.",
            sessionScreen.contains("finishedAtMillis.takeIf")
        )
        assertTrue(sessionScreen.contains("planRunningSessionUpload("))
        assertTrue(sessionScreen.contains("uploadAction.uploadResult("))
    }

    @Test
    fun runningSessionTargetOverrideActionsOwnOverridePlanningAndDiagnostics() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val targetOverrideActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionTargetOverrideActions.kt")
        )
        val movedDefinitions = listOf(
            "internal data class RunningSessionTargetOverrideAction",
            "internal fun planRunningSessionTargetOverrideAction("
        )
        val movedCalls = listOf(
            "runningTargetOverrideChange(",
            "runningTargetOverrideDiagnosticDetails("
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningSessionTargetOverrideActions.kt", sessionScreen.contains(definition))
            assertTrue("$definition missing from RunningSessionTargetOverrideActions.kt", targetOverrideActions.contains(definition))
        }
        movedCalls.forEach { call ->
            assertFalse("$call belongs behind RunningSessionTargetOverrideActions.kt", sessionScreen.contains(call))
            assertTrue("$call missing from RunningSessionTargetOverrideActions.kt", targetOverrideActions.contains(call))
        }
        assertTrue(sessionScreen.contains("planRunningSessionTargetOverrideAction("))
    }

    @Test
    fun runningSessionBlockProgressActionsOwnProgressionPlanningAndDiagnostics() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val blockProgressActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionBlockProgressActions.kt")
        )
        val movedDefinitions = listOf(
            "internal data class RunningSessionRecordBlockAction",
            "internal fun planRunningSessionRecordBlockAction(",
            "internal data class RunningSessionCatchUpAction",
            "internal fun planRunningSessionCatchUpAction(",
            "internal data class RunningSessionPreviousBlockAction",
            "internal fun planRunningSessionPreviousBlockAction(",
            "internal sealed interface RunningSessionStartBlockAction",
            "internal data class RunningSessionStartBlockReady",
            "internal data object RunningSessionStartBlockUnavailable",
            "internal fun planRunningSessionStartBlockAction("
        )
        val movedCalls = listOf(
            "recordRunningCurrentBlock(",
            "catchUpRunningSessionBlocks(",
            "runningRecordedBlockDiagnosticDetails(",
            "runningCatchUpDiagnosticDetails(",
            "runningBlockStartedDiagnosticDetails(",
            "progressUiState.withStartedBlock(",
            "progressUiState.withCurrentBlockRecorded(",
            "progressUiState.withCatchUp("
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningSessionBlockProgressActions.kt", sessionScreen.contains(definition))
            assertTrue("$definition missing from RunningSessionBlockProgressActions.kt", blockProgressActions.contains(definition))
        }
        movedCalls.forEach { call ->
            assertFalse("$call belongs behind RunningSessionBlockProgressActions.kt", sessionScreen.contains(call))
            assertTrue("$call missing from RunningSessionBlockProgressActions.kt", blockProgressActions.contains(call))
        }
        assertTrue(sessionScreen.contains("planRunningSessionRecordBlockAction("))
        assertTrue(sessionScreen.contains("planRunningSessionCatchUpAction("))
        assertTrue(sessionScreen.contains("planRunningSessionPreviousBlockAction("))
        assertTrue(sessionScreen.contains("planRunningSessionStartBlockAction("))
    }

    @Test
    fun runningActualBlocksStateOwnsJsonListSynchronization() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val actualBlocksState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionActualBlocksState.kt")
        )
        val movedCalls = listOf(
            "toRoutineBlocksJsonArray(",
            "runningBlocksFromJson("
        )

        movedCalls.forEach { call ->
            assertFalse("$call belongs behind RunningSessionActualBlocksState", sessionScreen.contains(call))
            assertTrue("$call missing from RunningSessionActualBlocksState.kt", actualBlocksState.contains(call))
        }
        assertFalse(
            "RunningSessionScreen.kt should keep actual block list/json as one state object.",
            sessionScreen.contains("var actualBlocks by")
        )
        assertTrue(actualBlocksState.contains("data class RunningSessionActualBlocksState"))
        assertTrue(sessionScreen.contains("var actualBlocksState"))
        assertTrue(sessionScreen.contains("RunningSessionActualBlocksState.restored("))
    }
}
