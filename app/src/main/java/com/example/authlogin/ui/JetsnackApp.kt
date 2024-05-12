/*
 * Copyright 2020 The Android Open Source Project
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

package com.example.authlogin.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.authlogin.AuthViewModel
import com.example.authlogin.ui.home.HomeSections
import com.example.authlogin.ui.home.addHomeGraph
import com.example.authlogin.ui.navigation.MainDestinations
import com.example.authlogin.ui.navigation.rememberJetsnackNavController
import com.example.authlogin.ui.theme.JetsnackTheme

@Composable
fun JetsnackApp(
    authViewModel: AuthViewModel
) {
    JetsnackTheme {
        val jetsnackNavController = rememberJetsnackNavController()
        NavHost(
            navController = jetsnackNavController.navController,
            startDestination = MainDestinations.HOME_ROUTE
        ) {
            jetsnackNavGraph(
                onSnackSelected = jetsnackNavController::navigateToSnackDetail,
                upPress = jetsnackNavController::upPress,
                onNavigateToRoute = jetsnackNavController::navigateToBottomBarRoute,
                authViewModel = authViewModel
            )
        }
    }
}

private fun NavGraphBuilder.jetsnackNavGraph(
    onSnackSelected: (String, NavBackStackEntry) -> Unit,
    upPress: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    authViewModel: AuthViewModel
) {
    navigation(
        route = MainDestinations.HOME_ROUTE,
        startDestination = HomeSections.FEED.route
    ) {
        addHomeGraph(onSnackSelected, onNavigateToRoute, authViewModel)
    }
    composable(
        "${MainDestinations.SNACK_DETAIL_ROUTE}/{${MainDestinations.SNACK_ID_KEY}}",
        arguments = listOf(navArgument(MainDestinations.SNACK_ID_KEY) { type = NavType.StringType })
    ) { backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val snackId = arguments.getString(MainDestinations.SNACK_ID_KEY)!!
    }
}
