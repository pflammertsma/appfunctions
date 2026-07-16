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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp

/**
 * Android TV Surface Paradigm Text Field.
 *
 * Unfocused / Idle state: Renders a focusable Surface container that responds to D-Pad navigation.
 * Pressing D-Pad Center (or click) enters editing mode and focuses the underlying OutlinedTextField.
 * Pressing Back or Enter/Done exits editing mode, hides the keyboard, and restores D-Pad navigation.
 */
@Composable
fun TvSurfaceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    shape: Shape = CircleShape,
) {
    var isEditing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val textFieldFocusRequester = remember { FocusRequester() }

    if (isEditing) {
        var tfValue by remember {
            mutableStateOf(
                androidx.compose.ui.text.input.TextFieldValue(
                    text = value,
                    selection = androidx.compose.ui.text.TextRange(0, value.length),
                )
            )
        }
        OutlinedTextField(
            value = tfValue,
            onValueChange = { newTfValue ->
                tfValue = newTfValue
                onValueChange(newTfValue.text)
            },
            singleLine = singleLine,
            label = label?.let { { Text(it) } },
            placeholder = { Text(placeholder) },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(
                onDone = {
                    isEditing = false
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                onSearch = {
                    isEditing = false
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
            ),
            modifier = modifier
                .focusRequester(textFieldFocusRequester)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Back && keyEvent.type == KeyEventType.KeyUp) {
                        isEditing = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        true
                    } else false
                }
        )
        LaunchedEffect(Unit) {
            textFieldFocusRequester.requestFocus()
        }
    } else {
        Surface(
            onClick = { isEditing = true },
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceBright,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifEmpty { placeholder },
                    color = if (value.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                trailingIcon?.invoke()
            }
        }
    }
}
