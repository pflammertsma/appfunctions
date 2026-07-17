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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.rememberDrawerState

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

        LaunchedEffect(drawerState.currentValue) {
            if (drawerState.currentValue == DrawerValue.Open) {
                val selectedIndex = items.indexOfFirst { screen ->
                    currentDestination?.hierarchy?.any {
                        it.route?.startsWith(screen) == true
                    } == true
                }
                if (selectedIndex != -1) {
                    try {
                        focusRequesters[selectedIndex].requestFocus()
                    } catch (e: Exception) {
                        // Ignore if not attached yet
                    }
                }
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            modifier = modifier
                .fillMaxSize()
                .consumeWindowInsets(WindowInsets(0, 0, 0, 0)),
            drawerContent = { _ ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    Column(
                        modifier = Modifier
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
                                    navController.navigate(screen) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                leadingContent = {
                                    Icon(icons[index], contentDescription = labels[index])
                                },
                                modifier = Modifier
                                    .focusRequester(focusRequesters[index])
                                    .padding(vertical = 4.dp),
                            ) {
                                Text(labels[index])
                            }
                        }
                    }
                }
            },
        ) {
            content(Modifier.fillMaxSize())
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
