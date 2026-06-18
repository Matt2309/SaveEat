package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.scan_receipt.data.remote.GeminiReceiptDataSource
import com.mattiamularoni.saveeat.features.scan_receipt.data.remote.GeminiReceiptItemDto
import com.mattiamularoni.saveeat.features.scan_receipt.domain.model.ParsedReceiptItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ScanReceiptRepositoryImpl(
    private val geminiDataSource: GeminiReceiptDataSource
) : ScanReceiptRepository {

    // Exclude other IA keys
    private val jsonParser = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeReceiptImage(bitmap: Bitmap): List<ParsedReceiptItem> = withContext(
        Dispatchers.IO) {
        // gemini call
        val jsonString = geminiDataSource.analyzeReceipt(bitmap)

        // 2. parsing into dto list
        val dtos = try {
            jsonParser.decodeFromString<List<GeminiReceiptItemDto>>(jsonString)
        } catch (e: Exception) {
            throw Exception("Impossibile decifrare lo scontrino. Riprova con una foto più nitida.", e)
        }

        // 3. Dto mapping into parsed receipt items
        dtos.map { dto ->
            ParsedReceiptItem(
                name = dto.name,
                categoryKey = dto.category,
                isPerishable = dto.isPerishable,
                estimatedExpiryDays = dto.estimatedExpiryDays,
                quantity = dto.quantity,
                unit = dto.unit
            )
        }
    }
}
