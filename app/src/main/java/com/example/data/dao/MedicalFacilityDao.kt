package com.example.data.dao

import androidx.room.*
import com.example.data.model.MedicalFacilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalFacilityDao {
    @Query("SELECT * FROM medical_facilities")
    fun getAll(): Flow<List<MedicalFacilityEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(facilities: List<MedicalFacilityEntity>)
}
