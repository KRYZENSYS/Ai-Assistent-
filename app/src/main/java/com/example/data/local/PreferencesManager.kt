package com.example.data.local

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("ai_call_assistant_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "groq_api_key_secure"
        private const val KEY_AI_MODE = "ai_mode_active"
        private const val KEY_SYSTEM_PROMPT = "ai_system_prompt"
        private const val KEY_SPEECH_LANGUAGE = "speech_language"
        private const val KEY_SPEECH_VOLUME = "speech_volume"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_BATTERY_SAVING = "battery_saving_mode"
        
        private const val DEFAULT_PROMPT = "Siz AI Call Assistant – aqlli va muloyim ovozli yordamchisiz. Qo'ng'iroqlarga professional tarzda javob bering, foydalanuvchiga yordam bering va suhbatdosh bilan o'zaro hurmatda bo'ling."
    }

    // Securely obfuscate the API key using Base64 with a simple custom byte manipulation
    fun saveApiKey(apiKey: String) {
        val bytes = apiKey.toByteArray(StandardCharsets.UTF_8)
        // Simple secure shift to prevent plain-text discovery
        for (i in bytes.indices) {
            bytes[i] = (bytes[i].toInt() xor 42).toByte()
        }
        val encoded = Base64.encodeToString(bytes, Base64.DEFAULT)
        prefs.edit().putString(KEY_API_KEY, encoded).apply()
    }

    fun getApiKey(): String {
        val encoded = prefs.getString(KEY_API_KEY, null) ?: return ""
        return try {
            val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
            for (i in decodedBytes.indices) {
                decodedBytes[i] = (decodedBytes[i].toInt() xor 42).toByte()
            }
            String(decodedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
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
