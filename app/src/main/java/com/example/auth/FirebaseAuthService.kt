package com.example.auth

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthService {

    private val _authState = MutableStateFlow<AuthState>(
        auth.currentUser?.let { AuthState.Authenticated(it.toAuthUser()) } ?: AuthState.Unauthenticated
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override val currentUser: AuthUser?
        get() = auth.currentUser?.toAuthUser()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _authState.value = if (user != null) {
                AuthState.Authenticated(user.toAuthUser())
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    override fun isUserSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signInWithGoogleCredential(credential: Credential): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            when (credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            signInWithGoogleIdToken(googleIdTokenCredential.idToken)
                        } catch (e: GoogleIdTokenParsingException) {
                            Result.failure(e)
                        }
                    } else {
                        Result.failure(IllegalArgumentException("Unsupported credential type: ${credential.type}"))
                    }
                }
                else -> Result.failure(IllegalArgumentException("Expected CustomCredential"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = suspendCancellableCoroutine { continuation ->
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val user = result.user?.toAuthUser()
                        if (user != null) {
                            continuation.resume(Result.success(user))
                        } else {
                            continuation.resume(Result.failure(IllegalStateException("User is null after Google sign-in")))
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
            if (authResult.isFailure) {
                _authState.value = AuthState.Error(
                    authResult.exceptionOrNull()?.localizedMessage ?: "Google sign-in failed",
                    authResult.exceptionOrNull()
                )
            }
            authResult
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Unexpected authentication error", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val authResult = suspendCancellableCoroutine { continuation ->
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { result ->
                        val user = result.user?.toAuthUser()
                        if (user != null) {
                            continuation.resume(Result.success(user))
                        } else {
                            continuation.resume(Result.failure(IllegalStateException("User is null after email sign-in")))
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
            if (authResult.isFailure) {
                _authState.value = AuthState.Error(
                    authResult.exceptionOrNull()?.localizedMessage ?: "Sign-in failed",
                    authResult.exceptionOrNull()
                )
            }
            authResult
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Sign-in failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val authResult = suspendCancellableCoroutine { continuation ->
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { result ->
                        val firebaseUser = result.user
                        if (firebaseUser != null) {
                            if (!displayName.isNullOrBlank()) {
                                val profileUpdate = UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName.trim())
                                    .build()
                                firebaseUser.updateProfile(profileUpdate).addOnCompleteListener {
                                    continuation.resume(Result.success(firebaseUser.toAuthUser()))
                                }
                            } else {
                                continuation.resume(Result.success(firebaseUser.toAuthUser()))
                            }
                        } else {
                            continuation.resume(Result.failure(IllegalStateException("User is null after sign-up")))
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
            if (authResult.isFailure) {
                _authState.value = AuthState.Error(
                    authResult.exceptionOrNull()?.localizedMessage ?: "Sign-up failed",
                    authResult.exceptionOrNull()
                )
            }
            authResult
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.localizedMessage ?: "Sign-up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                auth.sendPasswordResetEmail(email.trim())
                    .addOnSuccessListener {
                        continuation.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
