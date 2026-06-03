package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

object BatteryOptimizationHelper {

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isAlreadyIgnored = isIgnoringBatteryOptimizations(context)
            Log.d("BatteryOptimization", "Checking battery status: ignoring=$isAlreadyIgnored")
            
            if (!isAlreadyIgnored) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Log.i("BatteryOptimization", "Dispatched Settings intent to ignore background restrictions")
                } catch (e: Exception) {
                    Log.e("BatteryOptimization", "Fail to request battery ignore intent, redirecting to general settings: ${e.message}")
                    try {
                        val generalIntent = Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(generalIntent)
                    } catch (ex: Exception) {
                        Log.e("BatteryOptimization", "Absolute failure opening settings activity")
                    }
                }
            }
        }
    }
}
