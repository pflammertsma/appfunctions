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
package com.example.appfunctions.agent.ui.mobile.debugging

import android.app.PendingIntent
import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.appfunctions.metadata.AppFunctionMetadata
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.domain.appfunction.AppInfo
import com.example.appfunctions.agent.ui.contracts.DebuggingScreenLayout
import com.example.appfunctions.agent.ui.screens.debugging.AppsGroupState
import com.example.appfunctions.agent.ui.screens.debugging.DebuggingUiState
import com.example.appfunctions.agent.ui.screens.debugging.FunctionsFoundContent
import com.example.appfunctions.agent.ui.screens.debugging.SearchAppResultState
import com.example.appfunctions.agent.ui.screens.debugging.TroubleshootResult

object MobileDebuggingLayout : DebuggingScreenLayout {
    @Composable
    override fun Content(
        uiState: DebuggingUiState,
        onSearchQueryChanged: (String) -> Unit,
        onAppSelected: (AppInfo) -> Unit,
        onClearSelectedApp: () -> Unit,
        onFunctionInputsChange: (String, Map<String, Any>) -> Unit,
        onInvoke: (AppFunctionMetadata) -> Unit,
        onClearResult: () -> Unit,
        onFunctionExpandedChange: (String, Boolean) -> Unit,
        onLaunchPendingIntent: (PendingIntent) -> Unit,
        onTogglePin: (AppInfo) -> Unit,
        modifier: Modifier,
    ) {
        MobileDebuggingContent(
            uiState = uiState,
            onSearchQueryChanged = onSearchQueryChanged,
            onAppSelected = onAppSelected,
            onClearSelectedApp = onClearSelectedApp,
            onFunctionInputsChange = onFunctionInputsChange,
            onInvoke = onInvoke,
            onClearResult = onClearResult,
            onFunctionExpandedChange = onFunctionExpandedChange,
            onLaunchPendingIntent = onLaunchPendingIntent,
            onTogglePin = onTogglePin,
            modifier = modifier,
        )
    }
}

@Composable
fun MobileDebuggingContent(
    uiState: DebuggingUiState,
    onSearchQueryChanged: (String) -> Unit,
    onAppSelected: (AppInfo) -> Unit,
    onClearSelectedApp: () -> Unit,
    onFunctionInputsChange: (String, Map<String, Any>) -> Unit,
    onInvoke: (AppFunctionMetadata) -> Unit,
    onClearResult: () -> Unit,
    onFunctionExpandedChange: (String, Boolean) -> Unit,
    onLaunchPendingIntent: (PendingIntent) -> Unit,
    onTogglePin: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.selectedApp == null) {
        // STEP 1: App List Screen
        MobileAppListScreen(
            appGroups = uiState.filteredApps,
            searchQuery = uiState.searchQuery,
            isLoading = uiState.isLoading,
            onSearchQueryChanged = onSearchQueryChanged,
            onAppSelected = onAppSelected,
            onTogglePin = onTogglePin,
            modifier = modifier,
        )
    } else {
        BackHandler {
            onClearSelectedApp()
        }

        // STEP 2: App Functions Detail Screen
        MobileFunctionsDetailScreen(
            selectedApp = uiState.selectedApp,
            searchAppResultState = uiState.searchAppResultState,
            uiState = uiState,
            onBack = onClearSelectedApp,
            onFunctionInputsChange = onFunctionInputsChange,
            onInvoke = onInvoke,
            onClearResult = onClearResult,
            onFunctionExpandedChange = onFunctionExpandedChange,
            onLaunchPendingIntent = onLaunchPendingIntent,
            onTogglePin = onTogglePin,
            modifier = modifier,
        )
    }
}

/** Step 1: Full-Screen Installed Apps List on Mobile */
@Composable
private fun MobileAppListScreen(
    appGroups: AppsGroupState,
    searchQuery: String,
    isLoading: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onAppSelected: (AppInfo) -> Unit,
    onTogglePin: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = appGroups.sections
    val pinnedPackageNames =
        remember(sections) {
            sections
                .find { it.titleRes == Resources.ID_NULL }
                ?.apps
                ?.map { it.packageName }
                ?.toSet() ?: emptySet()
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Unspecified,
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = stringResource(id = R.string.debugging_installed_apps_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text(stringResource(R.string.debugging_search_app)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon =
                        if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                        } else {
                            null
                        },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (sections.all { it.apps.isEmpty() }) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.debugging_select_app_prompt),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    sections.forEach { section ->
                        if (section.apps.isNotEmpty()) {
                            item {
                                if (section.titleRes != Resources.ID_NULL) {
                                    Column(
                                        modifier =
                                            Modifier.padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 16.dp,
                                                bottom = 4.dp,
                                            ),
                                    ) {
                                        Text(
                                            text = stringResource(section.titleRes),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(top = 4.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                        )
                                    }
                                }
                            }
                            items(section.apps, key = { app -> "${section.titleRes}_${app.packageName}" }) { app ->
                                val isPinned = pinnedPackageNames.contains(app.packageName)
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = app.label,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = app.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    leadingContent =
                                        if (app.icon != null) {
                                            {
                                                Image(
                                                    bitmap = app.icon.toBitmap().asImageBitmap(),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(32.dp),
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                    trailingContent =
                                        if (section.showPin) {
                                            {
                                                IconButton(onClick = { onTogglePin(app) }) {
                                                    Icon(
                                                        imageVector =
                                                            if (isPinned) {
                                                                Icons.Filled.PushPin
                                                            } else {
                                                                Icons.Outlined.PushPin
                                                            },
                                                        contentDescription = if (isPinned) "Unpin" else "Pin",
                                                        tint =
                                                            if (isPinned) {
                                                                MaterialTheme.colorScheme.primary
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                    )
                                                }
                                            }
                                        } else {
                                            null
                                        },
                                    modifier =
                                        Modifier.clickable {
                                            onSearchQueryChanged(app.label)
                                            onAppSelected(app)
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Step 2: Functions Detail Screen for Selected App on Mobile */
@Composable
private fun MobileFunctionsDetailScreen(
    selectedApp: AppInfo,
    searchAppResultState: SearchAppResultState,
    uiState: DebuggingUiState,
    onBack: () -> Unit,
    onFunctionInputsChange: (String, Map<String, Any>) -> Unit,
    onInvoke: (AppFunctionMetadata) -> Unit,
    onClearResult: () -> Unit,
    onFunctionExpandedChange: (String, Boolean) -> Unit,
    onLaunchPendingIntent: (PendingIntent) -> Unit,
    onTogglePin: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPinned =
        remember(uiState.filteredApps, selectedApp.packageName) {
            uiState.filteredApps.sections
                .firstOrNull { it.titleRes == R.string.debugging_pinned_apps }
                ?.apps
                ?.any { it.packageName == selectedApp.packageName } == true
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Unspecified,
        topBar = {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to installed apps",
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (selectedApp.icon != null) {
                    Image(
                        bitmap = selectedApp.icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedApp.label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = selectedApp.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onTogglePin(selectedApp) }) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (isPinned) "Unpin App" else "Pin App",
                        tint =
                            if (isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
        ) {
            when (searchAppResultState) {
                is SearchAppResultState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SearchAppResultState.FunctionsFoundState -> {
                    FunctionsFoundContent(
                        state = searchAppResultState,
                        onFunctionExpandedChange = onFunctionExpandedChange,
                        onFunctionInputsChange = onFunctionInputsChange,
                        onInvoke = onInvoke,
                        onClearResult = onClearResult,
                        onLaunchPendingIntent = onLaunchPendingIntent,
                    )
                }
                is SearchAppResultState.TroubleshootUiState -> {
                    TroubleshootResult(
                        state = searchAppResultState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
