package com.mattiamularoni.saveeat.core.data.remote

import com.mattiamularoni.saveeat.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClientProvider {
    @Volatile
    private var client: SupabaseClient? = null

    fun getOrCreate(): SupabaseClient {
        return client ?: synchronized(this) {
            client ?: createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Postgrest)
                install(Auth) {
                    scheme = "saveeat"
                    host = "callback"
                }
            }.also { client = it }
        }
    }

    fun clear() {
        client = null
    }
}

