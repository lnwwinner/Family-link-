package com.example.service

import com.example.domain.auth.AuthResult
import com.example.domain.auth.IdentityProvider
import com.example.domain.identity.IdentityLevel
import com.example.domain.identity.VerifiedIdentity
import java.time.LocalDateTime
import java.util.UUID

class ThaiDAuthService : IdentityProvider {
    override val providerId: String = "THAID_OIDC"

    override fun getAuthIntent(): Any {
        // ในสถานการณ์จริง จะส่งไปยัง Intent ของแอป ThaiD
        return "thaid://auth?scope=openid&response_type=code"
    }

    override fun handleCallback(data: Any): AuthResult {
        // จำลองการรับ callback จาก ThaiD
        return try {
            // สมมติว่า data คือผลลัพธ์ที่ได้จากการ redirect
            val userId = UUID.randomUUID().toString()
            val identity = VerifiedIdentity(
                userId = userId,
                fullName = "นายสมปอง สมดี",
                nationalIdHash = "hashed_id_${UUID.randomUUID()}",
                identityLevel = IdentityLevel.VERIFIED_THAID,
                verifiedAt = LocalDateTime.now()
            )
            AuthResult.Success(identity)
        } catch (e: Exception) {
            AuthResult.Error("ThaiD Authentication Failed")
        }
    }
}
