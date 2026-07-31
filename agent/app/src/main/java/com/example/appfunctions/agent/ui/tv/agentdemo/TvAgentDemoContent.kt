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

import android.app.Activity
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.data.LlmModel
import com.example.appfunctions.agent.data.db.entities.ThreadEntity
import com.example.appfunctions.agent.domain.AgentStatus
import com.example.appfunctions.agent.ui.components.TvSurfaceTextField
import com.example.appfunctions.agent.ui.contracts.AgentDemoScreenLayout
import com.example.appfunctions.agent.ui.screens.agentdemo.AgentDemoLoadingScreen
import com.example.appfunctions.agent.ui.screens.agentdemo.AgentUiEvent
import com.example.appfunctions.agent.ui.screens.agentdemo.AgentUiState
import com.example.appfunctions.agent.ui.screens.agentdemo.AppFunctionCallHintCard
import com.example.appfunctions.agent.ui.screens.agentdemo.ChatHistorySidePanel
import com.example.appfunctions.agent.ui.screens.agentdemo.MessageBubble
import com.example.appfunctions.agent.ui.screens.agentdemo.StatusIndicator
import com.example.appfunctions.agent.ui.screens.agentdemo.parseMessageContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object TvAgentDemoLayout : AgentDemoScreenLayout {
    @Composable
    override fun Content(
        uiState: AgentUiState,
        onEvent: (AgentUiEvent) -> Unit,
        initialSidePanelVisible: Boolean,
        modifier: Modifier,
    ) {
        TvAgentDemoContent(
            uiState = uiState,
            onEvent = onEvent,
            initialSidePanelVisible = initialSidePanelVisible,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvAgentDemoContent(
    uiState: AgentUiState,
    modifier: Modifier = Modifier,
    onEvent: (AgentUiEvent) -> Unit,
    initialSidePanelVisible: Boolean = false,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val focusManager = LocalFocusManager.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) { focusManager.clearFocus() }

    when (uiState) {
        is AgentUiState.Loading -> {
            AgentDemoLoadingScreen()
        }

        is AgentUiState.Loaded -> {
            TvAgentDemoLoadedScreen(
                uiState = uiState,
                onEvent = onEvent,
                drawerState = drawerState,
                packageManager = packageManager,
                initialSidePanelVisible = initialSidePanelVisible,
                modifier = modifier.padding(start = 80.dp),
            )
        }
    }
}

@Composable
fun TvAgentDemoLoadedScreen(
    uiState: AgentUiState.Loaded,
    onEvent: (AgentUiEvent) -> Unit,
    drawerState: DrawerState,
    packageManager: PackageManager,
    modifier: Modifier = Modifier,
    initialSidePanelVisible: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var isSidePanelVisible by remember { mutableStateOf(initialSidePanelVisible) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }

    val inputFocusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val currentThreadId = uiState.currentThread.threadId

    val context = LocalContext.current
    val activity = context as? Activity
    DisposableEffect(Unit) {
        val originalMode = activity?.window?.attributes?.softInputMode
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        onDispose {
            if (originalMode != null) {
                activity.window?.setSoftInputMode(originalMode)
            }
        }
    }

    LaunchedEffect(currentThreadId) {
        android.util.Log.d("TvDebug", "SCROLL TRIGGER: currentThreadId changed to $currentThreadId")
        if (uiState.messages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(uiState.messages.size) {
        android.util.Log.d("TvDebug", "SCROLL TRIGGER: uiState.messages.size changed to ${uiState.messages.size}")
        if (uiState.messages.isNotEmpty() && listState.firstVisibleItemIndex == 0) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(listState) {
        androidx.compose.runtime.snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                android.util.Log.d("TvDebug", "TvAgentDemoContent: ListScrollState -> firstIndex=$index, offset=$offset")
            }
    }

    LaunchedEffect(Unit) {
        delay(100.milliseconds)
        inputFocusRequester.requestFocus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Unspecified,
        topBar = {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.nav_agent_demo),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .semantics { heading() },
                )
            }
        },
    ) { paddingValues ->
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            ) {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 24.dp)
                            .clip(RoundedCornerShape(16.dp)),
                ) {
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
                        key = { it.messageId },
                    ) { message ->
                        val (cleanText, toolCalls) = parseMessageContent(message.textContent)
                        val shouldRenderAppFunctionsInline = toolCalls.isNotEmpty()
                        Column {
                            if (shouldRenderAppFunctionsInline) {
                                toolCalls.forEach { toolCall ->
                                    AppFunctionCallHintCard(
                                        call = toolCall,
                                        installedApps = uiState.installedApps,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                    )
                                }
                            }
                            MessageBubble(
                                message = message.copy(textContent = cleanText),
                                isValidAction = uiState.activePendingActionIds.contains(message.messageId),
                                installedApps = uiState.installedApps,
                                showAppFunctionDebugDetails = uiState.isAppFunctionDebuggingEnabled,
                                enableTextSelection = false,
                                onConfirmAction = { pendingIntentId ->
                                    onEvent(AgentUiEvent.OnConfirmAction(pendingIntentId))
                                },
                            )
                        }
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    fun sendMessage() {
                        val trimmedText = messageText.text.trim()
                        if (trimmedText.isNotEmpty()) {
                            onEvent(AgentUiEvent.OnSendMessage(trimmedText))
                            messageText = TextFieldValue("")
                        }
                    }

                    TvSurfaceTextField(
                        value = messageText.text,
                        onValueChange = { messageText = TextFieldValue(it) },
                        placeholder = stringResource(R.string.agent_demo_ask_agent),
                        singleLine = false,
                        maxLines = 5,
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        modifier =
                            Modifier
                                .weight(1f)
                                .focusRequester(inputFocusRequester)
                                .onPreviewKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.key) {
                                            Key.DirectionUp -> {
                                                coroutineScope.launch { listState.animateScrollBy(150f) }
                                                true
                                            }

                                            Key.DirectionDown -> {
                                                coroutineScope.launch { listState.animateScrollBy(-150f) }
                                                true
                                            }

                                            Key.PageUp -> {
                                                coroutineScope.launch { listState.animateScrollBy(450f) }
                                                true
                                            }

                                            Key.PageDown -> {
                                                coroutineScope.launch { listState.animateScrollBy(-450f) }
                                                true
                                            }

                                            else -> false
                                        }
                                    } else {
                                        false
                                    }
                                },
                    )

                    // Send Button
                    val isSendEnabled = messageText.text.isNotBlank()
                    var isSendFocused by remember { mutableStateOf(false) }
                    val sendScale by animateFloatAsState(
                        if (isSendFocused) 1.1f else 1.0f,
                        label = "sendScale",
                    )
                    Surface(
                        onClick = { sendMessage() },
                        enabled = isSendEnabled,
                        modifier =
                            Modifier
                                .size(52.dp)
                                .scale(sendScale)
                                .onFocusChanged { isSendFocused = it.isFocused },
                        shape = CircleShape,
                        color =
                            when {
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
                                tint =
                                    when {
                                        !isSendEnabled ->
                                            MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.38f,
                                            )

                                        isSendFocused -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                            )
                        }
                    }

                    // Model Button
                    var isModelFocused by remember { mutableStateOf(false) }
                    val modelScale by animateFloatAsState(
                        if (isModelFocused) 1.1f else 1.0f,
                        label = "modelScale",
                    )
                    Surface(
                        onClick = { showModelDialog = true },
                        modifier =
                            Modifier
                                .size(52.dp)
                                .scale(modelScale)
                                .onFocusChanged { isModelFocused = it.isFocused },
                        shape = CircleShape,
                        color =
                            if (isModelFocused) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceBright
                            },
                        border =
                            if (isModelFocused) {
                                BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            },
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
                            if (isHistoryFocused) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceBright
                            },
                        border =
                            if (isHistoryFocused) {
                                BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint =
                                    if (isHistoryFocused) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
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
                            if (isAddFocused) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceBright
                            },
                        border =
                            if (isAddFocused) {
                                BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Thread",
                                tint =
                                    if (isAddFocused) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isSidePanelVisible,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                ) {
                    ChatHistorySidePanel(
                        threads = uiState.threads,
                        currentThread = uiState.currentThread,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }

    if (showHistoryDialog) {
        TvHistoryDialog(
            threads = uiState.threads,
            currentThread = uiState.currentThread,
            onThreadSelected = { threadId ->
                onEvent(AgentUiEvent.OnThreadSelected(threadId))
            },
            onDismissRequest = { showHistoryDialog = false },
        )
    }

    if (showModelDialog) {
        val models =
            listOf(
                LlmModel.GEMINI_3_1_PRO_PREVIEW,
                LlmModel.GEMINI_3_FLASH_PREVIEW,
                LlmModel.GEMINI_3_1_FLASH_LITE_PREVIEW,
            )
        TvModelDialog(
            models = models,
            selectedModel = uiState.currentThread.llmModel,
            onModelSelected = { onEvent(AgentUiEvent.OnModelSelected(it)) },
            isAppFunctionDebuggingEnabled = uiState.isAppFunctionDebuggingEnabled,
            onToggleAppFunctionDebugging = { onEvent(AgentUiEvent.OnToggleAppFunctionDebugging(it)) },
            onDismissRequest = { showModelDialog = false },
        )
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
                    .width(460.dp)
                    .padding(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.agent_demo_chat_history),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(threads) { thread ->
                        val isSelected = thread.threadId == currentThread?.threadId
                        var isItemFocused by remember { mutableStateOf(false) }
                        val itemScale by animateFloatAsState(
                            if (isItemFocused) 1.03f else 1.0f,
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
                                    .scale(itemScale)
                                    .onFocusChanged { isItemFocused = it.isFocused },
                            shape = MaterialTheme.shapes.medium,
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            border =
                                if (isItemFocused) {
                                    BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    null
                                },
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = thread.llmModel.modelName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                    Text(
                                        text = "ID: ${thread.threadId.take(8)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active Chat",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier =
                                            Modifier
                                                .padding(start = 12.dp)
                                                .size(20.dp),
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

@Composable
fun TvModelDialog(
    models: List<LlmModel>,
    selectedModel: LlmModel?,
    onModelSelected: (LlmModel) -> Unit,
    isAppFunctionDebuggingEnabled: Boolean,
    onToggleAppFunctionDebugging: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier =
                Modifier
                    .width(460.dp)
                    .padding(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
            ) {
                Text(
                    text = "AI Behavior",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Text(
                    text = "Select Model",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    models.forEach { model ->
                        val isSelected = model == selectedModel
                        var isItemFocused by remember { mutableStateOf(false) }
                        val itemScale by animateFloatAsState(
                            if (isItemFocused) 1.03f else 1.0f,
                            label = "itemScale",
                        )
                        Surface(
                            onClick = {
                                onModelSelected(model)
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .scale(itemScale)
                                    .onFocusChanged { isItemFocused = it.isFocused },
                            shape = MaterialTheme.shapes.medium,
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            border =
                                if (isItemFocused) {
                                    BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    null
                                },
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = model.modelName,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Debugging",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                var isToggleFocused by remember { mutableStateOf(false) }
                val toggleScale by animateFloatAsState(
                    if (isToggleFocused) 1.03f else 1.0f,
                    label = "toggleScale",
                )
                Surface(
                    onClick = {
                        onToggleAppFunctionDebugging(!isAppFunctionDebuggingEnabled)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .scale(toggleScale)
                            .onFocusChanged { isToggleFocused = it.isFocused },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border =
                        if (isToggleFocused) {
                            BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            null
                        },
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                        ) {
                            Text(
                                text = "AppFunction Debugging",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Show parameters & results in chat",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = isAppFunctionDebuggingEnabled,
                            onCheckedChange = { onToggleAppFunctionDebugging(it) },
                        )
                    }
                }
            }
        }
    }
}
