package com.mattiamularoni.saveeat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.mattiamularoni.saveeat.common.utils.RequestNotificationPermission
import com.mattiamularoni.saveeat.core.navigation.SaveEatNavHost
import com.mattiamularoni.saveeat.ui.theme.SaveEatTheme
import com.mattiamularoni.saveeat.ui.theme.ThemeController
import com.mattiamularoni.saveeat.ui.theme.ThemeMode
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    private val supabaseClient: SupabaseClient by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supabaseClient.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            SaveEatApp()
        }
    }
}

@Composable
fun SaveEatApp() {
    val themeController: ThemeController = koinInject()
    val darkMode by themeController.darkMode.collectAsState()
    SaveEatTheme(themeMode = if (darkMode) ThemeMode.Dark else ThemeMode.Light) {
    	RequestNotificationPermission()
        SaveEatNavHost()
    }
}

@Preview(showBackground = true)
@Composable
fun SaveEatAppPreview() {
    SaveEatApp()
}
