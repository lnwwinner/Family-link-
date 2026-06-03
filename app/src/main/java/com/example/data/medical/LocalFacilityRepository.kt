package com.example.data.medical

import com.example.data.dao.MedicalFacilityDao
import com.example.data.model.MedicalFacilityEntity
import com.example.domain.medical.MedicalFacility
import com.example.domain.medical.MedicalFacilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalFacilityRepository(
    private val dao: MedicalFacilityDao
) : MedicalFacilityRepository {

    // Note: The original interface used `fun getNearbyFacilities` (non-suspend)
    // We'll need to adapt this or change the interface if we want to use Flow/suspend
    
    // For now, let's keep it simple
    override fun getNearbyFacilities(lat: Double, lon: Double): List<MedicalFacility> {
        // Implementation would use the DAO and filtering logic
        return emptyList()
    }

    override fun getEmergencyCapabilities(facilityId: String): Boolean = true
    
    suspend fun syncFacilities(facilities: List<MedicalFacility>) {
        val entities = facilities.map {
            MedicalFacilityEntity(it.id, it.name, it.type, it.phoneNumber, it.address, it.latitude, it.longitude, it.is24Hours)
        }
        dao.insertAll(entities)
    }
}
