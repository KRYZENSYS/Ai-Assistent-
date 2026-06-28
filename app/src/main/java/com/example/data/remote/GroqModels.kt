package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroqMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class GroqChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<GroqMessage>,
    @Json(name = "temperature") val temperature: Double = 0.7,
    @Json(name = "stream") val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class GroqChatResponse(
    @Json(name = "id") val id: String?,
    @Json(name = "object") val `object`: String?,
    @Json(name = "created") val created: Long?,
    @Json(name = "model") val model: String?,
    @Json(name = "choices") val choices: List<GroqChoice>
)

@JsonClass(generateAdapter = true)
data class GroqChoice(
    @Json(name = "index") val index: Int,
    @Json(name = "message") val message: GroqMessage,
    @Json(name = "finish_reason") val finishReason: String?
)
