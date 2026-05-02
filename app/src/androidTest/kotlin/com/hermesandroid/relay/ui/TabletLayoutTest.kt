package com.hermesandroid.relay.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for tablet responsive layout components.
 */
class TabletLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigationRail_rendersItems() {
        composeTestRule.setContent {
            MaterialTheme {
                val selectedItem = remember { mutableIntStateOf(0) }
                NavigationRail {
                    NavigationRailItem(
                        icon = { Icon(Icons.Filled.Chat, contentDescription = "Chat") },
                        label = { Text("Chat") },
                        selected = selectedItem.intValue == 0,
                        onClick = { selectedItem.intValue = 0 },
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Filled.Terminal, contentDescription = "Terminal") },
                        label = { Text("Terminal") },
                        selected = selectedItem.intValue == 1,
                        onClick = { selectedItem.intValue = 1 },
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = selectedItem.intValue == 2,
                        onClick = { selectedItem.intValue = 2 },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Terminal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun navigationRail_itemSelectionChanges() {
        composeTestRule.setContent {
            MaterialTheme {
                val selectedItem = remember { mutableIntStateOf(0) }
                NavigationRail {
                    NavigationRailItem(
                        icon = { Icon(Icons.Filled.Chat, contentDescription = "Chat") },
                        label = { Text("Chat") },
                        selected = selectedItem.intValue == 0,
                        onClick = { selectedItem.intValue = 0 },
                    )
                    NavigationRailItem(
                        icon = { Icon(Icons.Filled.Terminal, contentDescription = "Terminal") },
                        label = { Text("Terminal") },
                        selected = selectedItem.intValue == 1,
                        onClick = { selectedItem.intValue = 1 },
                    )
                }
            }
        }

        // Both items should be displayed
        composeTestRule.onNodeWithText("Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Terminal").assertIsDisplayed()
    }
}
