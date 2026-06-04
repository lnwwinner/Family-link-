package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.util.LanguageManager
import com.example.R

@Composable
fun LanguageSettingsScreen() {
    val languages = LanguageManager.getSupportedLanguages()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(id = R.string.language_settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn {
            items(languages.entries.toList()) { (code, name) ->
                ListItem(
                    headlineContent = { Text(name) },
                    modifier = Modifier.clickable {
                        LanguageManager.setLocale(code)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
