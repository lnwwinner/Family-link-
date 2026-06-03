package com.example.domain.medical

data class MedicalFacility(
    val id: String,
    val name: String,
    val type: String, // "Hospital", "Clinic", "Emergency Center"
    val phoneNumber: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val is24Hours: Boolean,
    val distanceKm: Double,
    val estimatedMinutes: Int
)
