package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.lighthousepark.intervalsgym.core.TestContentDescriptions

internal fun ComposeTestRule.assertStrengthSupersetSelectionContract(
    existingGroupEntryIds: List<Int>,
    looseEntryId: Int,
    hiddenActionContentDescriptions: List<String>,
) {
    onNodeWithText("슈퍼세트로 묶을 운동을 선택하세요.").assertDoesNotExist()
    onNodeWithText("0개 선택됨").assertDoesNotExist()
    onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset).assertExists()
    onNodeWithContentDescription(TestContentDescriptions.StrengthClearSuperset).assertExists()
    onNodeWithContentDescription(TestContentDescriptions.StrengthCancelSuperset).assertExists()

    existingGroupEntryIds.forEach { entryId ->
        onNodeWithContentDescription(TestContentDescriptions.strengthSupersetEntryToggle(entryId))
            .assertDoesNotExist()
        onNodeWithContentDescription(TestContentDescriptions.strengthSupersetEntryLabel(entryId))
            .assertTextContains("A")
    }
    onNodeWithContentDescription(TestContentDescriptions.strengthSupersetEntryToggle(looseEntryId))
        .assertExists()
    hiddenActionContentDescriptions.forEach { contentDescription ->
        onNodeWithContentDescription(contentDescription).assertDoesNotExist()
    }
}
