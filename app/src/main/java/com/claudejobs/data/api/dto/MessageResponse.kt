package com.claudejobs.data.api.dto

/**
 * Response from POST /v1/messages.
 *
 * [stop_reason] values relevant to this app:
 *  - "end_turn"   – Claude finished normally
 *  - "pause_turn" – server-side tool loop hit its iteration limit; send the
 *                   assistant message back as a continuation to resume
 *  - "max_tokens" – response was truncated; increase max_tokens
 */
data class MessageResponse(
    val id: String = "",
    val type: String = "",
    val role: String = "",
    val content: List<ContentBlock> = emptyList(),
    val model: String = "",
    val stop_reason: String = ""
)

/**
 * A single block inside a [MessageResponse.content] array.
 *
 * Claude may return several block types in a single response:
 *  - "text"            – plain text answer; use [text]
 *  - "thinking"        – extended thinking (ignored by this app)
 *  - "server_tool_use" – Claude is invoking a server-side tool (web_search etc.)
 *  - "tool_use"        – client-side tool call (not used in this app)
 *
 * Only [text] blocks are surfaced to the user; all other block types are
 * preserved in the messages list so continuation requests are correct.
 *
 * [text] is nullable because non-text blocks carry different payload fields
 * (id, name, input) rather than a text value.
 */
data class ContentBlock(
    val type: String = "",
    val text: String? = null
)
