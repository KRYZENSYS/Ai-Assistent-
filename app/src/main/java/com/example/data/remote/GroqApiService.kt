package com.example.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("chat/completions")
    suspend fun getChatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): GroqChatResponse
}
