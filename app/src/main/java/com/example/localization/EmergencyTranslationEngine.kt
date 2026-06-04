package com.example.localization

import android.content.Context
import com.example.R

object EmergencyTranslationEngine {
    fun getEmergencyMessage(context: Context): String {
        return context.getString(R.string.emergency_sos)
    }
}
