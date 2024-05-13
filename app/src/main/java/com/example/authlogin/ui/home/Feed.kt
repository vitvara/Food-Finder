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

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationRequest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.authlogin.AuthViewModel

import com.example.authlogin.model.CollectionType
import com.example.authlogin.model.SnackCollection
import com.example.authlogin.model.SnackRepo
import com.example.authlogin.repository.SnackApi
import com.example.authlogin.ui.components.JetsnackDivider
import com.example.authlogin.ui.components.JetsnackScaffold
import com.example.authlogin.ui.components.JetsnackSurface
import com.example.authlogin.ui.components.SnackCollection
import com.example.authlogin.ui.theme.JetsnackTheme
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type


@Composable
fun Feed(
    onSnackClick: (String) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    authViewModel: AuthViewModel,
    currentLocation: MutableState<LatLng>,
    modifier: Modifier = Modifier
) {
    val snackCollections = remember { mutableStateOf(emptyList<SnackCollection>()) } // Initial empty list
    val coroutineScope = rememberCoroutineScope()
    val baseUrl = "http://192.168.1.109:8000" // Replace with your actual base URL



    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory()) // Add this line
        .build()

    val myApi: SnackApi = retrofit.create(SnackApi::class.java)
    LaunchedEffect(Unit) {
        coroutineScope.launch {

            snackCollections.value = SnackRepo.fetchData(myApi, currentLocation.value) // Update state variable

        }
    }

    JetsnackScaffold(
        bottomBar = {
            JetsnackBottomBar(
                tabs = HomeSections.values(),
                currentRoute = HomeSections.FEED.route,
                navigateToRoute = onNavigateToRoute
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Feed(
            snackCollections.value,
            onSnackClick,
            authViewModel,
            Modifier.padding(paddingValues)
        )
    }
}


@Composable
private fun Feed(
    snackCollections: List<SnackCollection>,
    onSnackClick: (String) -> Unit,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {

    JetsnackSurface(modifier = modifier.fillMaxSize()) {
        Box {
            SnackCollectionList(snackCollections, onSnackClick, authViewModel)
        }
    }
}

@Composable
private fun SnackCollectionList(
    snackCollections: List<SnackCollection>,
    onSnackClick: (String) -> Unit,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var filtersVisible by rememberSaveable { mutableStateOf(false) }

    Box(modifier) {
        LazyColumn {
            item {
                Spacer(
                    Modifier.windowInsetsTopHeight(
                        WindowInsets.statusBars.add(WindowInsets(top = 25.dp))
                    )
                )

            }
            itemsIndexed(snackCollections) { index, snackCollection ->
                if (index > 0) {
                    JetsnackDivider(thickness = 5.dp)
                }

                SnackCollection(
                    snackCollection = snackCollection,
                    onSnackClick = onSnackClick,
                    index = index,
                    authViewModel = authViewModel
                )
            }
        }
    }
    AnimatedVisibility(
        visible = filtersVisible,
        enter = slideInVertically() + expandVertically(
            expandFrom = Alignment.Top
        ) + fadeIn(initialAlpha = 0.3f),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
    }
}
