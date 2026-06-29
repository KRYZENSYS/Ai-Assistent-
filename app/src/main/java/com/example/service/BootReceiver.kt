package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.PreferencesManager
import com.example.util.AppLogger

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            AppLogger.i("BootReceiver", "Device boot completed, checking auto-start preference")
            
            val prefs = PreferencesManager(context)
            if (prefs.aiModeEnabled) {
                AppLogger.i("BootReceiver", "AI Mode was active. Starting CallAssistantService...")
                try {
                    val serviceIntent = Intent(context, CallAssistantService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    AppLogger.e("BootReceiver", "Failed to auto-start service after boot", e)
                }
            } else {
                AppLogger.d("BootReceiver", "AI Mode was disabled, ignoring boot event")
            }
        }
    }
}
