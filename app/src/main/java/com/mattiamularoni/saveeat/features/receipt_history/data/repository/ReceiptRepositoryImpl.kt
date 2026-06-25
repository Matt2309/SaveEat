package com.mattiamularoni.saveeat.features.receipt_history.data.repository

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.core.util.DateTimeUtils
import com.mattiamularoni.saveeat.features.receipt_history.data.remote.ReceiptDto
import com.mattiamularoni.saveeat.features.receipt_history.data.remote.ReceiptRemoteDataSource
import com.mattiamularoni.saveeat.features.receipt_history.domain.repository.Receipt
import com.mattiamularoni.saveeat.features.receipt_history.domain.repository.ReceiptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class ReceiptRepositoryImpl(
    private val remoteDataSource: ReceiptRemoteDataSource,
    private val sessionProvider: SessionProvider,
) : ReceiptRepository {
    override suspend fun uploadReceiptImage(bitmap: Bitmap): String =
        withContext(Dispatchers.IO) {
            val stream = ByteArrayOutputStream()
            // JPEG 70%: qualità sufficiente per la lettura dello scontrino, peso minimo su storage cloud
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            val path = "${sessionProvider.getCurrentUserId()}/${UUID.randomUUID()}.jpg"
            remoteDataSource.uploadImage(path, stream.toByteArray())
        }

    override suspend fun insertReceipt(
        storeName: String,
        totalPrice: Double,
        imageUrl: String,
    ): Receipt =
        withContext(Dispatchers.IO) {
            val dto =
                ReceiptDto(
                    id = UUID.randomUUID().toString(),
                    userId = sessionProvider.getCurrentUserId(),
                    storeName = storeName,
                    totalPrice = totalPrice,
                    imageUrl = imageUrl,
                    scannedAt = DateTimeUtils.formatToIso8601(System.currentTimeMillis()),
                )
            remoteDataSource.insertReceipt(dto).toDomain()
        }

    override suspend fun getReceipts(): List<Receipt> =
        withContext(Dispatchers.IO) {
            remoteDataSource.getReceipts(sessionProvider.getCurrentUserId()).map { it.toDomain() }
        }

    private fun ReceiptDto.toDomain() =
        Receipt(
            id = id,
            userId = userId,
            storeName = storeName,
            totalPrice = totalPrice,
            imageUrl = imageUrl,
            scannedAt = DateTimeUtils.parseIso8601OrDefault(scannedAt),
        )
}
