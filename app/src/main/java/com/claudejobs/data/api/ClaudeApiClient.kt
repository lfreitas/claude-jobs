package com.claudejobs.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClaudeApiClient {

    /**
     * Creates a [ClaudeApiService] configured with the given [apiKey].
     *
     * @param apiKey   Anthropic API key sent via `x-api-key` header.
     * @param betaHeader Optional value for the `anthropic-beta` header required by
     *   certain features, e.g. `"code-execution-web-tools-2026-02-09"` for dynamic
     *   web-search filtering when code execution is also enabled.
     */
    fun create(apiKey: String, betaHeader: String? = null): ClaudeApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttp = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                if (betaHeader != null) {
                    builder.addHeader("anthropic-beta", betaHeader)
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            // Code execution and web search can run for tens of seconds server-side.
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClaudeApiService::class.java)
    }
}
