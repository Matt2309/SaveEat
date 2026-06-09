package com.mattiamularoni.saveeat.features.auth.presentation.di

import com.mattiamularoni.saveeat.features.auth.data.local.BiometricPreferenceDataSource
import com.mattiamularoni.saveeat.features.auth.data.local.BiometricRepositoryImpl
import com.mattiamularoni.saveeat.features.auth.data.remote.AuthRepositoryImpl
import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository
import com.mattiamularoni.saveeat.features.auth.domain.repository.BiometricRepository
import com.mattiamularoni.saveeat.features.auth.domain.usecase.AuthenticateWithBiometricsUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.CheckBiometricAvailabilityUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.DisableBiometricUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.EnableBiometricUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.ObserveSessionStatusUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.RestoreAuthenticatedSessionUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignInWithEmailUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignInWithGoogleUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignOutUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignUpWithEmailUseCase
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module per il feature Auth.
 *
 * Configura la dependency injection per:
 * - Data layer: repository email/password e repository biometrico
 * - Domain layer: tutti i use case di autenticazione e biometria
 * - Presentation layer: ViewModel
 *
 * Scopes:
 * - Repository come `single`: istanza unica condivisa
 * - Use case come `factory`: stateless, creati on demand
 * - ViewModel come `viewModel`: lifecycle-aware, uno per schermata
 */
val authModule = module {

    // ---- Data Layer ----

    single<AuthRepository> {
        AuthRepositoryImpl(
            supabaseClient = get<SupabaseClient>()
        )
    }

    single { BiometricPreferenceDataSource(androidApplication()) }

    single<BiometricRepository> {
        BiometricRepositoryImpl(
            context = androidApplication(),
            supabaseClient = get(),
            preferenceDataSource = get()
        )
    }

    // ---- Domain Layer — Use Cases Email/Password + Google ----

    factory { SignInWithEmailUseCase(authRepository = get()) }
    factory { SignUpWithEmailUseCase(authRepository = get()) }
    factory { SignOutUseCase(authRepository = get()) }
    factory { SignInWithGoogleUseCase(authRepository = get()) }
    factory { ObserveSessionStatusUseCase(authRepository = get()) }

    // ---- Domain Layer — Use Cases Biometria ----

    factory { CheckBiometricAvailabilityUseCase(biometricRepository = get()) }
    factory { EnableBiometricUseCase(biometricRepository = get()) }
    factory { DisableBiometricUseCase(biometricRepository = get()) }
    factory { AuthenticateWithBiometricsUseCase(biometricRepository = get()) }
    factory { RestoreAuthenticatedSessionUseCase(biometricRepository = get()) }

    // ---- Presentation Layer ----

    viewModelOf(::AuthViewModel)
}
