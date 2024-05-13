package com.example.authlogin.ui.home

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.authlogin.AuthViewModel
import com.example.authlogin.model.Snack
import com.example.authlogin.ui.components.JetsnackScaffold
import com.example.authlogin.ui.theme.JetsnackTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun SnackListScreen(
    onNavigateToRoute: (String) -> Unit,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var snacks = remember { mutableStateOf<List<Snack>>(emptyList()) }
    var isLoading = remember { mutableStateOf(true) }
    val firebaseFirestore = Firebase.firestore
    LaunchedEffect(Unit) {
        val currentUserUid = authViewModel.currentUser.value?.uid

        if (currentUserUid != null) {
            val snacksCollection = firebaseFirestore.collection(currentUserUid)

            snacksCollection.get().addOnSuccessListener { querySnapshot ->
                val snacksList = querySnapshot.toObjects(Snack::class.java)

                snacks.value = snacksList  // Update the state with the fetched snacks
            }.addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting snacks: ${exception.message}")
                // Handle error (e.g., show an error message to the user)
            }
        } else {
            // Handle the case where the user is not logged in
            Log.w("Firestore", "User is not logged in. Cannot fetch snacks.")
        }
    }

   JetsnackScaffold(
        bottomBar = {
            JetsnackBottomBar(
                tabs = HomeSections.values(),
                currentRoute = HomeSections.SNACKLIST.route,
                navigateToRoute = onNavigateToRoute
            )
        },
        modifier = modifier
    ) {
       Row(
           verticalAlignment = Alignment.CenterVertically,
           modifier = Modifier
               .heightIn(min = 56.dp)
               .padding(start = 24.dp)
       ) {
           Text(
               text = "Liked restaurant",
               style = MaterialTheme.typography.h6,
               color = JetsnackTheme.colors.brand,
               maxLines = 1,
               overflow = TextOverflow.Ellipsis,
               modifier = Modifier
                   .weight(2f)
                   .wrapContentWidth(Alignment.Start)
           )

       }
       LazyColumn(
           modifier=Modifier.padding(top=50.dp)
       ) {
           itemsIndexed(snacks.value) { idx, snack ->
               SnackItem(snack = snack)
           }
       }
    }
}

@Composable
fun SnackItem(snack: Snack) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // (Optional) Display image if you have imageUrl
        // Image(...)

        Column(modifier = Modifier.padding(start = 8.dp)) { // Add padding if you include the image
            Text(snack.name, style = MaterialTheme.typography.h6)
            Text("Rating: ${snack.rating.toString()}", style = MaterialTheme.typography.body1)
            Text("User Rating: ${snack.userRating.toString()}", style = MaterialTheme.typography.body1)
        }
    }
}
