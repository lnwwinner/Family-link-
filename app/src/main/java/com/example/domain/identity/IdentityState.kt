package com.example.domain.identity

import com.example.domain.auth.AuthResult

sealed class IdentityState {
    object Idle : IdentityState()
    object Loading : IdentityState()
    data class Success(val identity: VerifiedIdentity) : IdentityState()
    data class Error(val message: String) : IdentityState()
}
