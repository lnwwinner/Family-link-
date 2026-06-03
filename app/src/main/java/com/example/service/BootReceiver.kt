package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        
        val action = intent.action
        Log.i("BootReceiver", "Boot broadcast received: action=$action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.w("BootReceiver", "AUTORESTART: Launching EmergencyForegroundService on boot startup")
            
            val serviceIntent = Intent(context, EmergencyForegroundService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.i("BootReceiver", "Emergency watchdog service fired up during boot cycle successfully")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to launch service: ${e.message}", e)
            }
        }
    }
}
