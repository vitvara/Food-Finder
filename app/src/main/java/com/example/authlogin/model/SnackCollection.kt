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

package com.example.authlogin.model

import android.util.Log
import androidx.compose.runtime.Immutable
import com.example.authlogin.repository.SnackApi
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Immutable
data class SnackCollection(
    val id: String,
    val name: String,
    val snacks: List<Snack>,
    val type: CollectionType = CollectionType.Normal
)

enum class CollectionType { Normal, Highlight }

/**
 * A fake repo
 */
object SnackRepo {
    private val _snackCollections = MutableStateFlow<List<SnackCollection>>(emptyList())
    val snackCollections: StateFlow<List<SnackCollection>> = _snackCollections

    suspend fun fetchData(myApi: SnackApi, currentLocation: LatLng): List<SnackCollection> {
        Log.d("test", "help")
        val response = myApi.getSnackItems(currentLocation.latitude, currentLocation.longitude)
        return listOf(
            SnackCollection(
                id="1L",
                snacks=response.execute().body()!!,
                name = "Android's picks",
                type = CollectionType.Highlight,
            )
        )
    }
}

/**
 * Static data
 */

private val tastyTreats = SnackCollection(
    id = "1L",
    name = "Android's picks",
    type = CollectionType.Highlight,
    snacks = listOf(
        Snack(
            id = "1L",
            name = "Cupcake",
            rating = 4.5,
            imageUrl = "https://source.unsplash.com/pGM4sjt_BdQ",
            userRating = 299
        ),
        Snack(
            id = "2L",
            name = "Donut",
            rating = 4.5,
            imageUrl = "https://source.unsplash.com/Yc5sL-ejk6U",
            userRating = 299
        ),
        Snack(
            id = "3L",
            name = "Donut",
            rating = 4.5,
            imageUrl = "https://source.unsplash.com/-LojFX9NfPY",
            userRating = 299
        ),
        Snack(
            id = "4L",
            name = "Donut",
            rating = 4.5,
            imageUrl = "https://source.unsplash.com/3U2V5WqK1PQ",
            userRating = 299
        )

    )
)


private val snackCollections = listOf(
    tastyTreats
)



@Immutable
data class OrderLine(
    val snack: Snack,
    val count: Int
)
