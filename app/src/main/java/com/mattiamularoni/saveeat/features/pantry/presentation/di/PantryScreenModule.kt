package com.mattiamularoni.saveeat.features.pantry.presentation.di

import com.mattiamularoni.saveeat.core.data.remote.AuthSessionProviderImpl
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryAssetRemoteDataSource
import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryAssetRemoteDataSourceImpl
import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryRemoteDataSource
import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryRemoteDataSourceImpl
import com.mattiamularoni.saveeat.features.pantry.data.repository.PantryAssetRepositoryImpl
import com.mattiamularoni.saveeat.features.pantry.data.repository.PantryRepositoryImpl
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryAssetRepository
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.pantry.presentation.domain.GetPantryItemsUseCase
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pantryScreenModule = module {
    single<SessionProvider> { AuthSessionProviderImpl(supabaseClient = get()) }
    factory<PantryRemoteDataSource> {
        PantryRemoteDataSourceImpl(supabaseClient = get())
    }
    factory<PantryRepository> {
        PantryRepositoryImpl(
            pantryDao = get(),
            remoteDataSource = get(),
            sessionProvider = get()
        )
    }
    factory<PantryAssetRemoteDataSource> {
        PantryAssetRemoteDataSourceImpl(supabaseClient = get())
    }
    single<PantryAssetRepository> {
        PantryAssetRepositoryImpl(dao = get(), remote = get())
    }
    factory { GetPantryItemsUseCase(get()) }
    viewModelOf(::PantryViewModel)
}
