package com.mattiamularoni.saveeat.features.auth.presentation.di

import com.mattiamularoni.saveeat.features.auth.data.remote.AuthRepositoryImpl
import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository
import com.mattiamularoni.saveeat.features.auth.domain.usecase.ObserveSessionStatusUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignInWithEmailUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignOutUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignUpWithEmailUseCase
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin module for the Auth feature.
 *
 * Configures the dependency injection for:
 * - Data layer: Repository implementation
 * - Domain layer: Use cases
 * - Presentation layer: ViewModel
 *
 * Scopes:
 * - Repository as single: Ensures single instance of auth repository
 * - Use cases as factory: Stateless services created on demand
 * - ViewModel as viewModel: Lifecycle-aware, created per screen
 *
 * Dependencies:
 * - SupabaseClient: Injected from networkModule (core)
 */
val authModule = module {
    // Data Layer
    single<AuthRepository> {
        AuthRepositoryImpl(
            supabaseClient = get<SupabaseClient>()
        )
    }

    // Domain Layer - Use Cases
    factory {
        SignInWithEmailUseCase(
            authRepository = get()
        )
    }

    factory {
        SignUpWithEmailUseCase(
            authRepository = get()
        )
    }

    factory {
        SignOutUseCase(
            authRepository = get()
        )
    }

    factory {
        ObserveSessionStatusUseCase(
            authRepository = get()
        )
    }

    // Presentation Layer - ViewModel
    viewModelOf(::AuthViewModel)
}
