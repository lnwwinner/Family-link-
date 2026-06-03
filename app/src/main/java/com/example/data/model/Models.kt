package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String, // "Elderly", "Family Member", "Caregiver"
    val groupCode: String? = null
)

@Entity(tableName = "emergency_logs")
data class EmergencyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val elderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val isResolved: Boolean = false,
    val resolvedBy: String? = null,
    val alertMessage: String = "EMERGENCY SOS TRIGGERED"
) {
    val googleMapsUrl: String
        get() = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
}

@Entity(tableName = "voice_messages")
data class VoiceMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val elderName: String,
    val filepath: String,
    val durationSec: Int,
    val timestamp: Long = System.currentTimeMillis(),
    // Analytical fields ready for future Voice Health Analysis
    val distressLevel: String = "Normal", // "Normal", "Slight distress", "Severe distress"
    val detectedPulseBpm: Int = 0, // Prepared for sensor/voice analytical data matching
    val comments: String = "No remarks"
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String, // e.g. "1 Pill", "10ml"
    val timeOfDay: String, // e.g. "08:00 AM", "08:00 PM"
    val description: String = "",
    val isDaily: Boolean = true
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val medName: String,
    val intakeTimestamp: Long = System.currentTimeMillis(),
    val isTaken: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "family_groups")
data class FamilyGroup(
    @PrimaryKey val code: String, // Dynamic generated code, e.g. "FAM-9104"
    val groupName: String,
    val ownerId: String
)

@Entity(tableName = "watch_health_data")
data class WatchHealthData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val watchType: String, // "Wear OS", "Samsung Galaxy Watch", "Xiaomi Watch", "Huawei Watch", "Amazfit"
    val timestamp: Long = System.currentTimeMillis(),
    val heartRate: Int,
    val oxygenLevel: Int, // SpO2 %
    val sleepDurationHours: Double,
    val sleepQuality: String, // "Deep & Healthy", "Light & Restless", "REM Cycle Peak"
    val stressLevel: Int, // 1-100
    val bloodPressureSystolic: Int,
    val bloodPressureDiastolic: Int,
    val ecgResult: String, // "Normal Sinus Rhythm", "Inconclusive Beats", "Atrial Fibrillation Detection"
    val batteryLevel: Int,
    val connectionStatus: String = "Connected", // "Connected", "Syncing", "Disconnected"
    val isSynced: Boolean = false
)
