package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.usecase

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.receipt_history.domain.repository.ReceiptRepository
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.ScanReceiptRepository

class ProcessReceiptUseCase(
    private val scanReceiptRepository: ScanReceiptRepository,
    private val receiptRepository: ReceiptRepository,
    private val pantryRepository: PantryRepository
) {
    /**
     * @param bitmap La foto dello scontrino
     * @return Result.success se tutto è andato bene, Result.failure in caso di errore
     */
    suspend operator fun invoke(bitmap: Bitmap): Result<Unit> {
        return try {
            // 1. Analyze image with IA (store name, total price, items)
            val scanned = scanReceiptRepository.analyzeReceiptImage(bitmap)

            if (scanned.items.isEmpty()) {
                throw Exception("Nessun prodotto alimentare trovato nello scontrino.")
            }

            // 2. Upload receipt photo to Supabase Storage
            val imageUrl = receiptRepository.uploadReceiptImage(bitmap)

            // 3. Insert the receipt row (store name, total price, image url)
            val receipt = receiptRepository.insertReceipt(
                storeName = scanned.storeName,
                totalPrice = scanned.totalPrice,
                imageUrl = imageUrl
            )

            // 4. Save items to pantry, linked to the real receipt id
            pantryRepository.saveReceiptItems(receipt.id, scanned.items)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
