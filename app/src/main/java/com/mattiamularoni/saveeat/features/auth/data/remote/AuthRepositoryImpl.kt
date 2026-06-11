package com.mattiamularoni.saveeat.features.auth.data.remote

import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
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
                put("display_name", "$firstName $lastName".trim())
            }
        }
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInWithGoogle(idToken: String) {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }

        val user = supabaseClient.auth.currentSessionOrNull()?.user ?: return
        val metadata = user.userMetadata
        val displayName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("name")?.jsonPrimitive?.contentOrNull
        val avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("picture")?.jsonPrimitive?.contentOrNull

        supabaseClient.from("users").upsert(
            UserProfileDto(
                id = user.id,
                email = user.email ?: "",
                displayName = displayName,
                avatarUrl = avatarUrl,
                authProvider = "google"
            )
        )
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}