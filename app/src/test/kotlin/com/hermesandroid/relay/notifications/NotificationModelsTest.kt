package com.hermesandroid.relay.notifications

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for NotificationEntry, NotificationMessage, and NotificationProgress
 * serialization round-trips and default values.
 */
class NotificationModelsTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    // --- JSON round-trip with all new fields populated ---

    @Test
    fun notificationEntry_fullRoundTrip() {
        val original = NotificationEntry(
            packageName = "com.example.app",
            title = "Title",
            text = "Body text",
            subText = "Sub text",
            postedAt = 1700000000000L,
            key = "key-1",
            category = "msg",
            bigText = "Big expanded text",
            inboxLines = listOf("Line 1", "Line 2"),
            actions = listOf("Reply", "Dismiss"),
            messages = listOf(
                NotificationMessage(text = "Hello", timestamp = 1700000001000L, sender = "Alice"),
                NotificationMessage(text = "Hi", timestamp = 1700000002000L, sender = "Bob")
            ),
            conversationTitle = "Group Chat",
            hasImage = true,
            progress = NotificationProgress(current = 50, max = 100, indeterminate = false)
        )

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NotificationEntry>(serialized)

        assertEquals(original.packageName, deserialized.packageName)
        assertEquals(original.title, deserialized.title)
        assertEquals(original.text, deserialized.text)
        assertEquals(original.subText, deserialized.subText)
        assertEquals(original.postedAt, deserialized.postedAt)
        assertEquals(original.key, deserialized.key)
        assertEquals(original.category, deserialized.category)
        assertEquals(original.bigText, deserialized.bigText)
        assertEquals(original.inboxLines, deserialized.inboxLines)
        assertEquals(original.actions, deserialized.actions)
        assertNotNull(deserialized.messages)
        assertEquals(2, deserialized.messages!!.size)
        assertEquals("Hello", deserialized.messages[0].text)
        assertEquals(1700000001000L, deserialized.messages[0].timestamp)
        assertEquals("Alice", deserialized.messages[0].sender)
        assertEquals(original.conversationTitle, deserialized.conversationTitle)
        assertEquals(original.hasImage, deserialized.hasImage)
        assertNotNull(deserialized.progress)
        assertEquals(50, deserialized.progress!!.current)
        assertEquals(100, deserialized.progress.max)
        assertFalse(deserialized.progress.indeterminate)
    }

    // --- JSON decode of legacy payload (only old fields) ---

    @Test
    fun notificationEntry_legacyPayload_defaultsNewFields() {
        val legacyJson = """
            {
                "package_name": "com.legacy.app",
                "title": "Legacy Title",
                "text": "Legacy text",
                "sub_text": "Legacy sub",
                "posted_at": 1600000000000,
                "key": "legacy-key"
            }
        """.trimIndent()

        val entry = json.decodeFromString<NotificationEntry>(legacyJson)

        assertEquals("com.legacy.app", entry.packageName)
        assertEquals("Legacy Title", entry.title)
        assertEquals("Legacy text", entry.text)
        assertEquals("Legacy sub", entry.subText)
        assertEquals(1600000000000L, entry.postedAt)
        assertEquals("legacy-key", entry.key)
        assertNull(entry.category)
        assertNull(entry.bigText)
        assertNull(entry.inboxLines)
        assertNull(entry.actions)
        assertNull(entry.messages)
        assertNull(entry.conversationTitle)
        assertFalse(entry.hasImage)
        assertNull(entry.progress)
    }

    // --- NotificationProgress defaults ---

    @Test
    fun notificationProgress_indeterminateDefaultsToFalse() {
        val progress = NotificationProgress(current = 50, max = 100)
        assertFalse(progress.indeterminate)
    }

    @Test
    fun notificationProgress_roundTrip() {
        val original = NotificationProgress(current = 25, max = 75, indeterminate = true)
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NotificationProgress>(serialized)

        assertEquals(original.current, deserialized.current)
        assertEquals(original.max, deserialized.max)
        assertTrue(deserialized.indeterminate)
    }

    // --- NotificationMessage serialization ---

    @Test
    fun notificationMessage_roundTrip() {
        val original = NotificationMessage(text = "hello", timestamp = 12345L, sender = "Alice")
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NotificationMessage>(serialized)

        assertEquals("hello", deserialized.text)
        assertEquals(12345L, deserialized.timestamp)
        assertEquals("Alice", deserialized.sender)
    }

    @Test
    fun notificationMessage_nullableFieldsRoundTrip() {
        val original = NotificationMessage(text = null, timestamp = 999L, sender = null)
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NotificationMessage>(serialized)

        assertNull(deserialized.text)
        assertEquals(999L, deserialized.timestamp)
        assertNull(deserialized.sender)
    }

    // --- Empty inboxLines/actions ---

    @Test
    fun notificationEntry_emptyListsRoundTrip() {
        val original = NotificationEntry(
            packageName = "com.empty.app",
            title = "Empty",
            text = "Text",
            postedAt = 1700000000000L,
            key = "empty-key",
            inboxLines = emptyList(),
            actions = emptyList()
        )

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NotificationEntry>(serialized)

        assertNotNull(deserialized.inboxLines)
        assertTrue(deserialized.inboxLines!!.isEmpty())
        assertNotNull(deserialized.actions)
        assertTrue(deserialized.actions!!.isEmpty())
    }

    // --- hasImage default ---

    @Test
    fun notificationEntry_hasImageDefaultsToFalse() {
        val entry = NotificationEntry(
            packageName = "com.test.app",
            postedAt = 1700000000000L,
            key = "test-key"
        )
        assertFalse(entry.hasImage)
    }
}
