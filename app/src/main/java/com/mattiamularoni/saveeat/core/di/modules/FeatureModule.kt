package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.features.leaderboard.presentation.di.leaderboardScreenModule
import com.mattiamularoni.saveeat.features.pantry.presentation.di.pantryScreenModule
import org.koin.core.module.Module

val featureModules: List<Module> = listOf(
    pantryScreenModule,
    leaderboardScreenModule
)
