package com.example.util

import android.content.Context
import android.content.Intent
import com.example.service.FloatingSOSService

/**
 * Manages the lifecycle and state of overlay services.
 */
object OverlayManager {

    fun toggleSOSOverlay(context: Context, enabled: Boolean) {
        val intent = Intent(context, FloatingSOSService::class.java)
        if (enabled) {
            context.startService(intent)
        } else {
            context.stopService(intent)
        }
    }
}
