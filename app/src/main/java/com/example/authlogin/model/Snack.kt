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

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class Snack(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("rating")
    val rating: Double = 0.0,
    @SerializedName("userRating")
    val userRating: Int = 0,
    @SerializedName("imageUrl")
    val imageUrl: String = "",
    @SerializedName("id")
    val id: String = ""
)
/**
 * Static data
 */

val snacks = listOf(
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
