package com.example.domain.moph

// ตารางเวลาการนัดหมาย
data class HospitalAppointment(
    val id: String,
    val hospitalName: String,
    val department: String,
    val dateTime: java.time.LocalDateTime,
    val status: String
)

interface AppointmentRepository {
    suspend fun getAppointments(userId: String): List<HospitalAppointment>
    suspend fun syncAppointments(userId: String)
}
