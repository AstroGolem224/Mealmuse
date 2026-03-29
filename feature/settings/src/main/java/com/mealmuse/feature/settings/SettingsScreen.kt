package com.mealmuse.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mealmuse.domain.model.LLMProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("AI Configuration", style = MaterialTheme.typography.titleMedium)

            // Provider Selector
            Text("Provider", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LLMProvider.entries.toTypedArray()) { provider ->
                    FilterChip(
                        selected = uiState.settings.provider == provider,
                        onClick = { viewModel.selectProvider(provider) },
                        label = { Text(provider.displayName) }
                    )
                }
            }

            // API Key
            OutlinedTextField(
                value = uiState.settings.apiKey,
                onValueChange = { viewModel.updateApiKey(it) },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    Row {
                        if (uiState.isValidating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            IconButton(onClick = { viewModel.validateKey() }) {
                                Icon(Icons.Default.Check, contentDescription = "Validate")
                            }
                        }
                    }
                },
                supportingText = {
                    when (uiState.validationResult) {
                        true -> Text("Valid key", color = MaterialTheme.colorScheme.primary)
                        false -> Text("Invalid key", color = MaterialTheme.colorScheme.error)
                        null -> null
                    }
                }
            )

            // Model Selector
            Text("Model", style = MaterialTheme.typography.labelMedium)
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = uiState.settings.model,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Model") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(
                    expanded = false,
                    onDismissRequest = {}
                ) {
                    uiState.availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = { viewModel.selectModel(model) }
                        )
                    }
                }
            }

            // Base URL (for NIM / self-hosted)
            if (uiState.settings.provider == LLMProvider.NIM) {
                OutlinedTextField(
                    value = uiState.settings.baseUrl ?: "",
                    onValueChange = { viewModel.updateBaseUrl(it) },
                    label = { Text("Base URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://your-nim-server/v1") }
                )
            }

            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(error, modifier = Modifier.padding(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save Configuration")
                }
            }

            // Status
            if (uiState.settings.isActive) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI is configured and active")
                    }
                }
            }
        }
    }
}
