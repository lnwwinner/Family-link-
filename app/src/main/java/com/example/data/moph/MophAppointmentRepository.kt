package com.example.data.moph

import com.example.domain.moph.AppointmentRepository
import com.example.domain.moph.HospitalAppointment

class MophAppointmentRepository : AppointmentRepository {
    override suspend fun getAppointments(userId: String): List<HospitalAppointment> {
        // Mock implementation
        return emptyList()
    }

    override suspend fun syncAppointments(userId: String) {
        // Implementation for MOPH API call would go here
    }
}
