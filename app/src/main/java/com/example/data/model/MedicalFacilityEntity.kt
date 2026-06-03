package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_facilities")
data class MedicalFacilityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val phone: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val is24Hours: Boolean
)
