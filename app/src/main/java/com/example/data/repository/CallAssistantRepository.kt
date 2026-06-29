package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.LogEntity
import com.example.data.local.PreferencesManager
import com.example.data.remote.GroqApiService
import com.example.data.remote.GroqChatRequest
import com.example.data.remote.GroqMessage
import com.example.util.AppLogger
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class CallAssistantRepository(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val conversationDao = database.conversationDao()
    private val logDao = database.logDao()
    private val prefs = PreferencesManager(context)

    companion object {
        private const val TAG = "CallAssistantRepository"
        
        // Define default recommended stable models for Groq
        val MODEL_LLAMA3_8B = "llama3-8b-8192"
        val MODEL_MIXTRAL_8X7B = "mixtral-8x7b-32768"
        val MODEL_LLAMA_3_3_70B = "llama-3.3-70b-versatile"
    }

    init {
        AppLogger.initialize(database)
    }

    // Database Conversations
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    val favoriteConversations: Flow<List<ConversationEntity>> = conversationDao.getFavoriteConversations()

    fun searchConversations(query: String): Flow<List<ConversationEntity>> {
        return conversationDao.searchConversations(query)
    }

    suspend fun insertConversation(conversation: ConversationEntity): Long {
        AppLogger.i(TAG, "Inserting conversation for: ${conversation.callerName} (${conversation.phoneNumber})")
        return conversationDao.insertConversation(conversation)
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        AppLogger.d(TAG, "Updating conversation ID: ${conversation.id}")
        conversationDao.updateConversation(conversation)
    }

    suspend fun deleteConversationById(id: Long) {
        AppLogger.w(TAG, "Deleting conversation ID: $id")
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearAllConversations() {
        AppLogger.w(TAG, "Clearing all conversation history")
        conversationDao.clearAllConversations()
    }

    // Database Logs
    fun getRecentLogs(limit: Int = 100): Flow<List<LogEntity>> {
        return logDao.getRecentLogs(limit)
    }

    suspend fun clearAllLogs() {
        logDao.clearAllLogs()
    }

    // Preferences & Settings Access
    var aiModeEnabled: Boolean
        get() = prefs.aiModeEnabled
        set(value) { prefs.aiModeEnabled = value }

    var systemPrompt: String
        get() = prefs.systemPrompt
        set(value) { prefs.systemPrompt = value }

    var speechLanguage: String
        get() = prefs.speechLanguage
        set(value) { prefs.speechLanguage = value }

    var speechVolume: Float
        get() = prefs.speechVolume
        set(value) { prefs.speechVolume = value }

    var speechRate: Float
        get() = prefs.speechRate
        set(value) { prefs.speechRate = value }

    var batterySavingMode: Boolean
        get() = prefs.batterySavingMode
        set(value) { prefs.batterySavingMode = value }

    var aiProvider: String
        get() = prefs.aiProvider
        set(value) { prefs.aiProvider = value }

    var aiModel: String
        get() = prefs.aiModel
        set(value) { prefs.aiModel = value }

    fun getApiKeyForProvider(provider: String): String = prefs.getApiKeyForProvider(provider)
    fun saveApiKeyForProvider(key: String, provider: String) { prefs.saveApiKeyForProvider(key, provider) }

    fun getApiKey(): String = prefs.getApiKey()
    fun saveApiKey(key: String) { prefs.saveApiKey(key) }
    fun clearPreferences() { prefs.clearAll() }

    private fun getApiServiceForProvider(provider: String): GroqApiService {
        val baseUrl = when (provider) {
            "openai" -> "https://api.openai.com/v1/"
            "openrouter" -> "https://openrouter.ai/api/v1/"
            "gemini" -> "https://generativelanguage.googleapis.com/v1beta/openai/"
            else -> "https://api.groq.com/openai/v1/"
        }
        
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            // Scrub authorization header and API keys from logs to prevent leaks
            val scrubbed = if (message.contains("Authorization: Bearer") || message.contains("api.groq.com") || message.contains("api_key")) {
                "Authorization: Bearer [REDACTED_FOR_SECURITY]"
            } else {
                message
            }
            AppLogger.d("OkHttp", scrubbed)
        }.setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GroqApiService::class.java)
    }

    suspend fun getGroqCompletion(
        userMessage: String,
        history: List<GroqMessage> = emptyList(),
        customModel: String = ""
    ): String {
        val provider = aiProvider
        val apiKey = getApiKeyForProvider(provider)
        if (apiKey.isBlank()) {
            val errorMsg = when (provider) {
                "gemini" -> "Gemini API kaliti sozlanmagan! Iltimos Sozlamalar bo'limida kiriting."
                "openai" -> "OpenAI API kaliti sozlanmagan! Iltimos Sozlamalar bo'limida kiriting."
                "openrouter" -> "OpenRouter API kaliti sozlanmagan! Iltimos Sozlamalar bo'limida kiriting."
                else -> "Groq API kaliti sozlanmagan! Iltimos Sozlamalar bo'limida kiriting."
            }
            AppLogger.e(TAG, errorMsg)
            throw IllegalStateException(errorMsg)
        }

        val modelToUse = if (customModel.isNotBlank()) {
            customModel
        } else {
            aiModel
        }

        val messages = mutableListOf<GroqMessage>()
        messages.add(GroqMessage(role = "system", content = systemPrompt))
        messages.addAll(history)
        messages.add(GroqMessage(role = "user", content = userMessage))

        val request = GroqChatRequest(
            model = modelToUse,
            messages = messages,
            temperature = 0.7,
            stream = false
        )

        AppLogger.i(TAG, "Requesting AI completions using $provider model: $modelToUse")
        val authHeader = "Bearer $apiKey"
        
        var retryCount = 0
        val maxRetries = 2
        var lastException: Exception? = null
        
        while (retryCount <= maxRetries) {
            try {
                val service = getApiServiceForProvider(provider)
                val response = service.getChatCompletions(authHeader, request)
                val reply = response.choices.firstOrNull()?.message?.content
                if (reply != null) {
                    AppLogger.d(TAG, "$provider response success: ${reply.take(50)}...")
                    return reply
                } else {
                    val errorMsg = "API bo'sh javob qaytardi."
                    AppLogger.e(TAG, errorMsg)
                    throw Exception(errorMsg)
                }
            } catch (e: Exception) {
                lastException = e
                retryCount++
                if (retryCount <= maxRetries) {
                    AppLogger.w(TAG, "API Call failed, retrying ($retryCount/$maxRetries)... Error: ${e.localizedMessage}")
                    kotlinx.coroutines.delay(1000L * retryCount)
                }
            }
        }
        
        val finalError = lastException ?: Exception("Noma'lum xato aloqada")
        AppLogger.e(TAG, "All retries failed for $provider API: ${finalError.localizedMessage}", finalError)
        throw finalError
    }
}
