package com.example.data.local

import android.content.Context
import com.example.util.KeystoreHelper

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("ai_call_assistant_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_PROVIDER = "selected_ai_provider"
        private const val KEY_SELECTED_MODEL = "selected_ai_model"
        private const val KEY_GROQ_API_KEY = "groq_api_key_secure"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key_secure"
        private const val KEY_OPENAI_API_KEY = "openai_api_key_secure"
        private const val KEY_OPENROUTER_API_KEY = "openrouter_api_key_secure"

        private const val KEY_AI_MODE = "ai_mode_active"
        private const val KEY_SYSTEM_PROMPT = "ai_system_prompt"
        private const val KEY_SPEECH_LANGUAGE = "speech_language"
        private const val KEY_SPEECH_VOLUME = "speech_volume"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_BATTERY_SAVING = "battery_saving_mode"
        
        private const val DEFAULT_PROMPT = "Siz AI Call Assistant – aqlli va muloyim ovozli yordamchisiz. Qo'ng'iroqlarga professional tarzda javob bering, foydalanuvchiga yordam bering va suhbatdosh bilan o'zaro hurmatda bo'ling."
    }

    var aiProvider: String
        get() = prefs.getString(KEY_SELECTED_PROVIDER, "groq") ?: "groq"
        set(value) = prefs.edit().putString(KEY_SELECTED_PROVIDER, value).apply()

    var aiModel: String
        get() = prefs.getString(KEY_SELECTED_MODEL, "llama-3.3-70b-versatile") ?: "llama-3.3-70b-versatile"
        set(value) = prefs.edit().putString(KEY_SELECTED_MODEL, value).apply()

    fun saveApiKey(apiKey: String) {
        saveApiKeyForProvider(apiKey, aiProvider)
    }

    fun getApiKey(): String {
        return getApiKeyForProvider(aiProvider)
    }

    fun saveApiKeyForProvider(apiKey: String, provider: String) {
        val prefKey = when (provider) {
            "gemini" -> KEY_GEMINI_API_KEY
            "openai" -> KEY_OPENAI_API_KEY
            "openrouter" -> KEY_OPENROUTER_API_KEY
            else -> KEY_GROQ_API_KEY
        }
        val encrypted = KeystoreHelper.encrypt(apiKey)
        prefs.edit().putString(prefKey, encrypted).apply()
    }

    fun getApiKeyForProvider(provider: String): String {
        val prefKey = when (provider) {
            "gemini" -> KEY_GEMINI_API_KEY
            "openai" -> KEY_OPENAI_API_KEY
            "openrouter" -> KEY_OPENROUTER_API_KEY
            else -> KEY_GROQ_API_KEY
        }
        val encrypted = prefs.getString(prefKey, "") ?: ""
        return if (encrypted.isNotEmpty()) {
            KeystoreHelper.decrypt(encrypted)
        } else {
            ""
        }
    }

    var aiModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_AI_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_AI_MODE, value).apply()

    var systemPrompt: String
        get() = prefs.getString(KEY_SYSTEM_PROMPT, DEFAULT_PROMPT) ?: DEFAULT_PROMPT
        set(value) = prefs.edit().putString(KEY_SYSTEM_PROMPT, value).apply()

    var speechLanguage: String
        get() = prefs.getString(KEY_SPEECH_LANGUAGE, "auto") ?: "auto"
        set(value) = prefs.edit().putString(KEY_SPEECH_LANGUAGE, value).apply()

    var speechVolume: Float
        get() = prefs.getFloat(KEY_SPEECH_VOLUME, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_VOLUME, value).apply()

    var speechRate: Float
        get() = prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_RATE, value).apply()

    var batterySavingMode: Boolean
        get() = prefs.getBoolean(KEY_BATTERY_SAVING, false)
        set(value) = prefs.edit().putBoolean(KEY_BATTERY_SAVING, value).apply()

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
