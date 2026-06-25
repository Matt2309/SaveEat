package com.mattiamularoni.saveeat.features.receipt_history.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReceiptRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient,
) : ReceiptRemoteDataSource {
    override suspend fun uploadImage(
        path: String,
        bytes: ByteArray,
    ): String =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient.storage.from("receipt-images").upload(path, bytes) {
                    upsert = true
                }
                supabaseClient.storage.from("receipt-images").publicUrl(path)
            } catch (e: Exception) {
                throw Exception("Failed to upload receipt image: ${e.message}", e)
            }
        }

    override suspend fun insertReceipt(dto: ReceiptDto): ReceiptDto =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("receipts")
                    .insert(dto) {
                        select()
                    }.decodeSingle<ReceiptDto>()
            } catch (e: Exception) {
                throw Exception("Failed to insert receipt: ${e.message}", e)
            }
        }

    override suspend fun getReceipts(userId: String): List<ReceiptDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("receipts")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order("scanned_at", Order.DESCENDING)
                    }.decodeList<ReceiptDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch receipts: ${e.message}", e)
            }
        }
}
