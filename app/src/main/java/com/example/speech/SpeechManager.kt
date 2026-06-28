package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.util.AppLogger
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "SpeechManager"
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    // Callbacks for Speech-to-Text
    private var onSttResultListener: ((String) -> Unit)? = null
    private var onSttPartialResultListener: ((String) -> Unit)? = null
    private var onSttErrorListener: ((String) -> Unit)? = null
    private var onSttActiveListener: ((Boolean) -> Unit)? = null

    // Callbacks for Text-to-Speech
    private var onTtsDoneListener: (() -> Unit)? = null

    init {
        // Initialize TTS
        tts = TextToSpeech(context, this)
        
        // Initialize STT on the Main thread (SpeechRecognizer requires Main thread)
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                setupSttIntent()
                setupSttListener()
                AppLogger.i(TAG, "Speech Recognizer initialized successfully")
            } else {
                AppLogger.w(TAG, "Speech Recognition is NOT available on this device")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to initialize SpeechRecognizer: ${e.localizedMessage}", e)
        }
    }

    // --- TEXT TO SPEECH (TTS) ---

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            AppLogger.i(TAG, "TextToSpeech engine initialized successfully")
            
            // Set default language
            setLanguage("en") // Default fallback

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    AppLogger.d(TAG, "TTS Speech started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    AppLogger.d(TAG, "TTS Speech completed: $utteranceId")
                    onTtsDoneListener?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    AppLogger.e(TAG, "TTS Speech error: $utteranceId")
                }
            })
        } else {
            AppLogger.e(TAG, "Failed to initialize TextToSpeech engine. Status: $status")
        }
    }

    fun speak(text: String, languageCode: String = "auto", volume: Float = 1.0f, speechRate: Float = 1.0f, onDone: (() -> Unit)? = null) {
        if (!isTtsReady || tts == null) {
            AppLogger.w(TAG, "TTS is not ready yet. Speech ignored: $text")
            return
        }

        this.onTtsDoneListener = onDone

        // Apply Speech parameters
        tts?.setSpeechRate(speechRate)
        // Note: setVolume is not directly available on some engines, we handle it natively via engine params
        val params = Bundle()
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)

        // Select Speech language
        val targetLocale = when (languageCode.lowercase()) {
            "uz" -> Locale("uz") // Uzbek
            "ru" -> Locale("ru", "RU") // Russian
            "en" -> Locale.US // English
            else -> detectLanguage(text) // Dynamic auto detection
        }
        
        AppLogger.i(TAG, "TTS playing in language: ${targetLocale.displayLanguage}")
        tts?.language = targetLocale
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "Utterance_Call_Assistant")
    }

    fun stopSpeaking() {
        if (isTtsReady) {
            tts?.stop()
        }
    }

    private fun detectLanguage(text: String): Locale {
        // Advanced client-side linguistic auto-detection heuristics
        val lowercaseText = text.lowercase()
        return when {
            // Check for Uzbek language markers
            lowercaseText.contains("salom") || lowercaseText.contains("rahmat") || 
            lowercaseText.contains("qanday") || lowercaseText.contains("aka") || 
            lowercaseText.contains("ovozli") || lowercaseText.contains("ha") || 
            lowercaseText.contains("yo'q") || lowercaseText.contains("bormi") -> Locale("uz")
            
            // Check for Russian language markers (Cyrillic characters range)
            lowercaseText.any { it in '\u0400'..'\u04FF' } -> Locale("ru", "RU")
            
            // Standard fallback is English
            else -> Locale.US
        }
    }

    fun setLanguage(langCode: String): Boolean {
        if (!isTtsReady || tts == null) return false
        val locale = when (langCode.lowercase()) {
            "uz" -> Locale("uz")
            "ru" -> Locale("ru", "RU")
            "en" -> Locale.US
            else -> Locale.getDefault()
        }
        val result = tts?.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    // --- SPEECH TO TEXT (STT) ---

    private fun setupSttIntent() {
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    private fun setupSttListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                AppLogger.d(TAG, "STT: Ready for speech")
                onSttActiveListener?.invoke(true)
            }

            override fun onBeginningOfSpeech() {
                AppLogger.d(TAG, "STT: Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // RMS amplitude changed (useful for animating visual waveforms)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                AppLogger.d(TAG, "STT: Speech ended")
                onSttActiveListener?.invoke(false)
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions missing"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server-side error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                    else -> "Unknown speech recognizer error"
                }
                AppLogger.w(TAG, "STT Error ($error): $errorMsg")
                onSttErrorListener?.invoke(errorMsg)
                onSttActiveListener?.invoke(false)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val speechText = matches[0]
                    AppLogger.d(TAG, "STT Full Result: $speechText")
                    onSttResultListener?.invoke(speechText)
                } else {
                    onSttErrorListener?.invoke("Hech qanday so'z aniqlanmadi")
                }
                onSttActiveListener?.invoke(false)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val partialText = matches[0]
                    AppLogger.d(TAG, "STT Partial Result: $partialText")
                    onSttPartialResultListener?.invoke(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening(
        languageCode: String = "auto",
        onActive: (Boolean) -> Unit,
        onPartialResult: (String) -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (speechRecognizer == null) {
            onError("Speech Recognizer is not available on this device")
            return
        }

        this.onSttActiveListener = onActive
        this.onSttPartialResultListener = onPartialResult
        this.onSttResultListener = onResult
        this.onSttErrorListener = onError

        val intent = recognizerIntent ?: return
        val targetLang = when (languageCode.lowercase()) {
            "uz" -> "uz-UZ"
            "ru" -> "ru-RU"
            "en" -> "en-US"
            else -> Locale.getDefault().toString()
        }
        
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLang)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, targetLang)
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, targetLang)
        
        AppLogger.i(TAG, "STT listening started in language: $targetLang")
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        onSttActiveListener?.invoke(false)
    }

    fun cancelListening() {
        speechRecognizer?.cancel()
        onSttActiveListener?.invoke(false)
    }

    fun shutdown() {
        try {
            stopSpeaking()
            tts?.shutdown()
            speechRecognizer?.destroy()
            AppLogger.i(TAG, "SpeechManager components shut down successfully")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error shutting down SpeechManager: ${e.localizedMessage}")
        }
    }
}
