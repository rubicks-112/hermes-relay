package com.hermesandroid.relay.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.hermesandroid.relay.auth.AuthState
import org.junit.Rule
import org.junit.Test

class ConnectionWizardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun manualEntryStep_showsPasteIcon_onApiUrlField() {
        composeTestRule.setContent {
            MaterialTheme {
                ManualEntryStep(
                    apiUrl = "",
                    onApiUrlChange = {},
                    relayUrl = "",
                    onRelayUrlChange = {},
                    code = "",
                    onCodeChange = {},
                    onBack = {},
                    onSubmit = {},
                )
            }
        }
        composeTestRule.onAllNodesWithContentDescription("Paste URL")
            .get(0)
            .assertIsDisplayed()
    }

    @Test
    fun manualEntryStep_showsPasteIcon_onRelayUrlField() {
        composeTestRule.setContent {
            MaterialTheme {
                ManualEntryStep(
                    apiUrl = "",
                    onApiUrlChange = {},
                    relayUrl = "",
                    onRelayUrlChange = {},
                    code = "",
                    onCodeChange = {},
                    onBack = {},
                    onSubmit = {},
                )
            }
        }
        composeTestRule.onAllNodesWithContentDescription("Paste URL")
            .get(1)
            .assertIsDisplayed()
    }

    @Test
    fun manualEntryStep_showsPasteIcon_onPairingCodeField() {
        composeTestRule.setContent {
            MaterialTheme {
                ManualEntryStep(
                    apiUrl = "",
                    onApiUrlChange = {},
                    relayUrl = "",
                    onRelayUrlChange = {},
                    code = "",
                    onCodeChange = {},
                    onBack = {},
                    onSubmit = {},
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Paste code")
            .assertIsDisplayed()
    }

    @Test
    fun verifyStep_showsCancelButton_duringActivePairing() {
        composeTestRule.setContent {
            MaterialTheme {
                VerifyStep(
                    authState = AuthState.Pairing,
                    error = null,
                    onRetry = {},
                    onBack = {},
                    onCancel = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
    }

    @Test
    fun verifyStep_showsCancelButton_afterError() {
        composeTestRule.setContent {
            MaterialTheme {
                VerifyStep(
                    authState = AuthState.Failed("Connection refused"),
                    error = "Connection refused",
                    onRetry = {},
                    onBack = {},
                    onCancel = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
    }
}
