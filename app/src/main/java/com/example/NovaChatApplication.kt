package com.example

import android.app.Application
import com.example.auth.AuthService
import com.example.auth.FirebaseAuthService
import com.example.auth.MockAuthService
import com.example.data.AppDatabase
import com.example.data.NovaChatRepository

class NovaChatApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: NovaChatRepository
        private set
    lateinit var authService: AuthService
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = NovaChatRepository(database, this)
        authService = try {
            FirebaseAuthService()
        } catch (e: Throwable) {
            MockAuthService()
        }
    }
}
