package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.features.auth.presentation.di.authModule
import com.mattiamularoni.saveeat.features.home.presentation.di.homeScreenModule
import com.mattiamularoni.saveeat.features.leaderboard.presentation.di.leaderboardScreenModule
import com.mattiamularoni.saveeat.features.notifications.di.notificationsModule
import com.mattiamularoni.saveeat.features.pantry.presentation.di.pantryScreenModule
import com.mattiamularoni.saveeat.features.profile.presentation.di.profileModule
import com.mattiamularoni.saveeat.features.receipt_history.presentation.di.receiptHistoryModule
import com.mattiamularoni.saveeat.features.recipes.presentation.di.recipeScreenModule
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.di.scanReceiptModule
import com.mattiamularoni.saveeat.features.shopping_list.presentation.di.shoppingListModule
import com.mattiamularoni.saveeat.features.stats.presentation.di.statsModule
import org.koin.core.module.Module

val featureModules: List<Module> =
    listOf(
        authModule,
        homeScreenModule,
        pantryScreenModule,
        leaderboardScreenModule,
        recipeScreenModule,
        profileModule,
        scanReceiptModule,
        receiptHistoryModule,
        notificationsModule,
        statsModule,
        shoppingListModule,
    )
