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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mealmuse.domain.model.LLMProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on successful save
    LaunchedEffect(uiState.validationResult) {
        if (uiState.validationResult == true) {
            snackbarHostState.showSnackbar("Settings saved successfully")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row {
                        if (uiState.isValidating) {
                            Text("...", modifier = Modifier.size(20.dp))
                        } else {
                            IconButton(onClick = { viewModel.validateKey() }) {
                                Icon(Icons.Default.Check, contentDescription = "Validate")
                            }
                        }
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide API key" else "Show API key"
                            )
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

            // Model Selector with Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Model", style = MaterialTheme.typography.labelMedium)
                TextButton(
                    onClick = { viewModel.refreshModels() },
                    enabled = !uiState.isRefreshingModels
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (uiState.isRefreshingModels) "Loading..." else "Refresh")
                }
            }

            ExposedDropdownMenuBox(
                expanded = modelDropdownExpanded,
                onExpandedChange = { modelDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.settings.model.ifBlank { "Select a model" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = modelDropdownExpanded,
                    onDismissRequest = { modelDropdownExpanded = false }
                ) {
                    if (uiState.availableModels.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No models available") },
                            onClick = { modelDropdownExpanded = false },
                            enabled = false
                        )
                    } else {
                        uiState.availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    viewModel.selectModel(model)
                                    modelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Model Info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Model Tips:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    when (uiState.settings.provider) {
                        LLMProvider.OPENAI -> Text(
                            "• gpt-4o-mini: Fast & cheap (Recommended)\n• gpt-4o: Most capable\n• gpt-3.5-turbo: Legacy",
                            style = MaterialTheme.typography.bodySmall
                        )
                        LLMProvider.ANTHROPIC -> Text(
                            "• claude-sonnet-4: Latest & best\n• claude-3.5-sonnet: Great balance\n• claude-3-haiku: Fast & cheap",
                            style = MaterialTheme.typography.bodySmall
                        )
                        LLMProvider.OPENROUTER -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "FREE models available!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• gemma-3-4b: Fast & reliable (Recommended)\n• gemma-3-27b: Most capable\n• Try llama for larger context",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        LLMProvider.NIM -> Text(
                            "• NVIDIA NIM for local/self-hosted\n• Enter your server Base URL",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Base URL (for NIM / self-hosted)
            if (uiState.settings.provider == LLMProvider.NIM) {
                OutlinedTextField(
                    value = uiState.settings.baseUrl ?: "",
                    onValueChange = { viewModel.updateBaseUrl(it) },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://your-nim-server/v1") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
                )
            }

            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && uiState.settings.apiKey.isNotBlank()
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save Configuration")
            }

            // Reset to Defaults
            OutlinedButton(
                onClick = { viewModel.resetSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset to Defaults")
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
