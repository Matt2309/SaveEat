package com.mattiamularoni.saveeat.features.receipt_history.presentation.di

import com.mattiamularoni.saveeat.core.data.remote.AuthSessionProviderImpl
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.receipt_history.data.remote.ReceiptRemoteDataSource
import com.mattiamularoni.saveeat.features.receipt_history.data.remote.ReceiptRemoteDataSourceImpl
import com.mattiamularoni.saveeat.features.receipt_history.data.repository.ReceiptRepositoryImpl
import com.mattiamularoni.saveeat.features.receipt_history.domain.repository.ReceiptRepository
import com.mattiamularoni.saveeat.features.receipt_history.domain.usecase.GetReceiptHistoryUseCase
import com.mattiamularoni.saveeat.features.receipt_history.presentation.viewmodel.ReceiptHistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val receiptHistoryModule =
    module {
        single<SessionProvider> { AuthSessionProviderImpl(supabaseClient = get()) }
        factory<ReceiptRemoteDataSource> {
            ReceiptRemoteDataSourceImpl(supabaseClient = get())
        }
        single<ReceiptRepository> {
            ReceiptRepositoryImpl(
                remoteDataSource = get(),
                sessionProvider = get(),
            )
        }
        factory { GetReceiptHistoryUseCase(get()) }
        viewModelOf(::ReceiptHistoryViewModel)
    }
