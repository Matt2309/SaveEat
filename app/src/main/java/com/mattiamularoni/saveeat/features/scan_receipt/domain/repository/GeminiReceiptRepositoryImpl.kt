package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.scan_receipt.data.remote.GeminiReceiptDataSource
import com.mattiamularoni.saveeat.features.scan_receipt.data.remote.GeminiReceiptResponseDto
import com.mattiamularoni.saveeat.features.scan_receipt.domain.model.ParsedReceiptItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ScanReceiptRepositoryImpl(
    private val geminiDataSource: GeminiReceiptDataSource,
) : ScanReceiptRepository {
    // Exclude other IA keys
    private val jsonParser = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeReceiptImage(bitmap: Bitmap): ScannedReceiptData =
        withContext(
            Dispatchers.IO,
        ) {
            // gemini call
            val jsonString = geminiDataSource.analyzeReceipt(bitmap)

            // 2. parsing into response dto
            val response =
                try {
                    jsonParser.decodeFromString<GeminiReceiptResponseDto>(jsonString)
                } catch (e: Exception) {
                    throw Exception("Impossibile decifrare lo scontrino. Riprova con una foto più nitida.", e)
                }

            // 3. Dto mapping into parsed receipt items
            ScannedReceiptData(
                storeName = response.storeName,
                totalPrice = response.totalPrice,
                items =
                    response.items.map { dto ->
                        ParsedReceiptItem(
                            name = dto.name,
                            categoryKey = dto.categoryKey,
                            category = dto.category,
                            isPerishable = dto.isPerishable,
                            estimatedExpiryDays = dto.estimatedExpiryDays,
                            quantity = dto.quantity,
                            unit = dto.unit,
                        )
                    },
            )
        }
}
