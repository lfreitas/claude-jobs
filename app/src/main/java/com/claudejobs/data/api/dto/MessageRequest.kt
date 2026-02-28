package com.claudejobs.data.api.dto

/**
 * Top-level request sent to POST /v1/messages.
 *
 * Fields serialised by Gson; `null` fields are omitted when Gson's default
 * serializeNulls is off (the default), so optional API fields such as [system]
 * and [tools] are cleanly absent from the JSON when not set.
 */
data class MessageRequest(
    val model: String,
    val max_tokens: Int = 4096,
    /** Optional system prompt. Omitted from JSON when null. */
    val system: String? = null,
    /**
     * Server-side tool definitions. Omitted from JSON when null.
     * Examples: web_search_20260209, web_fetch_20260209, code_execution_20260120
     */
    val tools: List<ToolDefinition>? = null,
    val messages: List<ApiMessage>
)

/**
 * A single conversation turn.
 *
 * [content] is typed as [Any] so it can be:
 *  - a plain [String] for simple user messages
 *  - a [List] of [ContentBlock] when replaying an assistant turn (e.g. during
 *    pause_turn continuation loops)
 *
 * Gson serialises both correctly: a String becomes a JSON string; a List becomes
 * a JSON array.
 */
data class ApiMessage(
    val role: String = "user",
    val content: Any
)

/**
 * Declares a server-side tool made available to Claude.
 *
 * For server-side tools Anthropic executes the tool on its own infrastructure;
 * the app never runs the tool itself.
 *
 * Known type/name pairs:
 *  - "web_search_20260209"  / "web_search"
 *  - "web_fetch_20260209"   / "web_fetch"
 *  - "code_execution_20260120" / "code_execution"
 */
data class ToolDefinition(
    val type: String,
    val name: String
)
