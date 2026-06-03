package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FamilyCareViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.AppNavigator
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.AccessibilitySettings

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge full bleed layout support
        enableEdgeToEdge()
        
        com.example.util.VoiceAnnouncementManager.init(this)
        
        setContent {
            val viewModel: FamilyCareViewModel = viewModel()
            val isHighContrast by viewModel.isHighContrast.collectAsState()
            val fontSizeMultiplier by viewModel.fontSizeMultiplier.collectAsState()
            
            MyApplicationTheme(
                dynamicColor = false, 
                darkTheme = true,
                accessibilitySettings = AccessibilitySettings(
                    isHighContrast = isHighContrast,
                    fontSizeMultiplier = fontSizeMultiplier
                )
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Root Navigator matching the chosen screens state flow
                    AppNavigator(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        com.example.util.VoiceAnnouncementManager.shutdown()
    }
}
