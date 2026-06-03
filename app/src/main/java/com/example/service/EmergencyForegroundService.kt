package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class EmergencyForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private val notificationId = 1912

    private val hardwareSosReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.ACTION_HARDWARE_SOS") {
                val source = intent.getStringExtra("source") ?: "Unknown Hardware Button"
                Log.e("EmergencyService", "Broadcasting Emergency SOS triggered from: $source")
                Toast.makeText(context, "FOREGROUND MONITOR: ACTION HARDWARE SOS KEY ($source)!", Toast.LENGTH_LONG).show()
                
                // Let's launch MainActivity or trigger a custom network dispatch
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("TRIGGER_SOS_AUTOMATICALLY", true)
                    putExtra("sos_source", source)
                }
                startActivity(launchIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("EmergencyService", "Creating Foreground Emergency Guard")
        createHeadsUpNotificationChannel()
        
        // Register receiver for high fidelity key actions
        val filter = IntentFilter("com.example.ACTION_HARDWARE_SOS")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(hardwareSosReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(hardwareSosReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("EmergencyService", "Starting Persistent Service Protection Flow")
        
        // Acquire CPU Wakelock to safeguard life checks on sleep
        acquireCpuWakeLock()

        // Display immediate heads-up status bar notification to indicate active background protection
        val notification = createPersistentNotification()
        startForeground(notificationId, notification)

        return START_STICKY // Safe automatic restart when cleared from UI stack
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun acquireCpuWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FamilyCare::EmergencyLockTag").apply {
                    acquire(10 * 60 * 1000L /*10 minutes limit*/)
                }
                Log.i("EmergencyService", "WakeLock acquired successfully")
            }
        } catch (e: Exception) {
            Log.e("EmergencyService", "Failed to acquire CPU wakeLock: ${e.message}")
        }
    }

    private fun releaseCpuWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            wakeLock = null
            Log.i("EmergencyService", "WakeLock released")
        } catch (e: Exception) {
            Log.e("EmergencyService", "Failed to release wakeLock")
        }
    }

    private fun createHeadsUpNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "emergency_guard_channel",
                "Grandpa Protection Active Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Monitors real-time fall impact sensors, bluetooth watch parameters, and accessibility triggers."
                enableVibration(true)
                setShowBadge(true)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createPersistentNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "emergency_guard_channel")
            .setContentTitle("FAMILY CARE GUARD IS ACTIVE")
            .setContentText("Keeping active watch on fall sensors, Smartwatch SpO2/ECG status, and hardware hotkeys")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Grandpa Sompong's location and active smartwatch vitals are constantly linked to family members. Triple press power button to broadcast emergency SOS immediately."))
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w("EmergencyService", "Service Destroy Called, rebooting backup trigger")
        releaseCpuWakeLock()
        unregisterReceiver(hardwareSosReceiver)
        
        // Auto-restart hook: Trigger intent broadcast to restart after removal
        val restartServiceIntent = Intent(applicationContext, this.javaClass).apply {
            setPackage(packageName)
        }
        try {
            startService(restartServiceIntent)
        } catch (e: Exception) {
            Log.e("EmergencyService", "Could not execute self-restart: ${e.message}")
        }
    }
}
