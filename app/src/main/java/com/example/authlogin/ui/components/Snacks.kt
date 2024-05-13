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

package com.example.authlogin.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.authlogin.AuthViewModel
import com.example.authlogin.R
import com.example.authlogin.model.CollectionType
import com.example.authlogin.model.Snack
import com.example.authlogin.model.SnackCollection
import com.example.authlogin.model.snacks
import com.example.authlogin.ui.theme.JetsnackTheme
import com.example.authlogin.ui.utils.mirroringIcon
import com.example.authlogin.ui.components.JetsnackSurface
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

private val HighlightCardWidth = 300.dp
private val HighlightCardPadding = 16.dp
private val Density.cardWidthWithPaddingPx
    get() = (HighlightCardWidth + HighlightCardPadding).toPx()

@Composable
fun SnackCollection(
    snackCollection: SnackCollection,
    onSnackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0,
    highlight: Boolean = true,
    authViewModel: AuthViewModel
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(start = 24.dp)
        ) {
            Text(
                text = snackCollection.name,
                style = MaterialTheme.typography.h6,
                color = JetsnackTheme.colors.brand,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(2f)
                    .wrapContentWidth(Alignment.Start)
            )

        }
        HighlightedSnacks(snackCollection.snacks, onSnackClick, authViewModel)
    }
}

@Composable
private fun HighlightedSnacks(
    snacks: List<Snack>,
    onSnackClick: (String) -> Unit,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val rowState = rememberLazyListState()
    val cardWidthWithPaddingPx = with(LocalDensity.current) { cardWidthWithPaddingPx }

    val scrollProvider = {
        // Simple calculation of scroll distance for homogenous item types with the same width.
        val offsetFromStart = cardWidthWithPaddingPx * rowState.firstVisibleItemIndex
        offsetFromStart + rowState.firstVisibleItemScrollOffset
    }

    val gradient = listOf(
        JetsnackTheme.colors.gradient6_1,
        JetsnackTheme.colors.gradient6_2
    )

    val (currentIndex, setCurrentIndex) = remember { mutableStateOf(0) }

    val handleDislike = {
        setCurrentIndex((currentIndex + 1) % snacks.size)
    }
    val firebaseFirestore = Firebase.firestore
    fun writeDataOnFirestore(studentItem: Snack){
        val student = HashMap<String, Any>()
        student["name"] = studentItem.name
        student["id"] = studentItem.id
        student["rating"] = studentItem.rating
        student["userRating"] = studentItem.userRating
        student["imageUrl"] = studentItem.imageUrl
        firebaseFirestore.collection(authViewModel.currentUser.value!!.uid).document(studentItem.id)
            .set(student)
            .addOnSuccessListener { Log.d("Firebase", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("Firebase", "Error writing document", e) }
    }
    fun handleNext(pass: Boolean, snack: Snack) {
        setCurrentIndex((currentIndex + 1) % snacks.size)
        if (pass) {
            writeDataOnFirestore(snack)
        }
    }

    LazyRow(
        state = rowState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(start=20.dp)
    ) {
        itemsIndexed(snacks) { index, snack ->
            HighlightSnackItem(
                snack = snack,
                onSnackClick = onSnackClick,
                index = index,
                gradient = gradient[index % 2],
                scrollProvider = scrollProvider,
                currentIndex = currentIndex,
                handleNextlike = { handleNext(true, snack) },
                handleNextdislike = { handleNext(false, snack) }
            )
        }
    }
}

@Composable
private fun Snacks(
    snacks: List<Snack>,
    onSnackClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
    ) {
        items(snacks) { snack ->
            SnackItem(snack, onSnackClick)
        }
    }
}

@Composable
fun SnackItem(
    snack: Snack,
    onSnackClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    JetsnackSurface(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.padding(
            start = 4.dp,
            end = 4.dp,
            bottom = 8.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(10.dp)
        ) {
            SnackImage(
                imageUrl = snack.imageUrl,
                elevation = 4.dp,
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Text(
                text = snack.name,
                style = MaterialTheme.typography.subtitle1,
                color = JetsnackTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun HighlightSnackItem(
    snack: Snack,
    onSnackClick: (String) -> Unit,
    index: Int,
    gradient: List<Color>,
    scrollProvider: () -> Float,
    handleNextlike: () -> Unit,
    handleNextdislike: () -> Unit,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    if (index == currentIndex) {
        JetsnackCard(
            modifier = modifier
                .size(width = 350.dp, height = 550.dp)
                .padding(bottom = 20.dp, end = 20.dp, start = 20.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 0.dp, vertical = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(340.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                                .offsetGradientBackground(
                                    colors = gradient,
                                    width = { 7 * cardWidthWithPaddingPx },
                                    offset = {
                                        val left = index * cardWidthWithPaddingPx
                                        val gradientOffset = left - (scrollProvider() / 3f)
                                        gradientOffset
                                    }
                                )
                        )
                        SnackImage(
                            imageUrl = snack.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(250.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = snack.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.h6,
                        color = JetsnackTheme.colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rating: ${snack.rating.toString()}",
                        style = MaterialTheme.typography.body1,
                        color = JetsnackTheme.colors.textHelp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "User Rating (${snack.userRating.toString()})",
                        style = MaterialTheme.typography.body1,
                        color = JetsnackTheme.colors.textHelp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Add Tinder-like buttons at the bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = handleNextdislike ,
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF252525), // Change the background color to red
                                contentColor = Color(0xFFEC3E3E)
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                modifier = Modifier.padding(all = 20.dp),
                                contentDescription = "Dislike"
                            )
                        }

                        Button(
                            onClick = handleNextlike,
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFF2F7FF), // Change the background color to red
                                contentColor = Color(0xFFEC3E3E)// Change the content (icon) color to white
                            )

                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.like),
                                modifier = Modifier.padding(all = 10.dp),
                                contentDescription = "Like"
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SnackImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp
) {
    JetsnackSurface(
        color = Color.LightGray,
        elevation = elevation,
        shape = CircleShape,
        modifier = modifier
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            placeholder = painterResource(R.drawable.placeholder),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}


