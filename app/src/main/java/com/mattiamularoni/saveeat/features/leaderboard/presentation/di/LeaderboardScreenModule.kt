package com.mattiamularoni.saveeat.features.leaderboard.presentation.di

import com.mattiamularoni.saveeat.features.leaderboard.data.remote.LeaderboardRemoteDataSource
import com.mattiamularoni.saveeat.features.leaderboard.data.remote.LeaderboardRemoteDataSourceImpl
import com.mattiamularoni.saveeat.features.leaderboard.data.repository.LeaderboardRepositoryImpl
import com.mattiamularoni.saveeat.features.leaderboard.domain.repository.LeaderboardRepository
import com.mattiamularoni.saveeat.features.leaderboard.presentation.domain.GetLeaderboardUseCase
import com.mattiamularoni.saveeat.features.leaderboard.presentation.viewmodel.LeaderboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module per l'iniezione delle dipendenze della leaderboard.
 *
 * Registra:
 * - RemoteDataSource: factory
 * - Repository: factory
 * - UseCase: factory
 * - ViewModel: viewModel
 */
val leaderboardScreenModule =
    module {
        factory<LeaderboardRemoteDataSource> {
            LeaderboardRemoteDataSourceImpl(supabaseClient = get())
        }
        factory<LeaderboardRepository> {
            LeaderboardRepositoryImpl(
                remoteDataSource = get(),
            )
        }
        factory { GetLeaderboardUseCase(get()) }
        viewModelOf(::LeaderboardViewModel)
    }
