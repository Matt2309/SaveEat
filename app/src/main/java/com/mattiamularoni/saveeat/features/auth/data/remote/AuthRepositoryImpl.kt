package com.mattiamularoni.saveeat.features.auth.data.remote

import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Implementation of AuthRepository using Supabase Auth.
 *
 * Handles:
 * - Email/password sign-up
 * - Email/password sign-in
 * - Sign-out
 * - Session status observation
 */
class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override val sessionStatus: StateFlow<SessionStatus> =
        supabaseClient.auth.sessionStatus

    override suspend fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String) {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password

            this.data = buildJsonObject {
                put("first_name", firstName)
                put("last_name", lastName)
            }
        }
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}