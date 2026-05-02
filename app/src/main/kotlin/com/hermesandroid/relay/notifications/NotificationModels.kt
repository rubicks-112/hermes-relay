package com.hermesandroid.relay.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire model for a single notification entry forwarded by
 * [HermesNotificationCompanion] over the WSS connection.
 *
 * Mirrors the Python-side payload shape that
 * `plugin/relay/channels/notifications.py::NotificationsChannel`
 * caches in its bounded deque, so the relay can deserialize without
 * any per-field translation.
 *
 * The fields are intentionally minimal — no icon, no big-text expansion,
 * no actions — because the smartwatch-companion use case is "tell me
 * what came in" not "let me interact with it from my LLM". Adding
 * fields later is a non-breaking change as long as Python's deque
 * stays a `dict[str, Any]`.
 */
@Serializable
data class NotificationEntry(
    @SerialName("package_name")
    val packageName: String,
    val title: String? = null,
    val text: String? = null,
    @SerialName("sub_text")
    val subText: String? = null,
    @SerialName("posted_at")
    val postedAt: Long,
    val key: String,
    val category: String? = null,
    @SerialName("big_text")
    val bigText: String? = null,
    @SerialName("inbox_lines")
    val inboxLines: List<String>? = null,
    val actions: List<String>? = null,
    val messages: List<NotificationMessage>? = null,
    @SerialName("conversation_title")
    val conversationTitle: String? = null,
    @SerialName("has_image")
    val hasImage: Boolean = false,
    val progress: NotificationProgress? = null,
)

@Serializable
data class NotificationMessage(
    val text: String? = null,
    val timestamp: Long,
    val sender: String? = null,
)

@Serializable
data class NotificationProgress(
    val current: Int,
    val max: Int,
    val indeterminate: Boolean = false,
)
