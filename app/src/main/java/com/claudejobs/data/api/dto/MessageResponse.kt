package com.claudejobs.data.api.dto

data class MessageResponse(
    val id: String = "",
    val type: String = "",
    val role: String = "",
    val content: List<ContentBlock> = emptyList(),
    val model: String = "",
    val stop_reason: String = ""
)

data class ContentBlock(
    val type: String = "",
    val text: String = ""
)
