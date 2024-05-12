package com.example.authlogin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authlogin.model.DataProvider
import com.example.authlogin.model.Response
import com.example.authlogin.repository.AuthRepository
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    val oneTapClient: SignInClient
): ViewModel() {

    val currentUser = getAuthState()
    init {
        getAuthState()
        CoroutineScope(Dispatchers.IO).launch {
            repository.verifyGoogleSignIn()
        }
    }

    private fun getAuthState() = repository.getAuthState(viewModelScope)

    fun signInAnonymously() = CoroutineScope(Dispatchers.IO).launch {
        DataProvider.anonymousSignInResponse = Response.Loading
        DataProvider.anonymousSignInResponse = repository.signInAnonymously()
    }

    fun oneTapSignIn() = CoroutineScope(Dispatchers.IO).launch {
        DataProvider.oneTapSignInResponse = Response.Loading
        DataProvider.oneTapSignInResponse = repository.onTapSignIn()
    }

    fun signInWithGoogle(credentials: SignInCredential) = CoroutineScope(Dispatchers.IO).launch {
        DataProvider.googleSignInResponse = Response.Loading
        DataProvider.googleSignInResponse = repository.signInWithGoogle(credentials)
    }

    fun signOut() = CoroutineScope(Dispatchers.IO).launch {
        DataProvider.signOutResponse = Response.Loading
        DataProvider.signOutResponse = repository.signOut()
    }
    fun checkNeedsReAuth() = CoroutineScope(Dispatchers.IO).launch {
        if (repository.checkNeedsReAuth()) {
            // Authorize google sign in
            val idToken = repository.authorizeGoogleSignIn()
            if (idToken != null) {
                deleteAccount(idToken)
            }
            else {
                // If failed initiate oneTap sign in flow
                // deleteAccount(googleIdToken:) will be called from oneTap result callback
                oneTapSignIn()
                Log.i("AuthViewModel:deleteAccount","OneTapSignIn")
            }
        } else {
            deleteAccount(null)
        }
    }

    fun deleteAccount(googleIdToken: String?) = CoroutineScope(Dispatchers.IO).launch {
        Log.i("AuthViewModel:deleteAccount","Deleting Account...")
        DataProvider.deleteAccountResponse = Response.Loading
        DataProvider.deleteAccountResponse = repository.deleteUserAccount(googleIdToken)
    }
}
