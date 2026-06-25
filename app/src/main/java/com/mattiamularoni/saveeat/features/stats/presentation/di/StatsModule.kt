package com.mattiamularoni.saveeat.features.stats.presentation.di

import com.mattiamularoni.saveeat.features.stats.data.remote.UserStatsRemoteDataSource
import com.mattiamularoni.saveeat.features.stats.data.remote.UserStatsRemoteDataSourceImpl
import com.mattiamularoni.saveeat.features.stats.data.repository.StatsRepositoryImpl
import com.mattiamularoni.saveeat.features.stats.domain.repository.StatsRepository
import com.mattiamularoni.saveeat.features.stats.domain.usecase.GetUserStatsUseCase
import com.mattiamularoni.saveeat.features.stats.domain.usecase.RefreshUserStatsUseCase
import org.koin.dsl.module

val statsModule =
    module {
        factory<UserStatsRemoteDataSource> {
            UserStatsRemoteDataSourceImpl(supabaseClient = get())
        }
        factory<StatsRepository> {
            StatsRepositoryImpl(
                userStatsDao = get(),
                remoteDataSource = get(),
                sessionProvider = get(),
            )
        }
        factory { GetUserStatsUseCase(get()) }
        factory { RefreshUserStatsUseCase(get()) }
    }
