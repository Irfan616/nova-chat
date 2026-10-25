package com.example.auth

import androidx.credentials.Credential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockAuthService(
    initialUser: AuthUser? = AuthUser(
        uid = "user_me",
        email = "alex.vance@novachat.network",
        displayName = "Alex Vance"
    )
) : AuthService {

    private val _authState = MutableStateFlow<AuthState>(
        initialUser?.let { AuthState.Authenticated(it) } ?: AuthState.Unauthenticated
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override var currentUser: AuthUser? = initialUser
        private set

    override fun isUserSignedIn(): Boolean = currentUser != null

    override suspend fun signInWithGoogleCredential(credential: Credential): Result<AuthUser> {
        val user = AuthUser(
            uid = "user_google_123",
            email = "alex.google@novachat.network",
            displayName = "Alex Vance"
        )
        currentUser = user
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser> {
        val user = AuthUser(
            uid = "user_google_123",
            email = "alex.google@novachat.network",
            displayName = "Alex Vance"
        )
        currentUser = user
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        val user = AuthUser(
            uid = "user_email_123",
            email = email,
            displayName = email.substringBefore("@")
        )
        currentUser = user
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthUser> {
        val user = AuthUser(
            uid = "user_email_new",
            email = email,
            displayName = displayName ?: email.substringBefore("@")
        )
        currentUser = user
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun signOut(): Result<Unit> {
        currentUser = null
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }
}
