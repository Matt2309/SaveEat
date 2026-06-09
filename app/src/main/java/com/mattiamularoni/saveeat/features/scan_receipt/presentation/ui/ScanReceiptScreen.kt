package com.mattiamularoni.saveeat.features.scan_receipt.presentation.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.viewmodel.ScanReceiptUiState
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.viewmodel.ScanReceiptViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScanReceiptViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Launcher per la fotocamera: Android scatta la foto e ci restituisce un Bitmap
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Se l'utente ha scattato la foto (non ha annullato), la passiamo a Gemini!
            viewModel.analyzeReceipt(bitmap)
        }
    }

    // Effetto di navigazione: se la scansione ha successo, torniamo alla dispensa
    LaunchedEffect(uiState) {
        if (uiState is ScanReceiptUiState.Success) {
            viewModel.resetState()
            onNavigateBack() // Torna alla PantryScreen dove vedremo i nuovi prodotti!
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scansiona Scontrino") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is ScanReceiptUiState.Idle -> {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Scatta una foto al tuo scontrino per aggiungere automaticamente i prodotti alla dispensa.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { cameraLauncher.launch() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Apri Fotocamera")
                    }
                }

                is ScanReceiptUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "L'Intelligenza Artificiale sta leggendo lo scontrino...\nPotrebbe volerci qualche secondo.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is ScanReceiptUiState.Error -> {
                    Text(
                        text = "Qualcosa è andato storto",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.resetState() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Riprova")
                    }
                }

                is ScanReceiptUiState.Success -> {
                }
            }
        }
    }
}