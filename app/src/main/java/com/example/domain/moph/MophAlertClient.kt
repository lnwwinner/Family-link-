package com.example.domain.moph

interface MophAlertClient {
    // API Call จากคู่มือ
    suspend fun sendNotification(message: String): Boolean
}
