package com.claudejobs.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.claudejobs.data.prefs.ApiKeyStore
import com.claudejobs.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val model by viewModel.model.collectAsStateWithLifecycle()
    var keyVisible by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saved) {
        if (saved) {
            snackbarHostState.showSnackbar("Saved")
            saved = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // API Key
            Text("Anthropic API Key", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = viewModel.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = { Text("API key") },
                placeholder = { Text("sk-ant-...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (keyVisible) VisualTransformation.None
                                        else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (keyVisible) "Hide key" else "Show key"
                        )
                    }
                }
            )
            Button(
                onClick = { viewModel.saveApiKey(); saved = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save API Key") }

            HorizontalDivider()

            // Model selector
            Text("Claude Model", style = MaterialTheme.typography.labelMedium)
            val models = listOf(
                ApiKeyStore.DEFAULT_MODEL to "Haiku 4.5 (fast, economical)",
                ApiKeyStore.MODEL_SONNET  to "Sonnet 4.6 (balanced)",
                ApiKeyStore.MODEL_OPUS    to "Opus 4.6 (most capable)"
            )
            models.forEach { (modelId, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = model == modelId,
                        onClick = { viewModel.saveModel(modelId) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Text(modelId, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
