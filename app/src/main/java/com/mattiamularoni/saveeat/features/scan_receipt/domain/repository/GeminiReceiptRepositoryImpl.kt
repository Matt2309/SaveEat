package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem
import com.mattiamularoni.saveeat.features.scan_receipt.data.remote.GeminiReceiptDataSource
import com.mattiamularoni.saveeat.features.scan_receipt.data.remote.GeminiReceiptItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID

class ScanReceiptRepositoryImpl(
    private val geminiDataSource: GeminiReceiptDataSource
) : ScanReceiptRepository {

    // Exclude other IA keys
    private val jsonParser = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeReceiptImage(bitmap: Bitmap): List<PantryItem> = withContext(
        Dispatchers.IO) {
        // gemini call
        val jsonString = geminiDataSource.analyzeReceipt(bitmap)

        // 2. parsing into dto list
        val dtos = try {
            jsonParser.decodeFromString<List<GeminiReceiptItemDto>>(jsonString)
        } catch (e: Exception) {
            throw Exception("Impossibile decifrare lo scontrino. Riprova con una foto più nitida.", e)
        }

        // 3. Dto mapping into pantry items
        dtos.map { dto ->
            PantryItem(
                id = UUID.randomUUID().toString(),
                userId = "",
                receiptId = null,
                name = dto.name,
                category = dto.category,
                isPlaceholder = false,
                status = "active",
                quantity = dto.quantity,
                unit = dto.unit,
                expirationDate = null
            )
        }
    }
}