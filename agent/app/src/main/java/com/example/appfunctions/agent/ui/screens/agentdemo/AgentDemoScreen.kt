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
package com.example.appfunctions.agent.ui.screens.agentdemo

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Extension
import org.json.JSONObject
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.data.LlmModel
import com.example.appfunctions.agent.data.db.entities.MessageEntity
import com.example.appfunctions.agent.data.db.entities.MessageProcessingStatus
import com.example.appfunctions.agent.data.db.entities.MessageRole
import com.example.appfunctions.agent.data.db.entities.ThreadEntity
import com.example.appfunctions.agent.domain.AgentStatus
import com.example.appfunctions.agent.domain.appfunction.AppInfo
import com.example.appfunctions.agent.ui.contracts.AgentDemoScreenLayout
import com.example.appfunctions.agent.ui.layout.FormFactor
import com.example.appfunctions.agent.ui.layout.rememberFormFactor
import com.example.appfunctions.agent.ui.mobile.agentdemo.MobileAgentDemoLayout
import com.example.appfunctions.agent.ui.screens.debugging.LazyExposedDropdownMenu
import com.example.appfunctions.agent.ui.tv.agentdemo.TvAgentDemoLayout
import com.mikepenz.markdown.m3.Markdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AgentDemoScreen(viewModel: AgentDemoViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AgentDemoContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
fun AgentDemoContent(
    uiState: AgentUiState,
    onEvent: (AgentUiEvent) -> Unit,
    initialSidePanelVisible: Boolean = false,
) {
    val formFactor = rememberFormFactor()
    val layout: AgentDemoScreenLayout = when (formFactor) {
        FormFactor.TV -> TvAgentDemoLayout
        FormFactor.WEAR, FormFactor.AUTO, FormFactor.XR, FormFactor.MOBILE -> MobileAgentDemoLayout
    }

    layout.Content(
        uiState = uiState,
        onEvent = onEvent,
        initialSidePanelVisible = initialSidePanelVisible,
        modifier = Modifier,
    )
}

@Composable
fun AgentDemoLoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDemoLoadedScreen(
    uiState: AgentUiState.Loaded,
    onEvent: (AgentUiEvent) -> Unit,
    isWideScreen: Boolean,
    drawerState: DrawerState,
    scope: CoroutineScope,
    packageManager: PackageManager,
    initialSidePanelVisible: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var isSidePanelVisible by remember { mutableStateOf(initialSidePanelVisible) }
    var selectedAppPackageName by remember { mutableStateOf<String?>(null) }
    val isTv = rememberFormFactor() == FormFactor.TV
    var showHistoryDialog by remember { mutableStateOf(false) }

    val inputFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val currentThreadId = uiState.currentThread.threadId
    var hasInitiallyScrolled by remember(currentThreadId) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages, currentThreadId) {
        if (uiState.messages.isNotEmpty() && !hasInitiallyScrolled) {
            hasInitiallyScrolled = true
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            if (listState.firstVisibleItemIndex == 0) {
                listState.animateScrollToItem(0)
            }
        }
    }

    if (isTv) {
        LaunchedEffect(Unit) {
            delay(100)
            inputFocusRequester.requestFocus()
        }
    }

    val chipBgColor = MaterialTheme.colorScheme.primaryContainer
    val chipTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    val visualTransformation =
        remember(uiState.installedApps, chipTextColor) {
            InlineAppScopingVisualTransformation(uiState.installedApps, chipTextColor)
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Unspecified,
        topBar = {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isTv) {
                    Text(
                        text = stringResource(R.string.agent_demo_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                    )
                } else {
                    ModelDropdown(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        currentThread = uiState.currentThread,
                        onModelSelected = { onEvent(AgentUiEvent.OnModelSelected(it)) },
                        onMenuClick = {
                            if (isWideScreen) {
                                isSidePanelVisible = !isSidePanelVisible
                            } else {
                                scope.launch { drawerState.open() }
                            }
                        },
                    )
                    IconButton(
                        onClick = {
                            onEvent(AgentUiEvent.OnCreateThread(uiState.currentThread.llmModel))
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create Thread")
                    }
                }
            }
        },
    ) { paddingValues ->
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                    ),
        ) {
            // Side Panel (only for wide screens)
            if (isWideScreen && !isTv) {
                AnimatedVisibility(
                    visible = isSidePanelVisible,
                    enter = slideInHorizontally() + expandHorizontally(),
                    exit = slideOutHorizontally() + shrinkHorizontally(),
                ) {
                    ChatHistorySidePanel(
                        threads = uiState.threads,
                        currentThread = uiState.currentThread,
                        onEvent = onEvent,
                    )
                }
            }

            // Main Chat Area
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                    reverseLayout = true,
                ) {
                    // Status item at the bottom (above input) if not
                    // idle
                    if (uiState.status != AgentStatus.Idle) {
                        item {
                            StatusIndicator(
                                status = uiState.status,
                                packageManager = packageManager,
                            )
                        }
                    }

                    items(
                        items = uiState.messages.reversed(),
                        key = { message -> message.messageId },
                    ) { message ->
                        MessageBubble(
                            message = message,
                            isValidAction =
                                message.pendingIntentId in uiState.activePendingActionIds,
                            installedApps = uiState.installedApps,
                            onConfirmAction = { onEvent(AgentUiEvent.OnConfirmAction(it)) },
                        )
                    }
                }

                val sendMessage = {
                    val textStr = messageText.text
                    if (textStr.isNotBlank()) {
                        onEvent(AgentUiEvent.OnSendMessage(textStr, selectedAppPackageName))
                        messageText = TextFieldValue("")
                        selectedAppPackageName = null
                        inputFocusRequester.requestFocus()
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                }

                val textStr = messageText.text
                val lastAtIndex = textStr.lastIndexOf('@')
                val showAutocomplete =
                    lastAtIndex >= 0 &&
                        (lastAtIndex == 0 || textStr[lastAtIndex - 1].isWhitespace()) &&
                        selectedAppPackageName == null
                val autocompleteQuery =
                    if (showAutocomplete) {
                        textStr.substring(lastAtIndex + 1)
                    } else {
                        ""
                    }
                val filteredApps =
                    remember(autocompleteQuery, uiState.installedApps) {
                        if (autocompleteQuery.isEmpty()) {
                            uiState.installedApps
                        } else {
                            uiState.installedApps.filter {
                                it.label.contains(autocompleteQuery, ignoreCase = true)
                            }
                        }
                    }

                val density = LocalDensity.current
                val popupPositionProvider =
                    remember(density) {
                        object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize,
                            ): IntOffset {
                                val gap = with(density) { 2.dp.roundToPx() }
                                return IntOffset(
                                    x = anchorBounds.left,
                                    y = anchorBounds.top - popupContentSize.height - gap,
                                )
                            }
                        }
                    }

                val appMentionRegex =
                    remember(uiState.installedApps) {
                        if (uiState.installedApps.isNotEmpty()) {
                            val appLabelsPattern =
                                uiState.installedApps.joinToString("|") { Regex.escape(it.label) }
                            Regex("@($appLabelsPattern)\\b", RegexOption.IGNORE_CASE)
                        } else {
                            null
                        }
                    }

                val isTv = rememberFormFactor() == FormFactor.TV

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 16.dp)
                        .onPreviewKeyEvent { keyEvent ->
                            android.util.Log.d("JetskiDebug", "Row onPreviewKeyEvent: keyEvent=$keyEvent")
                            if (isTv && keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.DirectionUp -> {
                                        val moved = focusManager.moveFocus(FocusDirection.Up)
                                        android.util.Log.d("JetskiDebug", "DirectionUp: moved=$moved")
                                        if (!moved) {
                                            android.util.Log.d("JetskiDebug", "DirectionUp: scrolling to ${listState.firstVisibleItemIndex + 1}")
                                            coroutineScope.launch {
                                                if (uiState.messages.isNotEmpty()) {
                                                    val nextIndex = (listState.firstVisibleItemIndex + 1)
                                                        .coerceAtMost(uiState.messages.size - 1)
                                                    listState.animateScrollToItem(nextIndex)
                                                }
                                            }
                                            true
                                        } else false
                                    }
                                    Key.DirectionDown -> {
                                        val moved = focusManager.moveFocus(FocusDirection.Down)
                                        android.util.Log.d("JetskiDebug", "DirectionDown: moved=$moved")
                                        if (!moved) {
                                            android.util.Log.d("JetskiDebug", "DirectionDown: scrolling to ${listState.firstVisibleItemIndex - 1}")
                                            coroutineScope.launch {
                                                if (listState.firstVisibleItemIndex > 0) {
                                                    val nextIndex = listState.firstVisibleItemIndex - 1
                                                    listState.animateScrollToItem(nextIndex)
                                                }
                                            }
                                            true
                                        } else false
                                    }
                                    else -> false
                                }
                            } else false
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        com.example.appfunctions.agent.ui.components.TvSurfaceTextField(
                            value = messageText.text,
                            placeholder = stringResource(R.string.agent_demo_ask_agent),
                            modifier = Modifier.focusRequester(inputFocusRequester).fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                            onValueChange = { newString ->
                                messageText = TextFieldValue(newString)
                                if (selectedAppPackageName != null && appMentionRegex != null) {
                                    if (!appMentionRegex.containsMatchIn(newString)) {
                                        selectedAppPackageName = null
                                    }
                                }
                            },
                            trailingIcon = if (isTv) null else {
                                {
                                    IconButton(
                                        onClick = sendMessage,
                                        enabled = messageText.text.isNotBlank(),
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription =
                                                stringResource(R.string.agent_demo_send),
                                        )
                                    }
                                }
                            },
                        )

                        if (showAutocomplete && filteredApps.isNotEmpty()) {
                            Popup(
                                popupPositionProvider = popupPositionProvider,
                                onDismissRequest = {},
                                properties = PopupProperties(focusable = false),
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(0.9f),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceBright,
                                        ),
                                    shape = MaterialTheme.shapes.medium,
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        filteredApps.take(5).forEach { app ->
                                            DropdownMenuItem(
                                                text = { Text(app.label) },
                                                onClick = {
                                                    val currentText = messageText.text
                                                    val selectionStart = messageText.selection.start
                                                    val textBeforeCursor =
                                                        currentText.take(
                                                            selectionStart,
                                                        )
                                                    val textAfterCursor =
                                                        currentText.drop(
                                                            selectionStart,
                                                        )
                                                    val mentionIndex = textBeforeCursor.lastIndexOf('@')
                                                    if (mentionIndex >= 0) {
                                                        val textBeforeMention =
                                                            textBeforeCursor.substring(
                                                                0,
                                                                mentionIndex,
                                                            )
                                                        val newText =
                                                            "$textBeforeMention@${app.label} $textAfterCursor"
                                                        val newCursorPosition =
                                                            mentionIndex + app.label.length + 2
                                                        messageText =
                                                            TextFieldValue(
                                                                text = newText,
                                                                selection =
                                                                    TextRange(
                                                                        newCursorPosition,
                                                                    ),
                                                            )
                                                        selectedAppPackageName = app.packageName
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isTv) {
                        val isSendEnabled = messageText.text.isNotBlank()
                        var isSendFocused by remember { mutableStateOf(false) }
                        val sendScale by animateFloatAsState(
                            if (isSendFocused) 1.1f else 1.0f,
                            label = "sendScale",
                        )
                        Surface(
                            onClick = sendMessage,
                            enabled = isSendEnabled,
                            modifier = Modifier
                                .size(52.dp)
                                .scale(sendScale)
                                .onFocusChanged { isSendFocused = it.isFocused },
                            shape = CircleShape,
                            color = when {
                                !isSendEnabled -> MaterialTheme.colorScheme.surfaceBright
                                isSendFocused -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            border =
                                if (isSendFocused) {
                                    BorderStroke(
                                        2.5.dp,
                                        MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                },
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.agent_demo_send),
                                    tint = when {
                                        !isSendEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        isSendFocused -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                )
                            }
                        }

                        // Model Dropdown
                        ModelDropdown(
                            currentThread = uiState.currentThread,
                            onModelSelected = { onEvent(AgentUiEvent.OnModelSelected(it)) },
                        )

                        // History Button
                        var isHistoryFocused by remember { mutableStateOf(false) }
                        val historyScale by animateFloatAsState(
                            if (isHistoryFocused) 1.1f else 1.0f,
                            label = "historyScale",
                        )
                        Surface(
                            onClick = { showHistoryDialog = true },
                            modifier =
                                Modifier
                                    .size(52.dp)
                                    .scale(historyScale)
                                    .onFocusChanged { isHistoryFocused = it.isFocused },
                            shape = CircleShape,
                            color =
                                if (isHistoryFocused) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceBright,
                            border =
                                if (isHistoryFocused) {
                                    BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                                } else null,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint =
                                        if (isHistoryFocused) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        // Add Button
                        var isAddFocused by remember { mutableStateOf(false) }
                        val addScale by animateFloatAsState(
                            if (isAddFocused) 1.1f else 1.0f,
                            label = "addScale",
                        )
                        Surface(
                            onClick = {
                                onEvent(AgentUiEvent.OnCreateThread(uiState.currentThread.llmModel))
                            },
                            modifier =
                                Modifier
                                    .size(52.dp)
                                    .scale(addScale)
                                    .onFocusChanged { isAddFocused = it.isFocused },
                            shape = CircleShape,
                            color =
                                if (isAddFocused) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceBright,
                            border =
                                if (isAddFocused) {
                                    BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                                } else null,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Thread",
                                    tint =
                                        if (isAddFocused) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHistoryDialog) {
        TvHistoryDialog(
            threads = uiState.threads,
            currentThread = uiState.currentThread,
            onThreadSelected = { onEvent(AgentUiEvent.OnThreadSelected(it)) },
            onDismissRequest = { showHistoryDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelDropdown(
    modifier: Modifier = Modifier,
    currentThread: ThreadEntity?,
    onModelSelected: (LlmModel) -> Unit,
    onMenuClick: (() -> Unit)? = null,
) {
    val isTv = rememberFormFactor() == FormFactor.TV
    var showModelDialog by remember { mutableStateOf(false) }

    val models =
        listOf(
            LlmModel.GEMINI_3_1_PRO_PREVIEW,
            LlmModel.GEMINI_3_FLASH_PREVIEW,
            LlmModel.GEMINI_3_1_FLASH_LITE_PREVIEW,
        )

    if (isTv) {
        var isFocused by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            if (isFocused) 1.1f else 1.0f,
            label = "modelDropdownScale",
        )
        Surface(
            onClick = { showModelDialog = true },
            modifier =
                modifier
                    .size(52.dp)
                    .scale(scale)
                    .onFocusChanged { isFocused = it.isFocused },
            shape = CircleShape,
            color =
                if (isFocused) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceBright,
            border =
                if (isFocused) BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary) else null,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Select Model",
                )
            }
        }

        if (showModelDialog) {
            TvModelDialog(
                models = models,
                selectedModel = currentThread?.llmModel,
                onModelSelected = onModelSelected,
                onDismissRequest = { showModelDialog = false },
            )
        }
    } else {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            Surface(
                modifier = Modifier.padding(bottom = 8.dp),
                shadowElevation = 2.dp,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceBright,
            ) {
                val text =
                    currentThread?.llmModel?.modelName
                        ?: stringResource(R.string.agent_demo_select_model_to_create_thread)
                val textColor =
                    if (currentThread != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(start = 4.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Row(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryEditable,
                                    enabled = true,
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.agent_demo_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            }

            LazyExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize(),
                containerColor = MaterialTheme.colorScheme.surfaceBright,
                shape = RoundedCornerShape(28.dp),
            ) {
                item {
                    Text(
                        "--- Gemini ---",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                items(models) { model ->
                    DropdownMenuItem(
                        text = { Text(model.modelName) },
                        onClick = {
                            onModelSelected(model)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    isValidAction: Boolean,
    installedApps: List<AppInfo>,
    onConfirmAction: (String) -> Unit,
) {
    val isTv = rememberFormFactor() == FormFactor.TV
    val alignment = if (message.role == MessageRole.USER) Alignment.End else Alignment.Start
    val isError = message.processingStatus == MessageProcessingStatus.FAILED
    val backgroundColor =
        when {
            isError -> MaterialTheme.colorScheme.errorContainer
            message.role == MessageRole.USER -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceBright
        }
    val textColor =
        when {
            isError -> MaterialTheme.colorScheme.onErrorContainer
            message.role == MessageRole.USER -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        }

    val parsedData = remember(message.textContent) {
        parseMessageContent(message.textContent)
    }
    val cleanContentText = parsedData.first
    val parsedCalls = parsedData.second

    android.util.Log.d("JetskiDebug", "MessageBubble: messageId=${message.messageId}, role=${message.role}, cleanText='$cleanContentText', callsSize=${parsedCalls.size}")

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = alignment,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = backgroundColor,
            shadowElevation = if (message.role == MessageRole.ASSISTANT) 1.dp else 0.dp,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val bubbleContent = @Composable {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isError) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = stringResource(R.string.debugging_error),
                                tint = textColor,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        val contentText =
                            if (cleanContentText.isEmpty() &&
                                message.pendingIntentId != null
                            ) {
                                stringResource(R.string.agent_demo_action_confirmation_needed)
                            } else {
                                cleanContentText
                            }
                        if (message.role != MessageRole.USER) {
                            Markdown(content = contentText)
                        } else {
                            val chipBgColor = MaterialTheme.colorScheme.primary
                            val chipTextColor = MaterialTheme.colorScheme.onPrimary
                            val formattedText =
                                remember(contentText, installedApps) {
                                    formatMessageText(contentText, installedApps)
                                }
                            val textMeasurer = rememberTextMeasurer()
                            val typographyStyle = MaterialTheme.typography.bodyLarge
                            val density = LocalDensity.current

                            val inlineContentMap =
                                remember(
                                    contentText,
                                    installedApps,
                                    chipBgColor,
                                    chipTextColor,
                                    density,
                                ) {
                                    val map = mutableMapOf<String, InlineTextContent>()
                                    if (installedApps.isNotEmpty() && contentText.contains("@")) {
                                        val appLabelsPattern =
                                            installedApps.joinToString(
                                                "|",
                                            ) { Regex.escape(it.label) }
                                        val regex =
                                            Regex("@($appLabelsPattern)\\b", RegexOption.IGNORE_CASE)
                                        regex.findAll(contentText).forEachIndexed { index, match ->
                                            val id = "chip_$index"
                                            val appName = match.value
                                            val measured =
                                                textMeasurer.measure(
                                                    text = appName,
                                                    style =
                                                        typographyStyle.copy(
                                                            fontWeight = FontWeight.Bold,
                                                        ),
                                                )
                                            val widthSp =
                                                with(
                                                    density,
                                                ) { (measured.size.width + 8.dp.roundToPx()).toSp() }
                                            val heightSp =
                                                with(
                                                    density,
                                                ) { (measured.size.height + 2.dp.roundToPx()).toSp() }

                                            map[id] =
                                                InlineTextContent(
                                                    Placeholder(
                                                        width = widthSp,
                                                        height = heightSp,
                                                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                                                    ),
                                                ) {
                                                    Surface(
                                                        shape =
                                                            androidx.compose.foundation.shape.RoundedCornerShape(
                                                                6.dp,
                                                            ),
                                                        color = chipBgColor,
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = appName,
                                                                color = chipTextColor,
                                                                style =
                                                                    typographyStyle.copy(
                                                                        fontWeight = FontWeight.Bold,
                                                                    ),
                                                                modifier =
                                                                    Modifier.padding(
                                                                        horizontal = 4.dp,
                                                                        vertical = 1.dp,
                                                                    ),
                                                            )
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                    map
                                }

                            Text(
                                text = formattedText,
                                inlineContent = inlineContentMap,
                                color = textColor,
                                style = typographyStyle,
                            )
                        }
                    }
                }
                if (isTv) {
                    bubbleContent()
                } else {
                    SelectionContainer {
                        bubbleContent()
                    }
                }
                if (parsedCalls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    parsedCalls.forEach { call ->
                        AppFunctionCallHintCard(call, installedApps)
                    }
                }

                if (message.pendingIntentId != null) {
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    androidx.compose.material3.Button(
                        onClick = { onConfirmAction(message.pendingIntentId) },
                        enabled = isValidAction,
                        shape = CircleShape,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Text(
                            if (isValidAction) {
                                stringResource(R.string.agent_demo_confirm_action)
                            } else {
                                stringResource(R.string.agent_demo_action_expired)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(
    status: AgentStatus,
    packageManager: PackageManager,
) {
    when (status) {
        AgentStatus.Thinking -> {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.agent_demo_thinking),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        is AgentStatus.InvokingTool -> {
            val appName =
                try {
                    val appInfo = packageManager.getApplicationInfo(status.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    status.packageName
                }
            val appIcon =
                try {
                    packageManager.getApplicationIcon(status.packageName)
                } catch (e: Exception) {
                    null
                }

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceBright,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    appIcon?.let {
                        Image(
                            bitmap = it.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Text(appName, style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.agent_demo_connecting),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }

        AgentStatus.Idle -> {
            // Nothing to show
        }
    }
}

@Composable
fun ChatHistorySidePanel(
    threads: List<ThreadEntity>,
    currentThread: ThreadEntity?,
    onEvent: (AgentUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(280.dp)
                .fillMaxHeight()
                .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.agent_demo_chat_history),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = threads,
                key = { thread -> thread.threadId },
            ) { thread ->
                val isSelected = thread.threadId == currentThread?.threadId
                val backgroundColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                val textColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onEvent(AgentUiEvent.OnThreadSelected(thread.threadId))
                            },
                    shape = MaterialTheme.shapes.medium,
                    color = backgroundColor,
                    contentColor = textColor,
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = thread.llmModel.modelName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                        )
                        Text(
                            text = "ID: ${thread.threadId.take(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

class InlineAppScopingVisualTransformation(
    private val installedApps: List<AppInfo>,
    private val chipTextColor: Color,
) : VisualTransformation {
    private val regex: Regex? =
        if (installedApps.isNotEmpty()) {
            val appLabelsPattern = installedApps.joinToString("|") { Regex.escape(it.label) }
            Regex("@($appLabelsPattern)\\b", RegexOption.IGNORE_CASE)
        } else {
            null
        }

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val currentRegex = regex
        if (currentRegex == null || !rawText.contains("@")) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val matches = currentRegex.findAll(rawText)

        val annotatedString =
            buildAnnotatedString {
                var lastIndex = 0
                matches.forEach { match ->
                    append(rawText.substring(lastIndex, match.range.first))
                    pushStringAnnotation(tag = "mention", annotation = match.value)
                    withStyle(
                        SpanStyle(
                            color = chipTextColor,
                            fontWeight = FontWeight.Bold,
                        ),
                    ) {
                        append(match.value)
                    }
                    pop()
                    lastIndex = match.range.last + 1
                }
                if (lastIndex < rawText.length) {
                    append(rawText.substring(lastIndex))
                }
            }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}

fun formatMessageText(
    text: String,
    installedApps: List<AppInfo>,
): AnnotatedString {
    if (installedApps.isEmpty() || !text.contains("@")) {
        return AnnotatedString(text)
    }
    val appLabelsPattern = installedApps.joinToString("|") { Regex.escape(it.label) }
    val regex = Regex("@($appLabelsPattern)\\b", RegexOption.IGNORE_CASE)
    val matches = regex.findAll(text)

    return buildAnnotatedString {
        var lastIndex = 0
        matches.forEachIndexed { index, match ->
            val precedingText = text.substring(lastIndex, match.range.first)
            if (precedingText.isNotEmpty()) {
                append(precedingText)
            }
            appendInlineContent(id = "chip_$index", alternateText = match.value)
            lastIndex = match.range.last + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
fun TvHistoryDialog(
    threads: List<ThreadEntity>,
    currentThread: ThreadEntity?,
    onThreadSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier =
                Modifier
                    .width(400.dp)
                    .heightIn(max = 400.dp)
                    .padding(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.agent_demo_chat_history),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(threads) { thread ->
                        val isSelected = thread.threadId == currentThread?.threadId
                        var isItemFocused by remember { mutableStateOf(false) }
                        val itemScale by animateFloatAsState(
                            if (isItemFocused) 1.04f else 1.0f,
                            label = "itemScale",
                        )
                        Surface(
                            onClick = {
                                onThreadSelected(thread.threadId)
                                onDismissRequest()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .scale(itemScale)
                                    .onFocusChanged { isItemFocused = it.isFocused },
                            shape = MaterialTheme.shapes.medium,
                            color =
                                if (isItemFocused) MaterialTheme.colorScheme.primaryContainer
                                else if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface,
                            border =
                                if (isItemFocused) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else null,
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = thread.llmModel.modelName,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = "ID: ${thread.threadId.take(8)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TvModelDialog(
    models: List<LlmModel>,
    selectedModel: LlmModel?,
    onModelSelected: (LlmModel) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier =
                Modifier
                    .width(400.dp)
                    .heightIn(max = 300.dp)
                    .padding(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Select Model",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(models) { model ->
                        val isSelected = model == selectedModel
                        var isItemFocused by remember { mutableStateOf(false) }
                        val itemScale by animateFloatAsState(
                            if (isItemFocused) 1.04f else 1.0f,
                            label = "itemScale",
                        )
                        Surface(
                            onClick = {
                                onModelSelected(model)
                                onDismissRequest()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .scale(itemScale)
                                    .onFocusChanged { isItemFocused = it.isFocused },
                            shape = MaterialTheme.shapes.medium,
                            color =
                                if (isItemFocused) MaterialTheme.colorScheme.primaryContainer
                                else if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface,
                            border =
                                if (isItemFocused) {
                                    BorderStroke(2.2.dp, MaterialTheme.colorScheme.primary)
                                } else null,
                        ) {
                            Text(
                                text = model.modelName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ParsedAppFunctionCall(
    val packageName: String,
    val functionId: String,
    val arguments: Map<String, Any?>,
)

fun parseMessageContent(content: String): Pair<String, List<ParsedAppFunctionCall>> {
    android.util.Log.d("JetskiDebug", "parseMessageContent input: $content")
    val regex = Regex("@@AppFunctionCall:(.*?)@@")
    val calls = mutableListOf<ParsedAppFunctionCall>()
    var cleanText = content

    regex.findAll(content).forEach { match ->
        try {
            val jsonStr = match.groupValues[1]
            val json = JSONObject(jsonStr)
            val packageName = json.getString("package")
            val functionId = json.getString("function")
            val argsJson = json.optJSONObject("args")
            val argsMap = mutableMapOf<String, Any?>()
            if (argsJson != null) {
                val keys = argsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    argsMap[key] = argsJson.get(key)
                }
            }
            calls.add(ParsedAppFunctionCall(packageName, functionId, argsMap))
        } catch (e: Exception) {
            android.util.Log.e("AgentDemoScreen", "Error parsing AppFunctionCall tag", e)
        }
        cleanText = cleanText.replace(match.value, "")
    }

    val result = Pair(cleanText.trim(), calls)
    android.util.Log.d("JetskiDebug", "parseMessageContent output: cleanText='${result.first}', callsSize=${result.second.size}")
    return result
}

@Composable
fun AppFunctionCallHintCard(
    call: ParsedAppFunctionCall,
    installedApps: List<AppInfo>,
    modifier: Modifier = Modifier,
) {
    val appLabel = remember(call.packageName, installedApps) {
        installedApps.find { it.packageName == call.packageName }?.label ?: call.packageName
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Extension,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AppFunction Invoked",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "App: $appLabel (${call.packageName})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Function: ${call.functionId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (call.arguments.isNotEmpty()) {
                val argsStr = call.arguments.entries.joinToString(", ") { "${it.key}=${it.value}" }
                Text(
                    text = "Parameters: $argsStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
