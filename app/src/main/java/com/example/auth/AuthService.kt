package com.example.auth

import android.content.Context
import androidx.credentials.Credential
import kotlinx.coroutines.flow.StateFlow

interface AuthService {
    val authState: StateFlow<AuthState>
    val currentUser: AuthUser?

    suspend fun signInWithGoogleCredential(credential: Credential): Result<AuthUser>
    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String? = null): Result<AuthUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signOut(): Result<Unit>

    fun isUserSignedIn(): Boolean
}
