package com.mattiamularoni.saveeat.features.pantry.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PantryAssetRemoteDataSource {
    suspend fun getAllAssets(): List<PantryAssetDto>
}

class PantryAssetRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient,
) : PantryAssetRemoteDataSource {
    override suspend fun getAllAssets(): List<PantryAssetDto> =
        withContext(Dispatchers.IO) {
            supabaseClient.from("pantry_item_assets").select().decodeList()
        }
}
