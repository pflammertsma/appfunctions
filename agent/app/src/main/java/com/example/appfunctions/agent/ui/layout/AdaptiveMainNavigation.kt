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
package com.example.appfunctions.agent.ui.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemDefaults
import androidx.tv.material3.rememberDrawerState

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AdaptiveMainNavigation(
    navController: NavHostController,
    items: List<String>,
    icons: List<ImageVector>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val isTv = isTvFormFactor()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    if (isTv) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val focusRequesters = remember { List(items.size) { FocusRequester() } }
        val contentFocusRequester = remember { FocusRequester() }
        var isDrawerFocused by remember { mutableStateOf(false) }

        val selectedIndex =
            items.indexOfFirst { screen ->
                currentDestination?.hierarchy?.any {
                    it.route?.startsWith(screen) == true
                } == true
            }

        ModalNavigationDrawer(
            drawerState = drawerState,
            modifier =
                modifier
                    .fillMaxSize()
                    .consumeWindowInsets(WindowInsets(0, 0, 0, 0)),
            drawerContent = { _ ->
                val drawerColor =
                    if (drawerState.currentValue == DrawerValue.Open) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        Color.Transparent
                    }

                Surface(
                    color = drawerColor,
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .onFocusChanged { isDrawerFocused = it.hasFocus }
                            .focusGroup()
                            .focusProperties {
                                onEnter = {
                                    if (selectedIndex in focusRequesters.indices) {
                                        focusRequesters[selectedIndex]
                                    } else {
                                        FocusRequester.Default
                                    }
                                }
                            },
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        items.forEachIndexed { index, screen ->
                            val isSelected =
                                currentDestination?.hierarchy?.any {
                                    it.route?.startsWith(screen) == true
                                } == true

                            NavigationDrawerItem(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        val popped = navController.popBackStack(screen, inclusive = false)
                                        if (!popped) {
                                            contentFocusRequester.requestFocus()
                                        }
                                    } else {
                                        navController.navigate(screen) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    drawerState.setValue(DrawerValue.Closed)
                                },
                                leadingContent = {
                                    Icon(icons[index], contentDescription = labels[index])
                                },
                                colors =
                                    NavigationDrawerItemDefaults.colors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color(0xFFC4C6D0),
                                        focusedContainerColor = Color(0xFF33353A),
                                        focusedContentColor = Color.White,
                                        selectedContainerColor = Color(0xFF44474F),
                                        selectedContentColor = Color.White,
                                        focusedSelectedContainerColor = Color(0xFF3F4759),
                                        focusedSelectedContentColor = Color.White,
                                    ),
                                border =
                                    NavigationDrawerItemDefaults.border(
                                        focusedBorder =
                                            androidx.tv.material3.Border(
                                                border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary),
                                            ),
                                        focusedSelectedBorder =
                                            androidx.tv.material3.Border(
                                                border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary),
                                            ),
                                    ),
                                modifier =
                                    Modifier
                                        .focusRequester(focusRequesters[index])
                                        .focusProperties {
                                            canFocus = isDrawerFocused || drawerState.currentValue == DrawerValue.Open || isSelected
                                        }
                                        .padding(vertical = 4.dp),
                            ) {
                                Text(labels[index])
                            }
                        }
                    }
                }
            },
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .focusRequester(contentFocusRequester),
            ) {
                content(Modifier.fillMaxSize())
            }
        }
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    items.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = labels[index]) },
                            label = { Text(labels[index]) },
                            selected =
                                currentDestination?.hierarchy?.any {
                                    it.route?.startsWith(screen) == true
                                } == true,
                            onClick = {
                                navController.navigate(screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            },
        ) { paddingValues ->
            content(Modifier.padding(paddingValues))
        }
    }
}
