package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.LogEntity
import com.example.data.repository.CallAssistantRepository
import com.example.data.remote.GroqMessage
import com.example.service.CallAssistantService
import com.example.speech.SpeechManager
import com.example.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

data class ConversationTurn(
    val sender: String, // "Caller", "AI Assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class CallAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CallAssistantRepository(application)
    private val speechManager = SpeechManager(application)
    private val context: Context = application.applicationContext

    companion object {
        private const val TAG = "CallAssistantViewModel"
    }

    // Reactively expose Conversations
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allConversations
            } else {
                repository.searchConversations(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteConversations: StateFlow<List<ConversationEntity>> = repository.favoriteConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live System Logs
    val systemLogs: StateFlow<List<LogEntity>> = repository.getRecentLogs(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings Parameters
    val aiProvider = MutableStateFlow(repository.aiProvider)
    val aiModel = MutableStateFlow(repository.aiModel)
    val groqApiKey = MutableStateFlow(repository.getApiKeyForProvider("groq"))
    val geminiApiKey = MutableStateFlow(repository.getApiKeyForProvider("gemini"))
    val openaiApiKey = MutableStateFlow(repository.getApiKeyForProvider("openai"))
    val openrouterApiKey = MutableStateFlow(repository.getApiKeyForProvider("openrouter"))

    val aiModeEnabled = MutableStateFlow(repository.aiModeEnabled)
    val systemPrompt = MutableStateFlow(repository.systemPrompt)
    val speechLanguage = MutableStateFlow(repository.speechLanguage)
    val speechVolume = MutableStateFlow(repository.speechVolume)
    val speechRate = MutableStateFlow(repository.speechRate)
    val batterySavingMode = MutableStateFlow(repository.batterySavingMode)
    val apiKey = MutableStateFlow(repository.getApiKey())

    // Background Service Status
    val isServiceRunning = CallAssistantService.isRunning
    val currentCallState = CallAssistantService.callState
    val activeNumber = CallAssistantService.activeNumber

    // Simulated Call UI State
    val isCallActive = MutableStateFlow(false)
    val callDuration = MutableStateFlow(0)
    val simCallerName = MutableStateFlow("")
    val simCallerPhone = MutableStateFlow("")
    val simLanguage = MutableStateFlow("uz") // Uzbek, English, Russian
    val transcriptTurns = MutableStateFlow<List<ConversationTurn>>(emptyList())
    val isAiResponding = MutableStateFlow(false)
    val isSttListening = MutableStateFlow(false)
    val sttPartialText = MutableStateFlow("")
    val sttRmsLevel = MutableStateFlow(0f)

    // Diagnostics State
    val isInternetConnected = MutableStateFlow(true)
    val apiTestStatus = MutableStateFlow("TEST BOSHLANMAGAN")

    private var durationTimer: Timer? = null

    init {
        AppLogger.i(TAG, "CallAssistantViewModel initialized")
        checkConnectivity()
        
        // Listen to service updates and sync AI Mode
        viewModelScope.launch {
            isServiceRunning.collect { running ->
                aiModeEnabled.value = running
                repository.aiModeEnabled = running
            }
        }
    }

    // --- CONVERSATION UTILITIES ---

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(conversation: ConversationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = conversation.copy(isFavorite = !conversation.isFavorite)
            repository.updateConversation(updated)
            AppLogger.d(TAG, "Toggled favorite for chat ID: ${conversation.id}")
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteConversationById(id)
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllConversations()
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllLogs()
        }
    }

    // --- SETTINGS MANAGEMENT ---
    
    fun saveSettings(
        provider: String,
        model: String,
        groqKey: String,
        geminiKey: String,
        openaiKey: String,
        openrouterKey: String,
        prompt: String,
        lang: String,
        vol: Float,
        rate: Float,
        batterySave: Boolean
    ) {
        viewModelScope.launch {
            repository.aiProvider = provider
            repository.aiModel = model
            repository.saveApiKeyForProvider(groqKey, "groq")
            repository.saveApiKeyForProvider(geminiKey, "gemini")
            repository.saveApiKeyForProvider(openaiKey, "openai")
            repository.saveApiKeyForProvider(openrouterKey, "openrouter")
            
            repository.systemPrompt = prompt
            repository.speechLanguage = lang
            repository.speechVolume = vol
            repository.speechRate = rate
            repository.batterySavingMode = batterySave

            aiProvider.value = provider
            aiModel.value = model
            groqApiKey.value = groqKey
            geminiApiKey.value = geminiKey
            openaiApiKey.value = openaiKey
            openrouterApiKey.value = openrouterKey
            
            apiKey.value = when (provider) {
                "gemini" -> geminiKey
                "openai" -> openaiKey
                "openrouter" -> openrouterKey
                else -> groqKey
            }
            
            systemPrompt.value = prompt
            speechLanguage.value = lang
            speechVolume.value = vol
            speechRate.value = rate
            batterySavingMode.value = batterySave
            
            AppLogger.i(TAG, "Settings saved successfully")
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            repository.clearPreferences()
            aiProvider.value = "groq"
            aiModel.value = "llama-3.3-70b-versatile"
            groqApiKey.value = ""
            geminiApiKey.value = ""
            openaiApiKey.value = ""
            openrouterApiKey.value = ""
            apiKey.value = ""
            systemPrompt.value = repository.systemPrompt
            speechLanguage.value = "auto"
            speechVolume.value = 1.0f
            speechRate.value = 1.0f
            batterySavingMode.value = false
            AppLogger.w(TAG, "Settings have been reset to defaults")
        }
    }

    // --- SERVICE CONTROLS ---

    fun toggleAiMode(enabled: Boolean) {
        aiModeEnabled.value = enabled
        repository.aiModeEnabled = enabled
        
        val serviceIntent = Intent(context, CallAssistantService::class.java)
        if (enabled) {
            AppLogger.i(TAG, "Starting CallAssistant Foreground Service")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            AppLogger.w(TAG, "Stopping CallAssistant Foreground Service")
            context.stopService(serviceIntent)
        }
    }

    // --- ACTIVE CALL SIMULATOR (CORE FEATURE WORKFLOW) ---

    fun startCallSimulation(callerName: String, callerPhone: String, language: String) {
        if (isCallActive.value) return
        
        AppLogger.i(TAG, "Starting Call Simulation with $callerName ($callerPhone) in language: $language")
        
        isCallActive.value = true
        simCallerName.value = callerName
        simCallerPhone.value = callerPhone
        simLanguage.value = language
        transcriptTurns.value = emptyList()
        callDuration.value = 0

        // Start Call Timer
        durationTimer = Timer()
        durationTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                callDuration.value += 1
            }
        }, 1000, 1000)

        // Simulate incoming call speaking first
        val initialMessage = when (language.lowercase()) {
            "uz" -> "Salom! Men sizga telefon qilyapman. Meni eshityapsizmi?"
            "ru" -> "Привет! Я звоню вам. Вы меня слышите?"
            else -> "Hello! I am calling you. Can you hear me?"
        }

        viewModelScope.launch {
            delay(1000) // Aesthetic delay for realistic timing
            addTranscriptTurn("Caller", initialMessage)
            speakText(initialMessage)
        }
    }

    fun endCallSimulation() {
        if (!isCallActive.value) return
        AppLogger.i(TAG, "Ending Call Simulation")

        durationTimer?.cancel()
        durationTimer = null

        speechManager.stopSpeaking()
        speechManager.cancelListening()
        
        isCallActive.value = false
        isSttListening.value = false

        // Save conversation history to local SQLite Room database
        val turns = transcriptTurns.value
        if (turns.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val transcriptJson = serializeTurns(turns)
                val entity = ConversationEntity(
                    callerName = simCallerName.value,
                    phoneNumber = simCallerPhone.value,
                    language = simLanguage.value,
                    transcriptJson = transcriptJson,
                    durationSeconds = callDuration.value
                )
                repository.insertConversation(entity)
                AppLogger.i(TAG, "Call transcription successfully saved to local database")
            }
        }
    }

    fun addTranscriptTurn(sender: String, text: String) {
        val current = transcriptTurns.value.toMutableList()
        current.add(ConversationTurn(sender, text))
        transcriptTurns.value = current
    }

    // Speech synthesis wrapper
    fun speakText(text: String, onFinished: (() -> Unit)? = null) {
        speechManager.speak(
            text = text,
            languageCode = simLanguage.value,
            volume = speechVolume.value,
            speechRate = speechRate.value,
            onDone = {
                Handler(Looper.getMainLooper()).post {
                    onFinished?.invoke()
                }
            }
        )
    }

    // Speech recognizer wrapper
    fun startListeningToCaller() {
        if (isSttListening.value || isAiResponding.value) return
        
        AppLogger.d(TAG, "STT: Start Listening requested")
        sttPartialText.value = ""
        speechManager.startListening(
            languageCode = simLanguage.value,
            onActive = { listening ->
                isSttListening.value = listening
            },
            onPartialResult = { partial ->
                sttPartialText.value = partial
            },
            onResult = { fullResult ->
                sttPartialText.value = ""
                if (fullResult.isNotBlank()) {
                    addTranscriptTurn("Caller", fullResult)
                    // Automatically trigger Groq Chat Completion for Response!
                    generateAiResponse(fullResult)
                }
            },
            onError = { error ->
                sttPartialText.value = ""
                AppLogger.e(TAG, "STT recognition error callback: $error")
            }
        )
    }

    fun stopListeningToCaller() {
        speechManager.stopListening()
    }

    private fun generateAiResponse(callerQuery: String) {
        if (isAiResponding.value) return
        isAiResponding.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Prepare conversation history for conversational context
                val historyList = transcriptTurns.value.takeLast(10).map {
                    val role = if (it.sender == "AI Assistant") "assistant" else "user"
                    GroqMessage(role = role, content = it.text)
                }

                // Call remote Groq Completions API
                val response = repository.getGroqCompletion(
                    userMessage = callerQuery,
                    history = historyList
                )

                // Render Response
                viewModelScope.launch(Dispatchers.Main) {
                    isAiResponding.value = false
                    addTranscriptTurn("AI Assistant", response)
                    speakText(response) {
                        // After AI finishes speaking, open Speech recognition automatically for interactive call!
                        startListeningToCaller()
                    }
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    isAiResponding.value = false
                    val errorReply = "Kechirasiz, aloqa xatosi tufayli javob berolmayapman: ${e.localizedMessage}"
                    addTranscriptTurn("AI Assistant", errorReply)
                    speakText(errorReply)
                }
            }
        }
    }

    // --- DIAGNOSTICS & SYSTEM CHECKS ---

    fun checkConnectivity() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        isInternetConnected.value = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        AppLogger.d(TAG, "Connectivity status: ${isInternetConnected.value}")
    }

    fun testApiConnection() {
        apiTestStatus.value = "KUTILMOQDA..."
        AppLogger.i(TAG, "Initiating API diagnostic test connection")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.getGroqCompletion("Bu test xabari, qisqa 'OK' deb javob bering.")
                viewModelScope.launch(Dispatchers.Main) {
                    apiTestStatus.value = "MUVAFFAQIYATLI: $result"
                    AppLogger.i(TAG, "API diagnostic test succeeded")
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    apiTestStatus.value = "XATO: ${e.localizedMessage}"
                    AppLogger.e(TAG, "API diagnostic test failed: ${e.localizedMessage}")
                }
            }
        }
    }

    // --- CONVERSATION SERIALIZATION HELPERS ---

    private fun serializeTurns(turns: List<ConversationTurn>): String {
        val sb = java.lang.StringBuilder()
        sb.append("[")
        for (i in turns.indices) {
            val t = turns[i]
            val cleanText = t.text.replace("\"", "\\\"").replace("\n", " ")
            sb.append("{\"sender\":\"${t.sender}\",\"text\":\"$cleanText\",\"timestamp\":${t.timestamp}}")
            if (i < turns.size - 1) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    fun deserializeTurns(json: String): List<ConversationTurn> {
        val list = mutableListOf<ConversationTurn>()
        try {
            val pattern = java.util.regex.Pattern.compile("\\{\"sender\":\"(.*?)\",\"text\":\"(.*?)\",\"timestamp\":(\\d+)\\}")
            val matcher = pattern.matcher(json)
            while (matcher.find()) {
                val sender = matcher.group(1) ?: ""
                val text = matcher.group(2) ?: ""
                val timestamp = matcher.group(3)?.toLong() ?: System.currentTimeMillis()
                list.add(ConversationTurn(sender, text, timestamp))
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error deserializing turns JSON: ${e.localizedMessage}")
        }
        return list
    }

    fun exportChatAsText(conversation: ConversationEntity): File? {
        val turns = deserializeTurns(conversation.transcriptJson)
        if (turns.isEmpty()) return null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fileDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(conversation.timestamp))
        val fileName = "AI_Call_${conversation.phoneNumber}_$fileDate.txt"
        
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            
            val file = File(exportDir, fileName)
            val writer = FileWriter(file)
            
            writer.write("=========================================\n")
            writer.write("AI CALL ASSISTANT - EXPORTED TRANSCRIPT\n")
            writer.write("=========================================\n")
            writer.write("Suhbatdosh: ${conversation.callerName}\n")
            writer.write("Telefon: ${conversation.phoneNumber}\n")
            writer.write("Sana: ${dateFormat.format(Date(conversation.timestamp))}\n")
            writer.write("Davomiyligi: ${conversation.durationSeconds} soniya\n")
            writer.write("Tili: ${conversation.language.uppercase()}\n")
            writer.write("=========================================\n\n")

            for (turn in turns) {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(turn.timestamp))
                writer.write("[$time] ${turn.sender}: ${turn.text}\n\n")
            }
            
            writer.flush()
            writer.close()
            AppLogger.i(TAG, "Chat exported to cache file: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error exporting chat file: ${e.localizedMessage}")
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
        durationTimer?.cancel()
    }
}
