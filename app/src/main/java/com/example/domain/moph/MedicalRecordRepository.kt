package com.example.domain.moph

interface MedicalRecordRepository {
    suspend fun syncMedicalRecords(userId: String)
}
