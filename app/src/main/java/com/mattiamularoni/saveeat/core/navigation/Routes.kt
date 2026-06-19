package com.mattiamularoni.saveeat.core.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object HomeRoute

@Serializable
object PantryRoute

@Serializable
object ScanReceiptRoute

@Serializable
object RecipeRoute

@Serializable
data class RecipeDetailRoute(val id: String)

@Serializable
object LeaderboardRoute

@Serializable
object BiometricRoute

@Serializable
object ProfileRoute

@Serializable
object SettingsRoute

@Serializable
object ReceiptHistoryRoute
