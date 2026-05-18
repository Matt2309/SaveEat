package com.mattiamularoni.saveeat.core.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient

object SupabaseClientProvider {
    // TODO: Move Supabase credentials to secure build config before production.
    private const val SUPABASE_URL = "https://example.supabase.co"
    private const val SUPABASE_ANON_KEY = "public-anon-key"

    @Volatile
    private var client: SupabaseClient? = null

    fun getOrCreate(): SupabaseClient {
        return client ?: synchronized(this) {
            client ?: createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_ANON_KEY
            ) {
                // TODO: Install Supabase plugins (Auth/Postgrest/Storage) when remote flows are implemented.
            }.also { client = it }
        }
    }

    fun clear() {
        client = null
    }
}
