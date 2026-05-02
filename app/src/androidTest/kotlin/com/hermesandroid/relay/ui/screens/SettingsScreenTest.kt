package com.hermesandroid.relay.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.hermesandroid.relay.network.ConnectionState
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activeAgentCard_rendersConnectedStatus() {
        composeTestRule.setContent {
            MaterialTheme {
                ActiveAgentCard(
                    agentName = "Hermes",
                    connectionLabel = "Home",
                    model = "gpt-4",
                    personalityLabel = "Default",
                    isCustomized = false,
                    connectionState = ConnectionState.Connected,
                    onClick = {},
                    isDarkTheme = false,
                )
            }
        }
        composeTestRule.onNodeWithText("Connected")
            .assertIsDisplayed()
    }

    @Test
    fun activeAgentCard_rendersDisconnectedStatus() {
        composeTestRule.setContent {
            MaterialTheme {
                ActiveAgentCard(
                    agentName = "Hermes",
                    connectionLabel = "Home",
                    model = "gpt-4",
                    personalityLabel = "Default",
                    isCustomized = false,
                    connectionState = ConnectionState.Disconnected,
                    onClick = {},
                    isDarkTheme = false,
                )
            }
        }
        composeTestRule.onNodeWithText("Disconnected")
            .assertIsDisplayed()
    }
}
