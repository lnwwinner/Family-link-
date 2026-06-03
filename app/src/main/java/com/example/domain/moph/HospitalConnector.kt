package com.example.domain.moph

interface HospitalConnector {
    suspend fun getRecords(nationalId: String): List<Any> // Simplified
}
