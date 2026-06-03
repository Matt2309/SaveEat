package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.usecase

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.ScanReceiptRepository
import java.util.UUID

class ProcessReceiptUseCase(
    private val scanReceiptRepository: ScanReceiptRepository,
    private val pantryRepository: PantryRepository
) {
    /**
     * @param bitmap La foto dello scontrino
     * @return Result.success se tutto è andato bene, Result.failure in caso di errore
     */
    suspend operator fun invoke(bitmap: Bitmap): Result<Unit> {
        return try {
            // 1. Analyze image with IA
            val scannedItems = scanReceiptRepository.analyzeReceiptImage(bitmap)

            if (scannedItems.isEmpty()) {
                throw Exception("Nessun prodotto alimentare trovato nello scontrino.")
            }

            val receiptId = UUID.randomUUID().toString()

            // save items to repository
            pantryRepository.saveReceiptItems(receiptId, scannedItems)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}