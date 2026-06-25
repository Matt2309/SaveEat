package com.mattiamularoni.saveeat.features.receipt_history.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.ReceiptHistoryRoute
import com.mattiamularoni.saveeat.features.receipt_history.presentation.ui.ReceiptHistoryScreen

fun NavGraphBuilder.receiptHistoryScreen(onNavigateBack: () -> Unit = {}) {
    composable<ReceiptHistoryRoute> {
        ReceiptHistoryScreen(onNavigateBack = onNavigateBack)
    }
}
