package com.example.auth

sealed interface AuthState {
    object Unauthenticated : AuthState
    object Loading : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
    data class Error(val message: String, val throwable: Throwable? = null) : AuthState
}
