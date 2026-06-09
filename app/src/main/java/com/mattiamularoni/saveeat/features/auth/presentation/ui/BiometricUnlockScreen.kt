package com.mattiamularoni.saveeat.features.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.mattiamularoni.saveeat.ui.theme.SaveEatTheme

/**
 * Schermata di sblocco biometrico (UI fedele al mockup).
 *
 * @param subtitle testo di saluto (es. "Bentornato su SaveEat")
 * @param onTapFingerprint invocata quando l'utente tocca l'impronta -> avvia il BiometricPrompt
 * @param onUsePassword fallback verso il login con password
 * @param errorMessage eventuale messaggio d'errore da mostrare sotto l'impronta
 */
@Composable
fun BiometricUnlockScreen(
    subtitle: String = "Bentornato su SaveEat",
    onTapFingerprint: () -> Unit,
    onUsePassword: () -> Unit,
    errorMessage: String? = null
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            // Alone decorativo verde sfumato (al posto dei blur del mockup, compatibile con tutte le API)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colors.primaryContainer.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(0f, 0f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(0.4f))

            // ---- Branding ----
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "SaveEat",
                    color = colors.primary,
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // ---- Trigger impronta ----
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerHigh)
                        .border(1.dp, colors.surfaceVariant, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTapFingerprint() },
                    contentAlignment = Alignment.Center
                ) {
                    // anello interno tenue
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(2.dp, colors.primary.copy(alpha = 0.10f), CircleShape)
                    )
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = "Accedi con l'impronta digitale",
                        tint = colors.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tocca per accedere",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.6f))

            // ---- Fallback password ----
            TextButton(onClick = onUsePassword) {
                Text(
                    text = "Usa password",
                    color = colors.primary,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BiometricUnlockScreenPreview() {
    SaveEatTheme {
        BiometricUnlockScreen(
            subtitle = "Bentornato su SaveEat",
            onTapFingerprint = {},
            onUsePassword = {},
            errorMessage = null
        )
    }
}
