package com.example.util

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppLogger {
    private var database: AppDatabase? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val IS_DEBUG = BuildConfig.DEBUG

    fun initialize(db: AppDatabase) {
        database = db
        i("AppLogger", "Logger initialized in ${if (IS_DEBUG) "DEBUG" else "RELEASE"} mode")
    }

    private fun writeLog(level: String, tag: String, message: String) {
        val cleanMessage = scrubSensitiveData(message)
        
        val db = database
        if (db != null) {
            scope.launch {
                try {
                    db.logDao().insertLog(LogEntity(tag = tag, level = level, message = cleanMessage))
                } catch (e: Exception) {
                    // Fail gracefully to prevent infinite loops
                }
            }
        }
    }

    fun d(tag: String, msg: String) {
        if (IS_DEBUG) {
            Log.d(tag, msg)
            writeLog("DEBUG", tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (IS_DEBUG) {
            Log.i(tag, msg)
        }
        writeLog("INFO", tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
        writeLog("WARN", tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        val fullMsg = if (tr != null) "$msg\n${Log.getStackTraceString(tr)}" else msg
        Log.e(tag, fullMsg)
        writeLog("ERROR", tag, fullMsg)
    }

    private fun scrubSensitiveData(message: String): String {
        var clean = message
        if (clean.contains("Bearer", ignoreCase = true)) {
            clean = clean.replace(Regex("Bearer\\s+[a-zA-Z0-9_\\-\\.\\s=]+", RegexOption.IGNORE_CASE), "Bearer [REDACTED_SENSITIVE]")
        }
        if (clean.contains("key", ignoreCase = true)) {
            clean = clean.replace(Regex("(key|api_key|gsk)[a-zA-Z0-9_\\-\\:\\s=]{5,}", RegexOption.IGNORE_CASE), "[REDACTED_SENSITIVE]")
        }
        return clean
    }

    fun reportCrash(throwable: Throwable) {
        e("CrashReporter", "FATAL CRASH intercepted: ${throwable.localizedMessage}", throwable)
    }

    fun reportError(tag: String, message: String, exception: Exception? = null) {
        e(tag, "Non-fatal error: $message", exception)
    }
}
