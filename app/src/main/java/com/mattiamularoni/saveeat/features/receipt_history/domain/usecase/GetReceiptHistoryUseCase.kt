package com.mattiamularoni.saveeat.features.receipt_history.domain.usecase

import com.mattiamularoni.saveeat.features.receipt_history.domain.repository.Receipt
import com.mattiamularoni.saveeat.features.receipt_history.domain.repository.ReceiptRepository

class GetReceiptHistoryUseCase(
    private val receiptRepository: ReceiptRepository,
) {
    suspend operator fun invoke(): List<Receipt> = receiptRepository.getReceipts()
}
