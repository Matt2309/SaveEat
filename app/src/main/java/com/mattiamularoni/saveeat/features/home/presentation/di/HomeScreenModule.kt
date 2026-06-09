package com.mattiamularoni.saveeat.features.home.presentation.di

import com.mattiamularoni.saveeat.core.data.remote.AuthSessionProviderImpl
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.home.data.remote.HomeRemoteDataSource
import com.mattiamularoni.saveeat.features.home.data.remote.HomeRemoteDataSourceImpl
import com.mattiamularoni.saveeat.features.home.data.repository.HomeRepositoryImpl
import com.mattiamularoni.saveeat.features.home.domain.repository.HomeRepository
import com.mattiamularoni.saveeat.features.home.presentation.domain.GetHomeDashboardUseCase
import com.mattiamularoni.saveeat.features.home.presentation.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module per il modulo Home.
 *
 * Configura l'iniezione di dipendenze per:
 * - Data layer: RemoteDataSource, Repository
 * - Domain layer: UseCase
 * - Presentation layer: ViewModel
 *
 * Factory scope per stateless services (DataSource, Repository, UseCase).
 * ViewModel scope per il ViewModel (managed lifecycle).
 *
 * Dipendenze:
 * - supabaseClient: injected da networkModule (core)
 * - homeDao: injected da databaseModule (core)
 * - userId: Managed internally in repository (MVP: "test-user-uuid")
 */
val homeScreenModule = module {
    single<SessionProvider> { AuthSessionProviderImpl(supabaseClient = get()) }

    factory<HomeRemoteDataSource> {
        HomeRemoteDataSourceImpl(supabaseClient = get())
    }

    // Repository
    factory<HomeRepository> {
        HomeRepositoryImpl(
            homeDao = get(),
            remoteDataSource = get(),
            sessionProvider = get()
        )
    }

    // Use Case
    factory { GetHomeDashboardUseCase(get()) }

    // ViewModel
    viewModelOf(::HomeViewModel)
}

