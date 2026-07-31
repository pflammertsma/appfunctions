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
package com.example.appfunctions.agent.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Android TV Surface Paradigm Text Field.
 *
 * Unfocused / Idle state: Renders a focusable Surface container that responds to D-Pad navigation.
 * High contrast focus stroke border and scale boost when focused.
 * Pressing D-Pad Center (or click) enters editing mode and focuses the underlying OutlinedTextField.
 * Pressing Back or Enter/Done exits editing mode, hides the keyboard, and restores D-Pad navigation.
 * Plain Enter sends/submits, while Shift+Enter or Ctrl+Enter inserts a new line.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TvSurfaceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = CircleShape,
) {
    var isEditing by remember { mutableStateOf(false) }
    var wasEditing by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val textFieldFocusRequester = remember { FocusRequester() }
    val surfaceFocusRequester = remember { FocusRequester() }

    val isImeVisible = WindowInsets.isImeVisible
    var keyboardHasShown by remember(isEditing) { mutableStateOf(false) }

    fun stopEditing() {
        isEditing = false
        keyboardController?.hide()
    }

    BackHandler(enabled = isEditing) {
        stopEditing()
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            wasEditing = true
            textFieldFocusRequester.requestFocus()
        } else if (wasEditing) {
            surfaceFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            keyboardHasShown = true
        } else if (keyboardHasShown && isEditing) {
            stopEditing()
        }
    }

    val targetHeight = if (label != null) 64.dp else 56.dp
    val heightModifier =
        if (singleLine) {
            Modifier.height(targetHeight)
        } else {
            Modifier.heightIn(min = targetHeight, max = 160.dp)
        }

    if (isEditing) {
        var tfValue by remember {
            mutableStateOf(
                TextFieldValue(
                    text = value,
                    selection = TextRange(0, value.length),
                ),
            )
        }
        LaunchedEffect(value) {
            if (tfValue.text != value) {
                tfValue = tfValue.copy(text = value, selection = TextRange(value.length))
            }
        }
        var tfFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = tfValue,
            onValueChange = { newTfValue ->
                tfValue = newTfValue
                onValueChange(newTfValue.text)
            },
            singleLine = singleLine,
            maxLines = maxLines,
            label = label?.let { { Text(it) } },
            placeholder = { Text(placeholder) },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        keyboardActions.onSend?.invoke(this)
                        stopEditing()
                    },
                    onDone = {
                        keyboardActions.onDone?.invoke(this)
                        stopEditing()
                    },
                    onGo = {
                        keyboardActions.onGo?.invoke(this)
                        stopEditing()
                    },
                    onSearch = {
                        keyboardActions.onSearch?.invoke(this)
                        stopEditing()
                    },
                ),
            shape = shape,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            modifier =
                modifier
                    .then(heightModifier)
                    .focusRequester(textFieldFocusRequester)
                    .onFocusChanged { focusState ->
                        if (tfFocused && !focusState.isFocused) {
                            stopEditing()
                        }
                        tfFocused = focusState.isFocused
                    }
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Back || keyEvent.key == Key.Escape) {
                            if (keyEvent.type == KeyEventType.KeyUp) {
                                stopEditing()
                            }
                            true
                        } else if (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) {
                            if (keyEvent.isCtrlPressed || keyEvent.isShiftPressed) {
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    val selection = tfValue.selection
                                    val text = tfValue.text
                                    val newText =
                                        text.substring(0, selection.min) + "\n" + text.substring(selection.max)
                                    val newSelection = TextRange(selection.min + 1)
                                    tfValue = tfValue.copy(text = newText, selection = newSelection)
                                    onValueChange(newText)
                                }
                                true
                            } else {
                                if (keyEvent.type == KeyEventType.KeyUp) {
                                    val actionScope =
                                        object : androidx.compose.foundation.text.KeyboardActionScope {
                                            override fun defaultKeyboardAction(imeAction: androidx.compose.ui.text.input.ImeAction) {}
                                        }
                                    if (keyboardActions.onSend != null) {
                                        keyboardActions.onSend?.invoke(actionScope)
                                    } else if (keyboardActions.onDone != null) {
                                        keyboardActions.onDone?.invoke(actionScope)
                                    }
                                    stopEditing()
                                }
                                true
                            }
                        } else {
                            false
                        }
                    },
        )
    } else {
        var isFocused by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(if (isFocused) 1.02f else 1.0f, label = "textFieldScale")

        Surface(
            onClick = { isEditing = true },
            modifier =
                modifier
                    .then(heightModifier)
                    .scale(scale)
                    .focusRequester(surfaceFocusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
            shape = shape,
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (label != null) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = value.ifEmpty { placeholder },
                        color =
                            when {
                                isFocused -> MaterialTheme.colorScheme.onPrimaryContainer
                                value.isNotEmpty() -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                trailingIcon?.invoke()
            }
        }
    }
}
