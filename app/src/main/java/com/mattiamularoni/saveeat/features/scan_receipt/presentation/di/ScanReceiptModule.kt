package com.mattiamularoni.saveeat.features.scan_receipt.presentation.di

import com.mattiamularoni.saveeat.features.scan_receipt.data.remote.GeminiReceiptDataSource
import com.mattiamularoni.saveeat.features.scan_receipt.data.remote.GeminiReceiptDataSourceImpl
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.ScanReceiptRepository
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.ScanReceiptRepositoryImpl
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.usecase.ProcessReceiptUseCase
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.viewmodel.ScanReceiptViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val scanReceiptModule = module {
    // Data Source
    single<GeminiReceiptDataSource> { GeminiReceiptDataSourceImpl() }

    // Repository
    single<ScanReceiptRepository> {
        ScanReceiptRepositoryImpl(geminiDataSource = get())
    }

    // Use Case
    factory {
        ProcessReceiptUseCase(
            scanReceiptRepository = get(),
            receiptRepository = get(),
            pantryRepository = get()
        )
    }

    viewModelOf(::ScanReceiptViewModel)
}