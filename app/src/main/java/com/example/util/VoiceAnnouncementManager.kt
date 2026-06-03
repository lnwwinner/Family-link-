package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object VoiceAnnouncementManager {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // Set language to Thai
                    tts?.language = Locale("th", "TH")
                    isInitialized = true
                }
            }
        }
    }

    fun announce(activity: String) {
        if (isInitialized) {
            tts?.speak("แจ้งเตือนกิจกรรม: $activity", TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        isInitialized = false
    }
}
