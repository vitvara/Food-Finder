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

package com.example.authlogin.ui.home

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.authlogin.AuthViewModel
import com.example.authlogin.model.AuthState
import com.example.authlogin.model.DataProvider
import com.example.authlogin.ui.components.JetsnackButton
import com.example.authlogin.ui.components.JetsnackScaffold
import com.example.authlogin.ui.theme.JetsnackTheme
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.common.api.ApiException

@Composable
fun Profile(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val authState = DataProvider.authState
    Log.i("HomeScreen", "WHAT happen $authState")
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = authViewModel.oneTapClient.getSignInCredentialFromIntent(result.data)
                authViewModel.deleteAccount(credential.googleIdToken)
            } catch (e: ApiException) {
                Log.e("HomeScreen:Launcher", "Re-auth error: $e")
            }
        }
    }

    fun launch(signInResult: BeginSignInResult) {
        val intent = IntentSenderRequest.Builder(signInResult.pendingIntent.intentSender).build()
        launcher.launch(intent)
    }

    val openLoginDialog = remember { mutableStateOf(false) }
    val openDeleteAccountAlertDialog = remember { mutableStateOf(false) }

    JetsnackScaffold(
        bottomBar = {
            JetsnackBottomBar(
                tabs = HomeSections.values(),
                currentRoute = HomeSections.PROFILE.route,
                navigateToRoute = onNavigateToRoute
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(24.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "User Id: ${authViewModel.currentUser.value!!.uid}")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                JetsnackButton(
                    onClick = {
                        if (authState != AuthState.Authenticated)
                            openLoginDialog.value = true
                        else
                            authViewModel.signOut()
                    },
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = if (authState != AuthState.Authenticated) "Sign-in" else "Sign out",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}


