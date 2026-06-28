package com.example.util

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppLogger {
    private var database: AppDatabase? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(db: AppDatabase) {
        database = db
    }

    private fun writeLog(level: String, tag: String, message: String) {
        val db = database
        if (db != null) {
            scope.launch {
                try {
                    db.logDao().insertLog(LogEntity(tag = tag, level = level, message = message))
                } catch (e: Exception) {
                    // Fail gracefully to prevent infinite loops
                }
            }
        }
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
        writeLog("DEBUG", tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
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
}
