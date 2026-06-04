package com.example.localization

import android.content.Context
import com.example.R

object MedicalTranslationEngine {
    fun getMedicationString(context: Context, keyResId: Int): String {
        return context.getString(keyResId)
    }
}
