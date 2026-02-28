package com.claudejobs.data.api.dto

data class MessageRequest(
    val model: String,
    val max_tokens: Int = 1024,
    val messages: List<ApiMessage>
)

data class ApiMessage(
    val role: String = "user",
    val content: String
)
