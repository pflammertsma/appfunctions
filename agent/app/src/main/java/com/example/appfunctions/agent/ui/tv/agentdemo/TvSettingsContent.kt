/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appfunctions.agent.ui.tv.agentdemo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.appfunctions.agent.BuildConfig
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.data.ServiceTier
import com.example.appfunctions.agent.ui.components.TvSurfaceTextField
import com.example.appfunctions.agent.ui.contracts.SettingsScreenLayout
import kotlinx.coroutines.delay

object TvSettingsLayout : SettingsScreenLayout {
    @Composable
    override fun Content(
        geminiApiKeyState: TextFieldState,
        serviceTier: ServiceTier,
        onServiceTierSelected: (ServiceTier) -> Unit,
        onOpenLicenses: () -> Unit,
        onNavigateToConnectedApps: () -> Unit,
        modifier: Modifier,
    ) {
        TvSettingsContent(
            geminiApiKeyState = geminiApiKeyState,
            serviceTier = serviceTier,
            onServiceTierSelected = onServiceTierSelected,
            onOpenLicenses = onOpenLicenses,
            onNavigateToConnectedApps = onNavigateToConnectedApps,
            modifier = modifier,
        )
    }
}

@Composable
fun TvSettingsContent(
    geminiApiKeyState: TextFieldState,
    serviceTier: ServiceTier,
    onServiceTierSelected: (ServiceTier) -> Unit,
    onOpenLicenses: () -> Unit,
    onNavigateToConnectedApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val apiKeyFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100)
        apiKeyFocusRequester.requestFocus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Unspecified,
        topBar = {
            Row(
                modifier = Modifier.padding(start = 80.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.nav_settings),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = 56.dp)
                    .consumeWindowInsets(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_agent),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp).semantics { heading() },
                )
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_gemini_api_key),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                if (BuildConfig.IS_RETAIL) {
                    TvSurfaceTextField(
                        value = geminiApiKeyState.text.toString(),
                        onValueChange = {},
                        placeholder = stringResource(id = R.string.settings_gemini_api_key),
                        modifier = Modifier.focusRequester(apiKeyFocusRequester).fillMaxWidth(),
                    )
                } else {
                    TvSurfaceTextField(
                        value = geminiApiKeyState.text.toString(),
                        onValueChange = { geminiApiKeyState.setTextAndPlaceCursorAtEnd(it) },
                        placeholder = stringResource(id = R.string.settings_gemini_api_key),
                        modifier = Modifier.focusRequester(apiKeyFocusRequester).fillMaxWidth(),
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_service_tier),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                TvServiceTierDropdown(
                    selectedTier = serviceTier,
                    onTierSelected = onServiceTierSelected,
                )
                Text(
                    text = stringResource(id = R.string.settings_service_tier_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            var isAppsFocused by remember { mutableStateOf(false) }
            Surface(
                onClick = onNavigateToConnectedApps,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .onFocusChanged { isAppsFocused = it.isFocused },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border =
                    if (isAppsFocused) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.manage_connected_apps),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.settings_about),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp).semantics { heading() },
                )
            }

            var isLicensesFocused by remember { mutableStateOf(false) }
            Surface(
                onClick = onOpenLicenses,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .onFocusChanged { isLicensesFocused = it.isFocused },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border =
                    if (isLicensesFocused) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.settings_open_source_licenses),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    leadingContent = {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun TvServiceTierDropdown(
    selectedTier: ServiceTier,
    onTierSelected: (ServiceTier) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = { showDialog = true },
        modifier =
            Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
        shape = MaterialTheme.shapes.extraLarge,
        color = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceBright,
        border =
            if (isFocused) {
                BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = selectedTier.labelRes()),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_service_tier),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                    ServiceTier.entries.forEach { tier ->
                        var isItemFocused by remember { mutableStateOf(false) }
                        Surface(
                            onClick = {
                                onTierSelected(tier)
                                showDialog = false
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .onFocusChanged { isItemFocused = it.isFocused },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isItemFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = if (isItemFocused) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(id = tier.labelRes()),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (tier == selectedTier) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ServiceTier.labelRes(): Int =
    when (this) {
        ServiceTier.STANDARD -> R.string.settings_service_tier_standard
        ServiceTier.PRIORITY -> R.string.settings_service_tier_priority
    }

