package com.mattiamularoni.saveeat.features.auth.domain.repository

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionStatus: StateFlow<SessionStatus>

    suspend fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String)
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signOut()
}