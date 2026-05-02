package com.hermesandroid.relay.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.material3.MaterialTheme
import com.hermesandroid.relay.data.ChatMessage
import com.hermesandroid.relay.data.MessageRole
import com.hermesandroid.relay.data.MessageStatus
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for MessageBubble status indicators.
 */
class MessageBubbleStatusTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userMessage_sending_showsScheduleIcon() {
        composeTestRule.setContent {
            MaterialTheme {
                MessageBubble(
                    message = ChatMessage(
                        id = "test-1",
                        role = MessageRole.USER,
                        content = "Hello",
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.SENDING,
                    ),
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Sending")
            .assertIsDisplayed()
    }

    @Test
    fun userMessage_failed_showsErrorIcon() {
        composeTestRule.setContent {
            MaterialTheme {
                MessageBubble(
                    message = ChatMessage(
                        id = "test-2",
                        role = MessageRole.USER,
                        content = "Hello",
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.FAILED,
                    ),
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Failed")
            .assertIsDisplayed()
    }

    @Test
    fun userMessage_sent_noStatusIcon() {
        composeTestRule.setContent {
            MaterialTheme {
                MessageBubble(
                    message = ChatMessage(
                        id = "test-3",
                        role = MessageRole.USER,
                        content = "Hello",
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.SENT,
                    ),
                )
            }
        }

        // SENT status should not show any icon
        composeTestRule
            .onNodeWithContentDescription("Sending")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithContentDescription("Failed")
            .assertDoesNotExist()
    }

    @Test
    fun assistantMessage_noStatusIconRegardless() {
        composeTestRule.setContent {
            MaterialTheme {
                MessageBubble(
                    message = ChatMessage(
                        id = "test-4",
                        role = MessageRole.ASSISTANT,
                        content = "Hi there",
                        timestamp = System.currentTimeMillis(),
                        status = MessageStatus.SENDING,
                    ),
                )
            }
        }

        // Assistant messages should not show status icons
        composeTestRule
            .onNodeWithContentDescription("Sending")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithContentDescription("Failed")
            .assertDoesNotExist()
    }
}
