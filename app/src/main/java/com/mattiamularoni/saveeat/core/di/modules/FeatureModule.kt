package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.features.auth.presentation.di.authModule
import com.mattiamularoni.saveeat.features.home.presentation.di.homeScreenModule
import com.mattiamularoni.saveeat.features.leaderboard.presentation.di.leaderboardScreenModule
import com.mattiamularoni.saveeat.features.pantry.presentation.di.pantryScreenModule
import com.mattiamularoni.saveeat.features.recipes.presentation.di.recipeScreenModule
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.di.scanReceiptModule
import org.koin.core.module.Module

val featureModules: List<Module> = listOf(
    authModule,
    homeScreenModule,
    pantryScreenModule,
    leaderboardScreenModule,
    recipeScreenModule,
    scanReceiptModule
)
