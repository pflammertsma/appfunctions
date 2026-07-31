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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Android TV Focusable Surface Card.
 * Standardizes D-Pad focus borders, scale animations, and surface container color transitions.
 */
@Composable
fun TvSurfaceCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    focusedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    unfocusedContainerColor: Color = MaterialTheme.colorScheme.surfaceBright,
    focusedBorder: BorderStroke? = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary),
    unfocusedBorder: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    onFocusStateChange: ((Boolean) -> Unit)? = null,
    content: @Composable (isFocused: Boolean) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.02f else 1.0f, label = "tvSurfaceCardScale")

    Surface(
        onClick = onClick,
        modifier =
            modifier
                .scale(scale)
                .fillMaxWidth()
                .onFocusChanged {
                    isFocused = it.isFocused
                    onFocusStateChange?.invoke(it.isFocused)
                },
        shape = shape,
        color = if (isFocused) focusedContainerColor else unfocusedContainerColor,
        border = if (isFocused) focusedBorder else unfocusedBorder,
    ) {
        content(isFocused)
    }
}

/**
 * Standardized settings row card for Android TV.
 */
@Composable
fun TvSettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    applyDefaultPadding: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
        )
    },
) {
    TvSurfaceCard(
        onClick = onClick,
        modifier =
            if (applyDefaultPadding) {
                Modifier.padding(horizontal = 24.dp, vertical = 8.dp).then(modifier)
            } else {
                modifier
            },
    ) { isFocused ->
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            leadingContent = leadingIcon,
            trailingContent = trailingIcon,
            colors =
                ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor =
                        if (isFocused) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    leadingIconColor =
                        if (isFocused) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    trailingIconColor =
                        if (isFocused) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                ),
        )
    }
}
