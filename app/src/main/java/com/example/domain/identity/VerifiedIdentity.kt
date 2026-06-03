package com.example.domain.identity

import java.time.LocalDateTime

enum class IdentityLevel {
    UNVERIFIED,
    BASIC_EMAIL,
    VERIFIED_THAID
}

data class VerifiedIdentity(
    val userId: String,
    val fullName: String,
    val nationalIdHash: String, // เก็บเฉพาะ Hash
    val identityLevel: IdentityLevel,
    val verifiedAt: LocalDateTime
)

data class CaregiverVerificationStatus(
    val isVerified: Boolean,
    val verifierId: String,
    val verifiedAt: LocalDateTime
)
