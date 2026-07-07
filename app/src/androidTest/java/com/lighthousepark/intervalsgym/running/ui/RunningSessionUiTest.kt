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
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.running.HeartRateSample
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
