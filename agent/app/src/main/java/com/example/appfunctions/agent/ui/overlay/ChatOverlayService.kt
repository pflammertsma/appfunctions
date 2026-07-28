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
package com.example.appfunctions.agent.ui.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.appfunctions.agent.MainActivity
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.data.SettingsRepository
import com.example.appfunctions.agent.domain.AgentOrchestrator
import com.example.appfunctions.agent.domain.AgentStatus
import com.example.appfunctions.agent.domain.appfunction.AppInfo
import com.example.appfunctions.agent.data.db.entities.MessageEntity
import com.example.appfunctions.agent.data.db.entities.MessageProcessingStatus
import com.example.appfunctions.agent.data.db.entities.MessageRole
import com.example.appfunctions.agent.domain.appfunction.GetInstalledAppsUseCase
import com.example.appfunctions.agent.domain.chat.GetChatHistoryUseCase
import com.example.appfunctions.agent.domain.chat.ManageThreadsUseCase
import com.example.appfunctions.agent.domain.chat.SendMessageUseCase
import com.example.appfunctions.agent.domain.pendingintent.ConsumePendingIntentUseCase
import com.example.appfunctions.agent.domain.pendingintent.LaunchPendingIntentUseCase
import com.example.appfunctions.agent.domain.pendingintent.ObserveActivePendingIntentsUseCase
import com.example.appfunctions.agent.ui.components.TvSurfaceTextField
import com.example.appfunctions.agent.ui.screens.agentdemo.MessageBubble
import com.example.appfunctions.agent.ui.screens.agentdemo.StatusIndicator
import com.example.appfunctions.agent.ui.theme.AppFunctionsAgentTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service that displays a floating screen overlay over a desired foreground AppFunction-compatible app.
 * Focus remains in the screen overlay so that the user types into the "Ask Agent" text box.
 *
 * Exiting overlay mode (via second Back press when the text field container is focused or clicking Close)
 * removes the overlay and returns the AppFunctions Agent app to the foreground.
 */
@AndroidEntryPoint
class ChatOverlayService : Service() {

    @Inject lateinit var manageThreadsUseCase: ManageThreadsUseCase
    @Inject lateinit var getChatHistoryUseCase: GetChatHistoryUseCase
    @Inject lateinit var sendMessageUseCase: SendMessageUseCase
    @Inject lateinit var agentOrchestrator: AgentOrchestrator
    @Inject lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase
    @Inject lateinit var observeActivePendingIntentsUseCase: ObserveActivePendingIntentsUseCase
    @Inject lateinit var consumePendingIntentUseCase: ConsumePendingIntentUseCase
    @Inject lateinit var launchPendingIntentUseCase: LaunchPendingIntentUseCase
    @Inject lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private lateinit var overlayOwner: OverlayLifecycleOwner

    private val _targetPackageName = MutableStateFlow<String?>(null)
    private val _targetAppLabel = MutableStateFlow<String>("")
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    private val _currentThreadId = MutableStateFlow<String?>(null)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW permission not granted. Cannot launch overlay.")
            stopSelf()
            return
        }

        overlayOwner = OverlayLifecycleOwner().apply {
            onCreate()
            onStart()
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        composeView =
            ComposeView(this).apply {
                setViewTreeLifecycleOwner(overlayOwner)
                setViewTreeViewModelStoreOwner(overlayOwner)
                setViewTreeSavedStateRegistryOwner(overlayOwner)
                setViewTreeOnBackPressedDispatcherOwner(overlayOwner)
                setOnKeyListener { _, keyCode, event ->
                    if (
                        keyCode == android.view.KeyEvent.KEYCODE_BACK &&
                            event.action == android.view.KeyEvent.ACTION_UP
                    ) {
                        if (overlayOwner.onBackPressedDispatcher.hasEnabledCallbacks()) {
                            overlayOwner.onBackPressedDispatcher.onBackPressed()
                            true
                        } else {
                            exitOverlayMode()
                            true
                        }
                    } else {
                        false
                    }
                }
                setContent {
                AppFunctionsAgentTheme {
                    val targetAppLabel by _targetAppLabel.collectAsState()
                    val targetPackageName by _targetPackageName.collectAsState()
                    val messages by _messages.collectAsState()
                    val status by agentOrchestrator.status.collectAsState(initial = AgentStatus.Idle)
                    val activePendingActionIds by observeActivePendingIntentsUseCase().collectAsState(initial = emptySet())
                    val installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
                    val isAppFunctionDebuggingEnabled by settingsRepository.appFunctionDebuggingEnabled.collectAsState(initial = true)

                    var appsList by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
                    LaunchedEffect(Unit) {
                        appsList = getInstalledAppsUseCase()
                    }

                    OverlayContent(
                        targetAppLabel = targetAppLabel,
                        targetPackageName = targetPackageName,
                        messages = messages,
                        installedApps = appsList,
                        status = status,
                        activePendingActionIds = activePendingActionIds,
                        isAppFunctionDebuggingEnabled = isAppFunctionDebuggingEnabled,
                        onSendMessage = { text ->
                            val threadId = _currentThreadId.value
                            if (threadId != null) {
                                serviceScope.launch {
                                    sendMessageUseCase(
                                        threadId = threadId,
                                        role = MessageRole.USER,
                                        textContent = text,
                                        processingStatus = MessageProcessingStatus.PENDING_AGENT_RESPONSE,
                                        targetPackageName = targetPackageName,
                                    )
                                }
                            }
                        },
                        onConfirmAction = { actionId ->
                            serviceScope.launch {
                                val pendingIntent = consumePendingIntentUseCase(actionId)
                                if (pendingIntent != null) {
                                    launchPendingIntentUseCase(pendingIntent)
                                }
                            }
                        },
                        onClose = {
                            exitOverlayMode()
                        }
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            0, // Focusable: do NOT set FLAG_NOT_FOCUSABLE so text input and D-Pad work
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
        }

        windowManager?.addView(composeView, params)

        serviceScope.launch {
            manageThreadsUseCase.getThreads().collectLatest { threads ->
                val currentThread = threads.firstOrNull()
                if (currentThread != null) {
                    _currentThreadId.value = currentThread.threadId
                    getChatHistoryUseCase(currentThread.threadId).collectLatest { msgs ->
                        _messages.value = msgs
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pkgName = intent?.getStringExtra(EXTRA_TARGET_PACKAGE_NAME)
        val label = intent?.getStringExtra(EXTRA_TARGET_APP_LABEL)
        if (pkgName != null) {
            _targetPackageName.value = pkgName
            _targetAppLabel.value = label ?: pkgName
        }
        return START_NOT_STICKY
    }

    private fun exitOverlayMode() {
        try {
            if (composeView != null && composeView?.isAttachedToWindow == true) {
                windowManager?.removeViewImmediate(composeView)
                composeView = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error removing overlay view", e)
        }
        stopSelf()

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(mainIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (composeView != null && composeView?.isAttachedToWindow == true) {
                windowManager?.removeViewImmediate(composeView)
                composeView = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error removing overlay view in onDestroy", e)
        }
        if (::overlayOwner.isInitialized) {
            overlayOwner.onDestroy()
        }
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "ChatOverlayService"
        const val EXTRA_TARGET_PACKAGE_NAME = "extra_target_package_name"
        const val EXTRA_TARGET_APP_LABEL = "extra_target_app_label"
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun OverlayContent(
    targetAppLabel: String,
    targetPackageName: String?,
    messages: List<MessageEntity>,
    installedApps: List<AppInfo>,
    status: AgentStatus,
    activePendingActionIds: Set<String>,
    isAppFunctionDebuggingEnabled: Boolean,
    onSendMessage: (String) -> Unit,
    onConfirmAction: (String) -> Unit,
    onClose: () -> Unit,
) {
    val inputFocusRequester = remember { FocusRequester() }
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(Unit) {
        delay(150)
        inputFocusRequester.requestFocus()
    }

    BackHandler {
        onClose()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 16.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Overlay Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (targetAppLabel.isNotEmpty()) "Target app: $targetAppLabel" else "Target app",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                var isCloseFocused by remember { mutableStateOf(false) }
                val closeScale by animateFloatAsState(
                    if (isCloseFocused) 1.1f else 1.0f,
                    label = "closeScale"
                )
                Surface(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .scale(closeScale)
                        .onFocusChanged { isCloseFocused = it.isFocused },
                    shape = CircleShape,
                    color = if (isCloseFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isCloseFocused) BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary) else null,
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Overlay Mode",
                            tint = if (isCloseFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Compact Chat Message List
            if (messages.isNotEmpty() || status != AgentStatus.Idle) {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    if (status != AgentStatus.Idle) {
                        item {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            StatusIndicator(status = status, packageManager = context.packageManager)
                        }
                    }

                    items(
                        items = messages.asReversed(),
                        key = { it.messageId }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            isValidAction = message.pendingIntentId in activePendingActionIds,
                            installedApps = installedApps,
                            showAppFunctionDebugDetails = isAppFunctionDebuggingEnabled,
                            onConfirmAction = onConfirmAction,
                        )
                    }
                }
            }

            // Input bar + Send button
            val sendMessage = {
                val textStr = messageText.text
                if (textStr.isNotBlank()) {
                    onSendMessage(textStr)
                    messageText = TextFieldValue("")
                    inputFocusRequester.requestFocus()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TvSurfaceTextField(
                        value = messageText.text,
                        placeholder = "Ask Agent in overlay...",
                        modifier = Modifier
                            .focusRequester(inputFocusRequester)
                            .fillMaxWidth(),
                        onValueChange = { newStr ->
                            messageText = TextFieldValue(newStr)
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                sendMessage()
                            }
                        ),
                    )
                }

                val isSendEnabled = messageText.text.isNotBlank()
                var isSendFocused by remember { mutableStateOf(false) }
                val sendScale by animateFloatAsState(
                    if (isSendFocused) 1.1f else 1.0f,
                    label = "sendScale"
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
                    border = if (isSendFocused) {
                        BorderStroke(2.5.dp, MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    },
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = when {
                                !isSendEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                isSendFocused -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }
        }
    }
}
