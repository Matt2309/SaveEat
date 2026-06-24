package com.mattiamularoni.saveeat.features.settings.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Modalità scura: stato reale e persistente
    val themeController: com.mattiamularoni.saveeat.ui.theme.ThemeController =
        org.koin.compose.koinInject()
    val darkMode by themeController.darkMode.collectAsState()

    // Avvisi scadenza: stato reale e persistente
    val notificationPreferencesController: com.mattiamularoni.saveeat.ui.settings.NotificationPreferencesController =
        org.koin.compose.koinInject()
    val expiryAlerts by notificationPreferencesController.expiryAlertsEnabled.collectAsState()

    // Stato ancora solo grafico (placeholder)
    var weeklyReport by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        topBar = { SettingsTopBar(onNavigateBack = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ---- Aspetto ----
            SettingsSection(title = "Aspetto") {
                ToggleRow(
                    icon = Icons.Filled.DarkMode,
                    title = "Modalità Scura",
                    subtitle = "Attiva il tema scuro per risparmiare batteria",
                    checked = darkMode,
                    onCheckedChange = { themeController.setDarkMode(it) }
                )
            }

            // ---- Notifiche ----
            SettingsSection(title = "Notifiche") {
                ToggleRow(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Avvisi scadenza alimenti",
                    subtitle = "Ricevi una notifica prima che il cibo scada",
                    checked = expiryAlerts,
                    onCheckedChange = { notificationPreferencesController.setExpiryAlertsEnabled(it) }
                )
                RowDivider()
                ToggleRow(
                    icon = Icons.Filled.Insights,
                    title = "Report Settimanali",
                    subtitle = "Statistiche sugli sprechi evitati",
                    checked = weeklyReport,
                    onCheckedChange = { weeklyReport = it }
                )
            }

            // ---- Generali ----
            SettingsSection(title = "Generali") {
                NavRow(
                    icon = Icons.Outlined.Language,
                    title = "Lingua",
                    subtitle = "Italiano (Italia)",
                    onClick = { /* TODO */ }
                )
                RowDivider()
                NavRow(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Account",
                    subtitle = "Gestisci profilo e sicurezza",
                    onClick = { onNavigateBack() } // torna al Profilo
                )
            }

            // ---- Info App ----
            SettingsSection(title = "Info App") {
                InfoRow(
                    icon = Icons.Outlined.Info,
                    title = "Versione",
                    subtitle = "v0.0.1 (Build 1)"
                )
                RowDivider()
                NavRow(
                    icon = Icons.Outlined.Policy,
                    title = "Note legali e Privacy",
                    subtitle = null,
                    onClick = { /* TODO */ }
                )
            }

            // ---- Esci dall'account (logout reale) ----
            OutlinedButton(
                onClick = { onLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Esci dall'account", style = MaterialTheme.typography.titleMedium)
            }

            // ---- Footer ----
            Text(
                text = "SaveEat © 2026 · Made with ♥ for the Planet",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 8.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Indietro",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Impostazioni",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = { /* TODO: ricerca impostazioni */ }) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = "Cerca",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(content = content)
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NavRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    )
}
