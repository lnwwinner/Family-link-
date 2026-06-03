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
    @PrimaryKey val groupId: String,
    val groupName: String,
    val groupCode: String,
    val ownerId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val memberId: Int = 0,
    val groupId: String,
    val userId: String,
    val role: String, // PATIENT, ADMIN, MEMBER, CAREGIVER, MEDICAL_CONTACT, EMERGENCY_CONTACT
    val relationship: String,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "join_requests")
data class JoinRequest(
    @PrimaryKey(autoGenerate = true) val requestId: Int = 0,
    val groupId: String,
    val userId: String,
    val status: String, // PENDING, APPROVED, REJECTED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trusted_devices")
data class TrustedDevice(
    @PrimaryKey val deviceId: String,
    val userId: String,
    val deviceName: String,
    val isTrusted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
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

@Entity(tableName = "hospital_appointments")
data class HospitalAppointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String = "Sompong Somdee",
    val hospitalName: String,
    val doctorName: String,
    val department: String,
    val appointmentTimestamp: Long, // Epoch millis
    val reminderMinutesBefore: Int = 60,
    val isReminderSet: Boolean = true,
    val notes: String = "",
    val qrCodeData: String? = null,
    val documentPath: String? = null,
    val ocrExtractedText: String? = null,
    val isFamilyShared: Boolean = true,
    val isSynced: Boolean = false,
    val treatmentSuggested: String = "",
    val diagnosis: String = ""
)

@Entity(tableName = "medical_documents")
data class MedicalDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appointmentId: Int? = null,
    val title: String,
    val documentType: String, // "Lab Report", "Prescription", "X-Ray Diagnosis", "Appointment Slip", "Other"
    val filePath: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val extractedDetails: String? = null,
    val isSynced: Boolean = false,
    val fileSizeKb: Int = 345
)

@Entity(tableName = "emergency_profiles")
data class EmergencyProfile(
    @PrimaryKey val id: Int = 1,
    val patientName: String = "Sompong Somdee",
    val bloodType: String = "O+",
    val allergies: String = "Sulfide Antibiotics, Penicillin",
    val chronicConditions: String = "Hypertension, Stage-2 Diabetes, Arrhythmia",
    val regularPrescriptions: String = "Aspirin 81mg (Daily), Metformin 500mg, Atorvastatin 20mg",
    val emergencyContactName: String = "Suda Somdee",
    val emergencyContactPhone: String = "081-234-5678",
    val hospitalPreference: String = "Bangkok General Hospital",
    val insuranceDetails: String = "AIA Health Lifetime - Policy #912-88X-CC",
    val additionalNotes: String = "Always check heartbeat levels before administering standard anesthesia."
)

