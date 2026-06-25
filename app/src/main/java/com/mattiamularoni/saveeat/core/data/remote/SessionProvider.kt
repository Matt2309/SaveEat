package com.mattiamularoni.saveeat.core.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.jsonPrimitive

interface SessionProvider {
    fun getCurrentUserId(): String

    fun getUserDisplayName(): String

    /** URL della foto profilo da Google (metadati auth), null se non presente. */
    fun getAvatarUrl(): String?
}

/**
 * Implementazione reale di SessionProvider.
 * Estrae l'ID e i metadati direttamente dalla sessione attiva di Supabase.
 */
class AuthSessionProviderImpl(
    private val supabaseClient: SupabaseClient,
) : SessionProvider {
    override fun getCurrentUserId(): String {
        // Estrae l'ID dell'utente attualmente loggato (grazie al token JWT)
        return supabaseClient.auth.currentUserOrNull()?.id ?: ""
    }

    override fun getUserDisplayName(): String {
        val user = supabaseClient.auth.currentUserOrNull()
        val metadata = user?.userMetadata

        // Estraiamo il JSON e lo convertiamo in stringhe in modo sicuro
        val firstName = metadata?.get("first_name")?.jsonPrimitive?.content ?: ""
        val lastName = metadata?.get("last_name")?.jsonPrimitive?.content ?: ""

        return if (firstName.isNotBlank() || lastName.isNotBlank()) {
            "$firstName $lastName".trim()
        } else {
            user?.email ?: "Utente"
        }
    }

    override fun getAvatarUrl(): String? {
        val metadata = supabaseClient.auth.currentUserOrNull()?.userMetadata
        // Google fornisce di solito "avatar_url" oppure "picture"
        return metadata?.get("avatar_url")?.jsonPrimitive?.content
            ?: metadata?.get("picture")?.jsonPrimitive?.content
    }
}
