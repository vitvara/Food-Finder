package com.example.authlogin.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.authlogin.AuthViewModel
import com.example.authlogin.R
import com.example.authlogin.model.AuthState
import com.example.authlogin.model.DataProvider
import com.example.authlogin.ui.components.AnonymousSignIn
import com.example.authlogin.ui.components.GoogleSignIn
import com.example.authlogin.ui.components.JetsnackButton
import com.example.authlogin.ui.components.OneTapSignIn
import com.example.authlogin.ui.theme.JetsnackColors
import com.example.authlogin.ui.theme.JetsnackTheme
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    loginState: MutableState<Boolean>? = null
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credentials = authViewModel.oneTapClient.getSignInCredentialFromIntent(result.data)
                authViewModel.signInWithGoogle(credentials)
            }
            catch (e: ApiException) {
                Log.e("LoginScreen:Launcher","Login One-tap $e")
            }
        }
        else if (result.resultCode == Activity.RESULT_CANCELED){
            Log.e("LoginScreen:Launcher","OneTapClient Canceled")
        }
    }

    fun launch(signInResult: BeginSignInResult) {
        val intent = IntentSenderRequest.Builder(signInResult.pendingIntent.intentSender).build()
        launcher.launch(intent)
    }

    Scaffold(
        backgroundColor = JetsnackTheme.colors.brand,
        contentColor = JetsnackTheme.colors.uiBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .wrapContentSize(Alignment.TopCenter),
            Arrangement.spacedBy(8.dp),
            Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f),
                painter = painterResource(R.drawable.loginscreen),
                contentDescription = "app_logo",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(color = JetsnackTheme.colors.iconInteractive)
            )

            if (DataProvider.authState == AuthState.SignedOut) {
                JetsnackButton(
                    onClick = {
                        authViewModel.signInAnonymously()
                    }
                ) {
                    Text(
                        text = "Guest Login",
                        modifier = Modifier.padding(6.dp),
                        color = JetsnackTheme.colors.textHelp
                    )
                }
            }
        }
    }



    OneTapSignIn (
        launch = {
            launch(it)
        }
    )

    GoogleSignIn {
        // Dismiss LoginScreen
        loginState?.let {
            it.value = false
        }
    }
}

