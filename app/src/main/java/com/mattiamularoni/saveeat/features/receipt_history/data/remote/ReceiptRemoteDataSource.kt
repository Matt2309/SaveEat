package com.mattiamularoni.saveeat.features.receipt_history.data.remote

interface ReceiptRemoteDataSource {
    suspend fun uploadImage(path: String, bytes: ByteArray): String

    suspend fun insertReceipt(dto: ReceiptDto): ReceiptDto

    suspend fun getReceipts(userId: String): List<ReceiptDto>
}
