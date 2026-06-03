package com.example.data.medical

import com.example.domain.medical.MedicalFacility

interface MedicalFacilityRepository {
    fun getNearbyFacilities(lat: Double, lon: Double): List<MedicalFacility>
    fun getEmergencyCapabilities(facilityId: String): Boolean
}

class MockMedicalFacilityRepository : MedicalFacilityRepository {
    override fun getNearbyFacilities(lat: Double, lon: Double): List<MedicalFacility> {
        return listOf(
            MedicalFacility("1", "Bangkok General Hospital", "Hospital", "02-123-4567", "Bangkok, Thailand", 13.75, 100.5, true, 2.5, 10),
            MedicalFacility("2", "Community Clinic A", "Clinic", "02-987-6543", "Bangkok, Thailand", 13.76, 100.51, false, 3.2, 15),
            MedicalFacility("3", "Emergency Center B", "Emergency Center", "02-555-0199", "Bangkok, Thailand", 13.74, 100.49, true, 1.8, 8)
        )
    }

    override fun getEmergencyCapabilities(facilityId: String): Boolean = true
}
