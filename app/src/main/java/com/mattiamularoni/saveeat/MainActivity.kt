package com.mattiamularoni.saveeat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mattiamularoni.saveeat.core.navigation.SaveEatNavHost
import com.mattiamularoni.saveeat.ui.theme.SaveEatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaveEatApp()
        }
    }
}

@Composable
fun SaveEatApp() {
    SaveEatTheme {
        SaveEatNavHost()
    }
}

@Preview(showBackground = true)
@Composable
fun SaveEatAppPreview() {
    SaveEatApp()
}
