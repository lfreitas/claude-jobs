package com.claudejobs.data.api

import com.claudejobs.data.api.dto.MessageRequest
import com.claudejobs.data.api.dto.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ClaudeApiService {
    /**
     * POST /v1/messages — Anthropic Messages API.
     *
     * Returns a [Response] wrapper so the caller can inspect HTTP status codes and
     * read the error body on non-2xx responses (e.g. to surface Anthropic's error
     * message rather than a generic Retrofit exception).
     */
    @POST("v1/messages")
    suspend fun sendMessage(@Body request: MessageRequest): Response<MessageResponse>
}
