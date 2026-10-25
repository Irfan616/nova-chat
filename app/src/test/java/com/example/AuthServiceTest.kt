package com.example

import com.example.auth.AuthState
import com.example.auth.AuthUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthServiceTest {

    @Test
    fun testAuthUserProperties() {
        val user = AuthUser(
            uid = "test_uid_123",
            email = "user@novachat.network",
            displayName = "Alex Vance",
            photoUrl = "https://example.com/avatar.jpg",
            phoneNumber = "+1 (555) 019-2834",
            isEmailVerified = true
        )

        assertEquals("test_uid_123", user.uid)
        assertEquals("user@novachat.network", user.email)
        assertEquals("Alex Vance", user.displayName)
        assertTrue(user.isEmailVerified)
    }

    @Test
    fun testAuthStateRepresentations() {
        val unauthenticated: AuthState = AuthState.Unauthenticated
        val loading: AuthState = AuthState.Loading
        val error: AuthState = AuthState.Error("Invalid credentials")

        val user = AuthUser(uid = "uid_456", email = "test@example.com")
        val authenticated: AuthState = AuthState.Authenticated(user)

        assertTrue(unauthenticated is AuthState.Unauthenticated)
        assertTrue(loading is AuthState.Loading)
        assertTrue(error is AuthState.Error)
        assertEquals("Invalid credentials", (error as AuthState.Error).message)
        assertTrue(authenticated is AuthState.Authenticated)
        assertEquals("uid_456", (authenticated as AuthState.Authenticated).user.uid)
    }
}
