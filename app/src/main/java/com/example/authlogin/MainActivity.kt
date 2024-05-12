package com.example.authlogin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.authlogin.model.AuthState
import com.example.authlogin.model.DataProvider
import com.example.authlogin.ui.JetsnackApp
import com.example.authlogin.ui.LoginScreen
import com.example.authlogin.ui.theme.JetsnackTheme


import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel by viewModels<AuthViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetsnackTheme {
                val currentUser = authViewModel.currentUser.collectAsState().value
                DataProvider.updateAuthState(currentUser)

                Log.i("AuthRepo", "Authenticated: ${DataProvider.isAuthenticated}")
                Log.i("AuthRepo", "Anonymous: ${DataProvider.isAnonymous}")
                Log.i("AuthRepo", "User: ${DataProvider.user}")
                Log.i("AuthRepo", "AuthState: ${DataProvider.authState}")
                if (DataProvider.authState != AuthState.SignedOut) {
                    JetsnackApp(authViewModel)
                } else {
                    LoginScreen(authViewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JetsnackTheme {
        JetsnackApp(hiltViewModel())
    }
}