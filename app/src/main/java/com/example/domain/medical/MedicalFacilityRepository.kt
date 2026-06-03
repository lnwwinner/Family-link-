package com.example.domain.medical

interface MedicalFacilityRepository {
    fun getNearbyFacilities(lat: Double, lon: Double): List<MedicalFacility>
    fun getEmergencyCapabilities(facilityId: String): Boolean
}
