package com.hermesandroid.relay.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hermesandroid.relay.ui.LocalSnackbarHost
import com.hermesandroid.relay.viewmodel.ChatViewModel
import com.hermesandroid.relay.viewmodel.ConnectionViewModel
import com.hermesandroid.relay.viewmodel.VoiceViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for the Chat empty state with suggestion chips.
 *
 * The empty state now sources suggestions from [R.array.chat_suggestions]
 * instead of hard-coding them inline. These tests verify at least one chip
 * renders and that chips expose a click action.
 */
class EmptyStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun chatEmptyState_showsAtLeastOneSuggestionChip() {
        composeTestRule.setContent {
            val chatViewModel = viewModel<ChatViewModel>()
            val connectionViewModel = viewModel<ConnectionViewModel>()
            val voiceViewModel = viewModel<VoiceViewModel>()
            MaterialTheme {
                CompositionLocalProvider(LocalSnackbarHost provides SnackbarHostState()) {
                    ChatScreen(
                        chatViewModel = chatViewModel,
                        connectionViewModel = connectionViewModel,
                        voiceViewModel = voiceViewModel,
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("What can you do?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Help me code").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explain something").assertIsDisplayed()
    }

    @Test
    fun chatEmptyState_suggestionChip_isClickable() {
        composeTestRule.setContent {
            val chatViewModel = viewModel<ChatViewModel>()
            val connectionViewModel = viewModel<ConnectionViewModel>()
            val voiceViewModel = viewModel<VoiceViewModel>()
            MaterialTheme {
                CompositionLocalProvider(LocalSnackbarHost provides SnackbarHostState()) {
                    ChatScreen(
                        chatViewModel = chatViewModel,
                        connectionViewModel = connectionViewModel,
                        voiceViewModel = voiceViewModel,
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("What can you do?").assertHasClickAction()
    }
}
