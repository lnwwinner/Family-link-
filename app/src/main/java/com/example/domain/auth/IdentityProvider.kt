package com.example.domain.auth

import com.example.domain.identity.VerifiedIdentity

sealed class AuthResult {
    data class Success(val identity: VerifiedIdentity) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

interface IdentityProvider {
    val providerId: String
    
    // เตรียมสำหรับ OIDC ในอนาคต
    fun getAuthIntent(): Any
    fun handleCallback(data: Any): AuthResult
}
