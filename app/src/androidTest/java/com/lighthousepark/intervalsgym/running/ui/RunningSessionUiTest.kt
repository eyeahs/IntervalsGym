package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RunningSessionUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun runningSessionActionBar_warmupPrimaryInvokesCallback() {
        var primaryClicked = false

        composeRule.setThemedContent {
            RunningSessionActionBar(
                phase = RunningSessionPhase.WARMUP,
                currentBlockIndex = 0,
                isLastBlock = false,
                onPreviousBlock = {},
                onPrimaryAction = { primaryClicked = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }

        composeRule.onNodeWithText("Warmup 종료").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPrimaryAction)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(primaryClicked)
        }
    }

    @Test
    fun runningSession_blockSkipAdvancesAndLastBlockOpensSaveDialog() {
        val blocks = listOf(
            runningBlock(targetText = "8km/h · 1%").copy(
                index = 0,
                title = "Block 1",
                durationSeconds = 600,
                startSecond = 0,
                endSecond = 600
            ),
            runningBlock(targetText = "10km/h · 2%").copy(
                index = 1,
                title = "Block 2",
                durationSeconds = 600,
                startSecond = 600,
                endSecond = 1_200
            )
        )

        composeRule.setThemedContent {
            RunningSessionScreen(
                apiKey = "",
                routineName = "running-block-skip-flow",
                blocks = blocks,
                totalSeconds = 1_200,
                isHeartRateConnected = false,
                heartRateBpm = null,
                heartRateSamples = emptyList(),
                onHeartRateClick = {},
                onBack = {},
                onWorkoutFinished = {},
                runtimeOptions = runningSessionTestRuntimeOptions()
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPrimaryAction)
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText("Block 1 / 2", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Block 건너뛰기").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText("Block 2 / 2", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("운동 마치기").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText("러닝 기록 업로드")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("앱 로컬에는 수행 결과를 저장했습니다.").assertExists()
    }

    @Test
    fun runningSession_multipleOverlaySkipsThenScreenSkipAndFinish() {
        var overlayActionRequest by mutableStateOf(0)
        val blocks = List(4) { index ->
            runningBlock(targetText = "${8 + index}km/h · 1%").copy(
                index = index,
                title = "Block ${index + 1}",
                durationSeconds = 600,
                startSecond = index * 600,
                endSecond = (index + 1) * 600
            )
        }

        composeRule.setThemedContent {
            RunningSessionScreen(
                apiKey = "",
                routineName = "running-overlay-skip-then-screen-skip",
                blocks = blocks,
                totalSeconds = 2_400,
                isHeartRateConnected = false,
                heartRateBpm = null,
                heartRateSamples = emptyList(),
                onHeartRateClick = {},
                onBack = {},
                onWorkoutFinished = {},
                runtimeOptions = runningSessionTestRuntimeOptions(
                    overlayActionRequestOverride = overlayActionRequest
                )
            )
        }

        composeRule.runOnIdle { overlayActionRequest += 1 }
        composeRule.waitForBlockLabel("Block 1 / 4")

        composeRule.runOnIdle { overlayActionRequest += 1 }
        composeRule.waitForBlockLabel("Block 2 / 4")
        composeRule.runOnIdle { overlayActionRequest += 1 }
        composeRule.waitForBlockLabel("Block 3 / 4")

        composeRule.onNodeWithText("Block 건너뛰기").performClick()
        composeRule.waitForBlockLabel("Block 4 / 4")
        composeRule.onNodeWithText("운동 마치기").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText("러닝 기록 업로드")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("앱 로컬에는 수행 결과를 저장했습니다.").assertExists()
    }

    @Test
    fun runningSessionActionBar_blockActionsRespectPreviousAvailability() {
        var previousClicked = false
        var primaryClicks = 0

        composeRule.setThemedContent {
            RunningSessionActionBar(
                phase = RunningSessionPhase.BLOCK,
                currentBlockIndex = 0,
                isLastBlock = false,
                onPreviousBlock = { previousClicked = true },
                onPrimaryAction = { primaryClicks += 1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPreviousBlock)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPrimaryAction)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(false, previousClicked)
            assertEquals(1, primaryClicks)
        }
    }

    @Test
    fun runningBlockProgressEffect_stopsAfterCatchUpFinishesWorkout() {
        var phase by mutableStateOf(RunningSessionPhase.BLOCK)
        var catchUpCalls = 0
        var moveToNextCalls = 0

        composeRule.setThemedContent {
            RunningBlockProgressEffect(
                phase = phase,
                blockStartedAtMillis = 1L,
                blockEndAtMillis = 1L,
                currentBlockIndex = 0,
                currentBlockTargetText = "6km/h",
                actualBlocks = emptyList(),
                onNowMillisChanged = {},
                onCatchUpElapsedBlocks = {
                    catchUpCalls += 1
                    phase = RunningSessionPhase.FINISHED
                    true
                },
                isWorkoutFinished = { phase == RunningSessionPhase.FINISHED },
                onMoveToNextBlock = { _, _ -> moveToNextCalls += 1 }
            )
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertEquals(RunningSessionPhase.FINISHED, phase)
            assertEquals(1, catchUpCalls)
            assertEquals(0, moveToNextCalls)
        }
    }

    @Test
    fun runningBlockProgressEffect_restartsOnceAfterActualBlocksCatchUp() {
        var phase by mutableStateOf(RunningSessionPhase.BLOCK)
        var actualBlocks by mutableStateOf(emptyList<RoutineBlock>())
        var catchUpCalls = 0
        val restoredBlock = runningBlock(targetText = "6km/h").copy(durationSeconds = 180)

        composeRule.setThemedContent {
            val actualBlocksSnapshot = actualBlocks
            RunningBlockProgressEffect(
                phase = phase,
                blockStartedAtMillis = 1L,
                blockEndAtMillis = Long.MAX_VALUE,
                currentBlockIndex = 1,
                currentBlockTargetText = "6km/h",
                actualBlocks = actualBlocksSnapshot,
                onNowMillisChanged = {},
                onCatchUpElapsedBlocks = {
                    catchUpCalls += 1
                    if (actualBlocksSnapshot.isEmpty()) {
                        actualBlocks = listOf(restoredBlock)
                        true
                    } else {
                        phase = RunningSessionPhase.FINISHED
                        false
                    }
                },
                isWorkoutFinished = { phase == RunningSessionPhase.FINISHED },
                onMoveToNextBlock = { _, _ -> }
            )
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertEquals(RunningSessionPhase.FINISHED, phase)
            assertEquals(1, actualBlocks.size)
            assertEquals(2, catchUpCalls)
        }
    }

    @Test
    fun runningBlockProgressEffect_passesExpiredBlockIdentityToTransition() {
        var expectedIndex = -1
        var expectedStartedAtMillis = -1L

        composeRule.setThemedContent {
            RunningBlockProgressEffect(
                phase = RunningSessionPhase.BLOCK,
                blockStartedAtMillis = 123L,
                blockEndAtMillis = 1L,
                currentBlockIndex = 4,
                currentBlockTargetText = "8km/h",
                actualBlocks = emptyList(),
                onNowMillisChanged = {},
                onCatchUpElapsedBlocks = { false },
                isWorkoutFinished = { false },
                onMoveToNextBlock = { blockIndex, startedAtMillis ->
                    expectedIndex = blockIndex
                    expectedStartedAtMillis = startedAtMillis
                }
            )
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertEquals(4, expectedIndex)
            assertEquals(123L, expectedStartedAtMillis)
        }
    }

    @Test
    fun runningOverlayActionEffect_processesQueuedRequestsOneByOne() {
        var actionRequest by mutableStateOf(0)
        var handledActions = 0

        composeRule.setThemedContent {
            RunningOverlayActionEffect(
                actionRequestOverride = actionRequest,
                onPrimaryAction = { handledActions += 1 }
            )
        }

        composeRule.runOnIdle { actionRequest = 3 }
        composeRule.waitUntil(timeoutMillis = 5_000L) { handledActions == 3 }
        composeRule.runOnIdle {
            assertEquals(3, handledActions)
        }
    }

    @Test
    fun runningSessionActionBar_lastBlockInvokesPreviousAndFinishCallbacks() {
        var previousClicked = false
        var finishedClicked = false

        composeRule.setThemedContent {
            RunningSessionActionBar(
                phase = RunningSessionPhase.BLOCK,
                currentBlockIndex = 1,
                isLastBlock = true,
                onPreviousBlock = { previousClicked = true },
                onPrimaryAction = { finishedClicked = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }

        composeRule.onNodeWithText("운동 마치기").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPreviousBlock)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPrimaryAction)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(previousClicked)
            assertTrue(finishedClicked)
        }
    }

    @Test
    fun runningBlockPanel_exposesStepperActions() {
        var speedIncreaseClicked = false
        var inclineDecreaseClicked = false

        composeRule.setThemedContent {
            RunningBlockPanel(
                block = runningBlock(targetText = "10km/h · 1%"),
                blockIndex = 0,
                blockCount = 1,
                remainingSeconds = 15,
                blinkOn = false,
                isLastBlock = true,
                onSpeedDecrease = {},
                onSpeedIncrease = { speedIncreaseClicked = true },
                onInclineDecrease = { inclineDecreaseClicked = true },
                onInclineIncrease = {},
            )
        }

        composeRule.onNodeWithText("Block 1 / 1", substring = true).assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningTargetStepper("속도", "increase"))
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningTargetStepper("경사도", "decrease"))
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(speedIncreaseClicked)
            assertTrue(inclineDecreaseClicked)
        }
    }

    @Test
    fun runningTargetStepper_ignoresDisabledDecreaseAndInvokesEnabledIncrease() {
        var decreases = 0
        var increases = 0

        composeRule.setThemedContent {
            RunningTargetStepper(
                label = "속도",
                value = "0km/h",
                onDecrease = { decreases += 1 },
                onIncrease = { increases += 1 },
                canDecrease = false,
                canIncrease = true
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningTargetStepper("속도", "decrease"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningTargetStepper("속도", "increase"))
            .performClick()

        composeRule.runOnIdle {
            assertEquals(0, decreases)
            assertEquals(1, increases)
        }
    }

    @Test
    fun heartRateGraph_connectButtonInvokesCallback() {
        var connectClicked = false

        composeRule.setThemedContent {
            HeartRateGraph(
                samples = emptyList<HeartRateSample>(),
                isHeartRateConnected = false,
                heartRateBpm = null,
                onHeartRateClick = { connectClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningConnectHeartRate)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(connectClicked)
        }
    }

    @Test
    fun runningSessionTopBar_invokesBackAndStopCallbacks() {
        var backClicked = false
        var stopClicked = false

        composeRule.setThemedContent {
            RunningSessionTopBar(
                routineName = "러닝 Routine",
                phase = RunningSessionPhase.BLOCK,
                isStopEnabled = true,
                onBack = { backClicked = true },
                onStop = { stopClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningSessionBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningStopWorkout)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(backClicked)
            assertTrue(stopClicked)
        }
    }

    @Test
    fun runningSessionTopBar_hidesStopActionWhenFinished() {
        composeRule.setThemedContent {
            RunningSessionTopBar(
                routineName = "러닝 Routine",
                phase = RunningSessionPhase.FINISHED,
                isStopEnabled = true,
                onBack = {},
                onStop = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningSessionBack)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningStopWorkout)
            .assertDoesNotExist()
    }

    @Test
    fun runningFinishUploadChoiceDialog_invokesUploadAndGarminCallbacks() {
        var uploaded = false
        var usedGarmin = false

        composeRule.setThemedContent {
            RunningFinishUploadChoiceDialog(
                apiKey = "api-key",
                isUploading = false,
                finishError = "네트워크 오류",
                onUpload = { uploaded = true },
                onUseGarmin = { usedGarmin = true }
            )
        }

        composeRule.onNodeWithText("앱 로컬에는 수행 결과를 저장했습니다.").assertExists()
        composeRule.onNodeWithText("네트워크 오류").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningFinishUpload)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningFinishUseGarmin)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(uploaded)
            assertTrue(usedGarmin)
        }
    }

    @Test
    fun runningFinishUploadChoiceDialog_disablesUnavailableActions() {
        var apiKey by mutableStateOf("")
        var isUploading by mutableStateOf(false)

        composeRule.setThemedContent {
            RunningFinishUploadChoiceDialog(
                apiKey = apiKey,
                isUploading = isUploading,
                finishError = null,
                onUpload = {},
                onUseGarmin = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningFinishUpload)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningFinishUseGarmin)
            .assertIsEnabled()

        composeRule.runOnIdle {
            apiKey = "api-key"
            isUploading = true
        }

        composeRule.onNodeWithText("업로드 중").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningFinishUpload)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningFinishUseGarmin)
            .assertIsNotEnabled()
    }

    @Test
    fun runningStopSaveDialog_invokesSaveAndDiscardCallbacks() {
        var saved = false
        var discarded = false

        composeRule.setThemedContent {
            RunningStopSaveDialog(
                onDismiss = {},
                onSave = { saved = true },
                onDiscard = { discarded = true }
            )
        }

        composeRule.onNodeWithText("운동 중지").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningStopSave)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningStopDiscard)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(saved)
            assertTrue(discarded)
        }
    }

    @Test
    fun runningFinishedPanel_closeButtonInvokesCallback() {
        var closed = false

        composeRule.setThemedContent {
            RunningFinishedPanel(
                totalSeconds = 90,
                onClose = { closed = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningFinishClose)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(closed)
        }
    }
}

private fun runningBlock(targetText: String): RoutineBlock {
    return RoutineBlock(
        index = 0,
        title = "Block 1",
        kind = "work",
        targetText = targetText,
        durationSeconds = 60,
        startSecond = 0,
        endSecond = 60,
        isRecovery = false
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.waitForBlockLabel(
    label: String,
) {
    waitUntil(timeoutMillis = 5_000L) {
        onAllNodesWithText(label, substring = true).fetchSemanticsNodes().isNotEmpty()
    }
}

private fun runningSessionTestRuntimeOptions(
    overlayActionRequestOverride: Int? = null,
): RunningSessionRuntimeOptions {
    return RunningSessionRuntimeOptions(
        requestOverlayPermissionOnStart = false,
        enableSessionTickerEffects = false,
        enableExternalRuntimeEffects = false,
        overlayActionRequestOverride = overlayActionRequestOverride
    )
}
