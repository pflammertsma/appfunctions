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
package com.example.appfunctions.agent.ui.tv.debugging

import android.app.PendingIntent
import androidx.activity.compose.BackHandler
import androidx.appfunctions.metadata.AppFunctionMetadata
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.domain.appfunction.AppInfo
import com.example.appfunctions.agent.domain.appfunction.ExecuteAppFunctionResult
import com.example.appfunctions.agent.ui.components.TvSurfaceTextField
import com.example.appfunctions.agent.ui.contracts.DebuggingScreenLayout
import com.example.appfunctions.agent.ui.screens.debugging.AppFunctionDataTypeInput
import com.example.appfunctions.agent.ui.screens.debugging.DebuggingUiState
import com.example.appfunctions.agent.ui.screens.debugging.SearchAppResultState
import com.example.appfunctions.agent.ui.screens.debugging.TroubleshootResult
import com.example.appfunctions.agent.ui.screens.debugging.createDefaultValue
import com.example.appfunctions.agent.ui.theme.GoogleSansCodeFontFamily
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as columnItemsIndexed

object TvDebuggingLayout : DebuggingScreenLayout {
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
        TvDebuggingContent(
            uiState = uiState,
            onSearchQueryChanged = onSearchQueryChanged,
            onAppSelected = onAppSelected,
            onClearSelectedApp = onClearSelectedApp,
            onFunctionInputsChange = onFunctionInputsChange,
            onInvoke = onInvoke,
            onClearResult = onClearResult,
            onTogglePin = onTogglePin,
            modifier = modifier,
        )
    }
}

@Composable
fun TvDebuggingContent(
    uiState: DebuggingUiState,
    onSearchQueryChanged: (String) -> Unit,
    onAppSelected: (AppInfo) -> Unit,
    onClearSelectedApp: () -> Unit,
    onFunctionInputsChange: (String, Map<String, Any>) -> Unit,
    onInvoke: (AppFunctionMetadata) -> Unit,
    onClearResult: () -> Unit,
    onTogglePin: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeExecutionFunction by remember { mutableStateOf<AppFunctionMetadata?>(null) }
    var lastSelectedAppPackageName by remember { mutableStateOf<String?>(null) }
    var lastSelectedFunctionId by remember { mutableStateOf<String?>(null) }

    if (uiState.selectedApp == null) {
        // STEP 1: TV App List / Search Grid
        TvAppListScreen(
            uiState = uiState,
            onSearchQueryChanged = onSearchQueryChanged,
            onAppSelected = { app ->
                lastSelectedAppPackageName = app.packageName
                onAppSelected(app)
            },
            onClearSearch = onClearSelectedApp,
            lastSelectedAppPackageName = lastSelectedAppPackageName,
            modifier = modifier,
        )
    } else if (activeExecutionFunction == null) {
        BackHandler {
            onClearSelectedApp()
        }

        // STEP 2: TV Functions Screen for Selected App
        TvFunctionListScreen(
            selectedApp = uiState.selectedApp,
            searchAppResultState = uiState.searchAppResultState,
            uiState = uiState,
            onBack = { onClearSelectedApp() },
            onSelectFunction = { function ->
                lastSelectedFunctionId = function.id
                activeExecutionFunction = function
            },
            lastSelectedFunctionId = lastSelectedFunctionId,
            onTogglePin = onTogglePin,
            modifier = modifier,
        )
    } else {
        // STEP 3: TV Function Execution Screen (Full Screen)
        val function = activeExecutionFunction!!
        val functionsState = uiState.searchAppResultState as? SearchAppResultState.FunctionsFoundState
        val inputValues = functionsState?.functionInputs?.get(function.id) ?: emptyMap()

        BackHandler {
            activeExecutionFunction = null
        }

        TvFunctionExecutionScreen(
            function = function,
            selectedApp = uiState.selectedApp,
            inputValues = inputValues,
            onBack = { activeExecutionFunction = null },
            onInputValuesChange = { inputs ->
                onFunctionInputsChange(function.id, inputs)
            },
            onInvoke = {
                onInvoke(function)
            },
            modifier = modifier,
        )
    }

    // Execution Result Dialog
    val executionResult = (uiState.searchAppResultState as? SearchAppResultState.FunctionsFoundState)?.executionResult
    if (executionResult != null) {
        val okButtonFocusRequester = remember { FocusRequester() }
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(executionResult) {
            okButtonFocusRequester.requestFocus()
        }

        Dialog(
            onDismissRequest = onClearResult,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp, vertical = 32.dp)
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.DirectionUp -> {
                                        coroutineScope.launch {
                                            scrollState.animateScrollBy(-200f)
                                        }
                                        true
                                    }

                                    Key.DirectionDown -> {
                                        coroutineScope.launch {
                                            scrollState.animateScrollBy(200f)
                                        }
                                        true
                                    }

                                    else -> false
                                }
                            } else {
                                false
                            }
                        },
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text =
                            when (executionResult) {
                                is ExecuteAppFunctionResult.Data -> "Execution Result"
                                is ExecuteAppFunctionResult.Error -> "Execution Error"
                                is ExecuteAppFunctionResult.PendingIntentAction -> "Action Required"
                            },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (executionResult is ExecuteAppFunctionResult.Error) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                    ) {
                        SelectionContainer {
                            Text(
                                text =
                                    when (executionResult) {
                                        is ExecuteAppFunctionResult.Data -> executionResult.formattedJson
                                        is ExecuteAppFunctionResult.Error -> executionResult.exception.message ?: "Unknown error"
                                        is ExecuteAppFunctionResult.PendingIntentAction -> "Pending Intent Action required"
                                    },
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = GoogleSansCodeFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    var isOkFocused by remember { mutableStateOf(false) }
                    val okScale by animateFloatAsState(if (isOkFocused) 1.04f else 1.0f, label = "okScale")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Surface(
                            onClick = onClearResult,
                            modifier =
                                Modifier
                                    .scale(okScale)
                                    .focusRequester(okButtonFocusRequester)
                                    .onFocusChanged { isOkFocused = it.isFocused },
                            shape = MaterialTheme.shapes.medium,
                            color =
                                if (isOkFocused) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                            border =
                                if (isOkFocused) {
                                    BorderStroke(2.5.dp, MaterialTheme.colorScheme.onPrimaryContainer)
                                } else {
                                    null
                                },
                        ) {
                            Text(
                                text = "OK",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color =
                                    if (isOkFocused) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** STEP 1: TV App Grid / Search Screen */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TvAppListScreen(
    uiState: DebuggingUiState,
    onSearchQueryChanged: (String) -> Unit,
    onAppSelected: (AppInfo) -> Unit,
    onClearSearch: () -> Unit,
    lastSelectedAppPackageName: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(start = 80.dp, top = 16.dp, end = 24.dp, bottom = 24.dp)
                .focusGroup()
                .focusRestorer(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.debugging_installed_apps_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(modifier = Modifier.weight(1f))
            TvSurfaceTextField(
                value = uiState.searchQuery,
                placeholder = stringResource(R.string.debugging_search_app),
                onValueChange = onSearchQueryChanged,
                trailingIcon =
                    if (uiState.searchQuery.isNotEmpty()) {
                        {
                            var isClearFocused by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = onClearSearch,
                                modifier = Modifier.onFocusChanged { isClearFocused = it.isFocused },
                                colors =
                                    IconButtonDefaults.colors(
                                        containerColor = Color.Transparent,
                                        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    tint =
                                        if (isClearFocused) {
                                            MaterialTheme.colorScheme.inverseOnSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                            }
                        }
                    } else {
                        null
                    },
                modifier = Modifier.width(320.dp),
            )
        }

        val hasApps = uiState.filteredApps.sections.any { it.apps.isNotEmpty() }
        val targetAppFocusRequester = remember { FocusRequester() }
        val firstAppFocusRequester = remember { FocusRequester() }
        var focusRequested by remember(uiState.searchQuery) { mutableStateOf(false) }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (!hasApps) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.debugging_select_app_prompt),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .focusGroup()
                        .focusRestorer(),
            ) {
                uiState.filteredApps.sections.forEachIndexed { sectionIndex, section ->
                    if (section.apps.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                                Text(
                                    text = stringResource(section.titleRes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }

                        gridItemsIndexed(
                            items = section.apps,
                            key = { _, app -> "${section.titleRes}_${app.packageName}" },
                        ) { index, app ->
                            val isTargetApp = app.packageName == lastSelectedAppPackageName
                            val isFirstAppOverall = sectionIndex == 0 && index == 0
                            val shouldFocus = isTargetApp || (lastSelectedAppPackageName == null && isFirstAppOverall)

                            var isCardFocused by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                if (isCardFocused) 1.04f else 1.0f,
                                label = "appCardScale",
                            )

                            Surface(
                                onClick = { onAppSelected(app) },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .scale(scale)
                                        .then(
                                            if (isTargetApp) {
                                                Modifier.focusRequester(
                                                    targetAppFocusRequester,
                                                )
                                            } else if (isFirstAppOverall) {
                                                Modifier.focusRequester(
                                                    firstAppFocusRequester,
                                                )
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .then(
                                            if (shouldFocus) {
                                                Modifier.onGloballyPositioned {
                                                    if (!focusRequested) {
                                                        if (isTargetApp) {
                                                            targetAppFocusRequester.requestFocus()
                                                        } else {
                                                            firstAppFocusRequester.requestFocus()
                                                        }
                                                        focusRequested = true
                                                    }
                                                }
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .onFocusChanged { isCardFocused = it.isFocused },
                                shape = MaterialTheme.shapes.medium,
                                color =
                                    if (isCardFocused) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    },
                                border =
                                    if (isCardFocused) {
                                        BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        null
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (app.icon != null) {
                                        Image(
                                            bitmap = app.icon.toBitmap().asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                        )
                                    } else {
                                        Box(modifier = Modifier.size(36.dp))
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color =
                                                if (isCardFocused) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                        )
                                        Text(
                                            text = app.packageName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = GoogleSansCodeFontFamily,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color =
                                                if (isCardFocused) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
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
    }
}

/** STEP 2: TV Functions Screen for Selected App */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TvFunctionListScreen(
    selectedApp: AppInfo,
    searchAppResultState: SearchAppResultState,
    uiState: DebuggingUiState,
    onBack: () -> Unit,
    onSelectFunction: (AppFunctionMetadata) -> Unit,
    lastSelectedFunctionId: String?,
    onTogglePin: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetItemFocusRequester = remember { FocusRequester() }
    val firstItemFocusRequester = remember { FocusRequester() }
    var focusRequested by remember(selectedApp.packageName) { mutableStateOf(false) }

    val isPinned =
        remember(uiState.filteredApps, selectedApp.packageName) {
            uiState.filteredApps.sections
                .firstOrNull { it.titleRes == R.string.debugging_pinned_apps }
                ?.apps
                ?.any { it.packageName == selectedApp.packageName } == true
        }

    val functions =
        (searchAppResultState as? SearchAppResultState.FunctionsFoundState)
            ?.functions ?: emptyList()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(start = 80.dp, top = 36.dp, end = 24.dp, bottom = 24.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var isBackFocused by remember { mutableStateOf(false) }
            IconButton(
                onClick = onBack,
                modifier = Modifier.onFocusChanged { isBackFocused = it.isFocused },
                colors =
                    IconButtonDefaults.colors(
                        containerColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                    ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint =
                        if (isBackFocused) {
                            MaterialTheme.colorScheme.inverseOnSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (selectedApp.icon != null) {
                Image(
                    bitmap = selectedApp.icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedApp.label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = selectedApp.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = GoogleSansCodeFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            var isPinFocused by remember { mutableStateOf(false) }
            IconButton(
                onClick = { onTogglePin(selectedApp) },
                modifier = Modifier.onFocusChanged { isPinFocused = it.isFocused },
                colors =
                    IconButtonDefaults.colors(
                        containerColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                    ),
            ) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) "Unpin App" else "Pin App",
                    tint =
                        if (isPinFocused) {
                            MaterialTheme.colorScheme.inverseOnSurface
                        } else if (isPinned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (searchAppResultState) {
                is SearchAppResultState.FunctionsFoundState -> {
                    if (functions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No App Functions found for this app.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Functions",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )

                            LazyColumn(
                                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .focusGroup()
                                        .focusRestorer(),
                            ) {
                                columnItemsIndexed(items = functions, key = { _, fn -> fn.id }) { index, function ->
                                    val isTargetFunction = function.id == lastSelectedFunctionId
                                    val isFirstItem = index == 0

                                    var isFunctionFocused by remember { mutableStateOf(false) }
                                    val scale by animateFloatAsState(
                                        if (isFunctionFocused) 1.03f else 1.0f,
                                        label = "fnScale",
                                    )

                                    val hashIndex = function.id.indexOf('#')
                                    val name =
                                        if (hashIndex != -1) {
                                            function.id.substring(hashIndex + 1)
                                        } else {
                                            function.id
                                        }

                                    val shouldFocus = isTargetFunction || (lastSelectedFunctionId == null && isFirstItem)

                                    Surface(
                                        onClick = { onSelectFunction(function) },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .scale(scale)
                                                .zIndex(if (isFunctionFocused) 1f else 0f)
                                                .then(
                                                    if (isTargetFunction) {
                                                        Modifier.focusRequester(
                                                            targetItemFocusRequester,
                                                        )
                                                    } else if (isFirstItem) {
                                                        Modifier.focusRequester(
                                                            firstItemFocusRequester,
                                                        )
                                                    } else {
                                                        Modifier
                                                    },
                                                )
                                                .then(
                                                    if (shouldFocus) {
                                                        Modifier.onGloballyPositioned {
                                                            if (!focusRequested) {
                                                                if (isTargetFunction) {
                                                                    targetItemFocusRequester.requestFocus()
                                                                } else {
                                                                    firstItemFocusRequester.requestFocus()
                                                                }
                                                                focusRequested = true
                                                            }
                                                        }
                                                    } else {
                                                        Modifier
                                                    },
                                                )
                                                .onFocusChanged { isFunctionFocused = it.isFocused },
                                        shape = MaterialTheme.shapes.medium,
                                        color =
                                            if (isFunctionFocused) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerLow
                                            },
                                        border =
                                            if (isFunctionFocused) {
                                                BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                                            } else {
                                                null
                                            },
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color =
                                                    if (isFunctionFocused) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.ic_rounded_function),
                                                        contentDescription = null,
                                                        tint =
                                                            if (isFunctionFocused) {
                                                                MaterialTheme.colorScheme.onPrimary
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color =
                                                        if (isFunctionFocused) {
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        },
                                                )
                                                Text(
                                                    text = function.id,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = GoogleSansCodeFontFamily,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color =
                                                        if (isFunctionFocused) {
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        },
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                            ) {
                                                Text(
                                                    text = "${function.parameters.size} params",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is SearchAppResultState.TroubleshootUiState -> {
                    TroubleshootResult(state = searchAppResultState, modifier = Modifier.fillMaxWidth())
                }
                else -> {}
            }
        }
    }
}

/** STEP 3: TV Function Execution Screen */
@Composable
private fun TvFunctionExecutionScreen(
    function: AppFunctionMetadata,
    selectedApp: AppInfo,
    inputValues: Map<String, Any>,
    onBack: () -> Unit,
    onInputValuesChange: (Map<String, Any>) -> Unit,
    onInvoke: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hashIndex = function.id.indexOf('#')
    val name = if (hashIndex != -1) function.id.substring(hashIndex + 1) else function.id

    val firstParamFocusRequester = remember { FocusRequester() }
    val runButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(function.id) {
        if (function.parameters.isNotEmpty()) {
            firstParamFocusRequester.requestFocus()
        } else {
            runButtonFocusRequester.requestFocus()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(start = 80.dp, top = 36.dp, end = 24.dp, bottom = 24.dp),
    ) {
        // Header Row
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var isBackFocused by remember { mutableStateOf(false) }
            IconButton(
                onClick = onBack,
                modifier = Modifier.onFocusChanged { isBackFocused = it.isFocused },
                colors =
                    IconButtonDefaults.colors(
                        containerColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                    ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint =
                        if (isBackFocused) {
                            MaterialTheme.colorScheme.inverseOnSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            if (selectedApp.icon != null) {
                Image(
                    bitmap = selectedApp.icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.width(14.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = function.id,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = GoogleSansCodeFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (function.parameters.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "This function takes no parameters.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                text = "Parameters",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            ) {
                columnItemsIndexed(
                    items = function.parameters,
                    key = { _, param -> param.name },
                ) { index, parameter ->
                    val currentValue =
                        inputValues[parameter.name]
                            ?: createDefaultValue(parameter.dataType)

                    val isArray = parameter.dataType is androidx.appfunctions.metadata.AppFunctionArrayTypeMetadata

                    @Composable
                    fun ResetButton() {
                        var isResetFocused by remember { mutableStateOf(false) }
                        val resetScale by animateFloatAsState(
                            if (isResetFocused) 1.05f else 1.0f,
                            label = "resetScale",
                        )

                        Surface(
                            onClick = {
                                val defaultValue = createDefaultValue(parameter.dataType)
                                onInputValuesChange(inputValues + (parameter.name to defaultValue))
                            },
                            modifier =
                                Modifier
                                    .scale(resetScale)
                                    .onFocusChanged { isResetFocused = it.isFocused },
                            shape = MaterialTheme.shapes.medium,
                            color =
                                if (isResetFocused) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                            border =
                                if (isResetFocused) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                                } else {
                                    null
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset parameter",
                                    tint =
                                        if (isResetFocused) {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        if (isResetFocused) {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                )
                            }
                        }
                    }

                    if (isArray) {
                        var isItemFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isItemFocused = it.hasFocus }
                                    .zIndex(if (isItemFocused) 1f else 0f)
                                    .then(
                                        if (index == 0) {
                                            Modifier.focusRequester(
                                                firstParamFocusRequester,
                                            )
                                        } else {
                                            Modifier
                                        },
                                    ),
                        ) {
                            AppFunctionDataTypeInput(
                                dataType = parameter.dataType,
                                value = currentValue,
                                onValueChange = { value ->
                                    onInputValuesChange(inputValues + (parameter.name to value))
                                },
                                components = function.components,
                                label = parameter.name,
                                isRequired = parameter.isRequired,
                                trailingAction = { ResetButton() },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        var isItemFocused by remember { mutableStateOf(false) }
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isItemFocused = it.hasFocus }
                                    .zIndex(if (isItemFocused) 1f else 0f),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .then(
                                            if (index == 0) {
                                                Modifier.focusRequester(
                                                    firstParamFocusRequester,
                                                )
                                            } else {
                                                Modifier
                                            },
                                        ),
                            ) {
                                AppFunctionDataTypeInput(
                                    dataType = parameter.dataType,
                                    value = currentValue,
                                    onValueChange = { value ->
                                        onInputValuesChange(inputValues + (parameter.name to value))
                                    },
                                    components = function.components,
                                    label = parameter.name,
                                    isRequired = parameter.isRequired,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            ResetButton()
                        }
                    }
                }
            }
        }

        // Run Button positioned at the bottom right
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var isRunFocused by remember { mutableStateOf(false) }
            val runScale by animateFloatAsState(
                if (isRunFocused) 1.04f else 1.0f,
                label = "runScale",
            )

            Surface(
                onClick = onInvoke,
                modifier =
                    Modifier
                        .scale(runScale)
                        .focusRequester(runButtonFocusRequester)
                        .onFocusChanged { isRunFocused = it.isFocused },
                shape = MaterialTheme.shapes.large,
                color =
                    if (isRunFocused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                border =
                    if (isRunFocused) {
                        BorderStroke(2.5.dp, MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        null
                    },
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint =
                            if (isRunFocused) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.debugging_invoke),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (isRunFocused) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                    )
                }
            }
        }
    }
}
