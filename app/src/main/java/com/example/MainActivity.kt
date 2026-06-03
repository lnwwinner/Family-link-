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
import com.example.ui.screens.AppNavigator
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge full bleed layout support
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme(dynamicColor = false, darkTheme = true) {
                val viewModel: FamilyCareViewModel = viewModel()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Root Navigator matching the chosen screens state flow
                    AppNavigator(viewModel = viewModel)
                }
            }
        }
    }
}
