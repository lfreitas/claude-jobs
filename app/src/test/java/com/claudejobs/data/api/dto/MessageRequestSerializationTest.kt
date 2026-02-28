package com.claudejobs.data.api.dto

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that [MessageRequest] and [ApiMessage] serialize to the exact JSON shape
 * required by the Anthropic Messages API.
 *
 * A mismatch here (e.g. camelCase field names or missing `max_tokens`) causes a
 * 400 Invalid Request error at the API level, so keeping this green is the first
 * line of defence against that class of bug.
 */
class MessageRequestSerializationTest {

    private val gson = Gson()

    // ── MessageRequest ────────────────────────────────────────────────────────

    @Test
    fun `max_tokens serialises with snake_case key`() {
        val request = MessageRequest(
            model = "claude-haiku-4-5-20251001",
            messages = listOf(ApiMessage(content = "hello"))
        )
        val obj = gson.toJson(request).asJsonObject()
        assertTrue("max_tokens key missing from JSON", obj.has("max_tokens"))
        assertEquals(1024, obj["max_tokens"].asInt)
    }

    @Test
    fun `custom max_tokens value is preserved`() {
        val request = MessageRequest(
            model = "claude-haiku-4-5-20251001",
            max_tokens = 2048,
            messages = listOf(ApiMessage(content = "hello"))
        )
        val obj = gson.toJson(request).asJsonObject()
        assertEquals(2048, obj["max_tokens"].asInt)
    }

    @Test
    fun `model name is included verbatim`() {
        listOf("claude-haiku-4-5-20251001", "claude-sonnet-4-6").forEach { model ->
            val obj = gson.toJson(
                MessageRequest(model = model, messages = listOf(ApiMessage(content = "x")))
            ).asJsonObject()
            assertEquals(model, obj["model"].asString)
        }
    }

    @Test
    fun `messages array is present and has correct size`() {
        val request = MessageRequest(
            model = "claude-haiku-4-5-20251001",
            messages = listOf(
                ApiMessage(content = "first"),
                ApiMessage(content = "second")
            )
        )
        val obj = gson.toJson(request).asJsonObject()
        assertTrue("messages key missing", obj.has("messages"))
        assertEquals(2, obj["messages"].asJsonArray.size())
    }

    @Test
    fun `all three required top-level fields are present`() {
        val obj = gson.toJson(
            MessageRequest(
                model = "claude-haiku-4-5-20251001",
                messages = listOf(ApiMessage(content = "ping"))
            )
        ).asJsonObject()

        assertTrue("model field missing",      obj.has("model"))
        assertTrue("max_tokens field missing", obj.has("max_tokens"))
        assertTrue("messages field missing",   obj.has("messages"))
    }

    // ── ApiMessage ────────────────────────────────────────────────────────────

    @Test
    fun `ApiMessage defaults role to user`() {
        val obj = gson.toJson(ApiMessage(content = "test")).asJsonObject()
        assertEquals("user", obj["role"].asString)
    }

    @Test
    fun `ApiMessage content is serialised correctly`() {
        val obj = gson.toJson(ApiMessage(content = "What is 2+2?")).asJsonObject()
        assertEquals("What is 2+2?", obj["content"].asString)
    }

    @Test
    fun `ApiMessage custom role is preserved`() {
        val obj = gson.toJson(ApiMessage(role = "assistant", content = "4")).asJsonObject()
        assertEquals("assistant", obj["role"].asString)
    }

    @Test
    fun `message inside request has correct structure`() {
        val request = MessageRequest(
            model = "claude-haiku-4-5-20251001",
            messages = listOf(ApiMessage(content = "hello"))
        )
        val firstMsg = gson.toJson(request).asJsonObject()["messages"]
            .asJsonArray[0].asJsonObject

        assertTrue("role missing from message",    firstMsg.has("role"))
        assertTrue("content missing from message", firstMsg.has("content"))
        assertEquals("user",  firstMsg["role"].asString)
        assertEquals("hello", firstMsg["content"].asString)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun String.asJsonObject(): JsonObject = gson.fromJson(this, JsonObject::class.java)
}
