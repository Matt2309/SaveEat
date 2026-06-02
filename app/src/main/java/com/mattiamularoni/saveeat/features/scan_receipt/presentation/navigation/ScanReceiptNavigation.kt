package com.mattiamularoni.saveeat.features.scan_receipt.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.ScanReceiptRoute
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.ui.ScanReceiptScreen

fun NavGraphBuilder.scanReceiptScreen(
    onNavigateBack: () -> Unit = {}
) {
    composable<ScanReceiptRoute> {
        ScanReceiptScreen(onNavigateBack = onNavigateBack)
    }
}
