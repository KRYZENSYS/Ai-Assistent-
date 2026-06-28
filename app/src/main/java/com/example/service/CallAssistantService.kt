package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow

class CallAssistantService : Service() {

    companion object {
        private const val TAG = "CallAssistantService"
        private const val CHANNEL_ID = "CallAssistantServiceChannel"
        private const val NOTIFICATION_ID = 4242

        // Reactively exposed states for UI observation
        val isRunning = MutableStateFlow(false)
        val callState = MutableStateFlow("IDLE") // "IDLE", "RINGING", "OFFHOOK"
        val activeNumber = MutableStateFlow("")
    }

    private var callReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        AppLogger.i(TAG, "Foreground Service creating")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("AI Call monitoring is active"))
        registerCallReceiver()
        isRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.d(TAG, "Foreground Service onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Binding not required, we use reactive flows
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.w(TAG, "Foreground Service stopping")
        unregisterCallReceiver()
        isRunning.value = false
        callState.value = "IDLE"
        activeNumber.value = ""
    }

    private fun registerCallReceiver() {
        callReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
                
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "Noma'lum"

                AppLogger.i(TAG, "Call State Changed: State=$state, Number=$incomingNumber")

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        callState.value = "RINGING"
                        activeNumber.value = incomingNumber
                        updateNotification("Kiruvchi qo'ng'iroq: $incomingNumber")
                        
                        // Broadcast to launch overlay or notify app UI
                        val notifyIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("LAUNCH_CALL_OVERLAY", true)
                            putExtra("INCOMING_NUMBER", incomingNumber)
                        }
                        context?.startActivity(notifyIntent)
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        callState.value = "OFFHOOK"
                        updateNotification("Suhbat faol: $incomingNumber")
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        callState.value = "IDLE"
                        activeNumber.value = ""
                        updateNotification("AI Call monitoring is active")
                    }
                }
            }
        }

        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(callReceiver, filter)
        AppLogger.d(TAG, "Call State BroadcastReceiver registered dynamically")
    }

    private fun unregisterCallReceiver() {
        callReceiver?.let {
            try {
                unregisterReceiver(it)
                AppLogger.d(TAG, "Call State BroadcastReceiver unregistered")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error unregistering receiver", e)
            }
        }
        callReceiver = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "AI Call Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs Call Monitoring in background to intercept incoming states."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Call Assistant")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call) // Safe fallback icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(text))
    }
}
