package com.claudejobs.data.api

import com.claudejobs.data.api.dto.MessageRequest
import com.claudejobs.data.api.dto.MessageResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun sendMessage(@Body request: MessageRequest): MessageResponse
}
