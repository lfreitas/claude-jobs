package com.claudejobs.data.api.dto

/**
 * Represents the error body returned by the Anthropic API for non-2xx responses.
 *
 * Example payload:
 * ```json
 * {
 *   "type": "error",
 *   "error": {
 *     "type": "invalid_request_error",
 *     "message": "max_tokens: field required"
 *   }
 * }
 * ```
 */
data class AnthropicErrorResponse(
    val type: String = "",
    val error: AnthropicError? = null
)

data class AnthropicError(
    val type: String = "",
    val message: String = ""
)
