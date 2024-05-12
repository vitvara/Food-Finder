package com.example.authlogin.repository

import android.util.Log
import com.example.authlogin.model.Constants
import com.example.authlogin.model.DataProvider
import com.example.authlogin.model.DeleteAccountResponse
import com.example.authlogin.model.FirebaseSignInResponse
import com.example.authlogin.model.OneTapSignInResponse
import com.example.authlogin.model.Response
import com.example.authlogin.model.SignOutResponse
import com.example.authlogin.model.isWithinPast
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private var oneTapClient: SignInClient,
    private var googleSignInClient: GoogleSignInClient,

    @Named(Constants.SIGN_IN_REQUEST)
    private var signInRequest: BeginSignInRequest,
    @Named(Constants.SIGN_UP_REQUEST)
    private var signUpRequest: BeginSignInRequest,
): AuthRepository {
    override fun getAuthState(viewModelScope: CoroutineScope) = callbackFlow {
        val authStateListener = AuthStateListener { auth ->
            auth.currentUser?.let { user ->
                /*try {
                    userRepository.getUserDocument(user)
                }
                catch (e: FirestoreException) {
                    if (e.message == "DocumentDoesNotExist") {
                        Log.i(TAG, "User Document Does Not Exist!")
                        GlobalScope.launch(Dispatchers.IO) {
                            verifyAuthTokenResult()
                        }
                    }
                }
                catch(e: Exception) {
                    // other errors
                }*/
            }

            trySend(auth.currentUser)
            Log.i(TAG, "User: ${auth.currentUser?.uid ?: "Not authenticated"}")
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), auth.currentUser)


    override suspend fun verifyGoogleSignIn(): Boolean {
        auth.currentUser?.let { user ->
            if (user.providerData.map { it.providerId }.contains("google.com")) {
                return try {
                    googleSignInClient.silentSignIn().await()
                    true
                } catch (e: ApiException) {
                    Log.e(TAG, "Error: ${e.message}")
                    signOut()
                    false
                }
            }
        }
        return false
    }

    override suspend fun signInAnonymously(): FirebaseSignInResponse {
        return try {
            val authResult = auth.signInAnonymously().await()
            authResult?.user?.let { user ->
                Log.i(TAG, "FirebaseAuthSuccess: Anonymous UID: ${user.uid}")
            }
            Response.Success(authResult)
        } catch (error: Exception) {
            Log.e(TAG, "FirebaseAuthError: Failed to Sign in anonymously")
            Response.Failure(error)
        }
    }

    override suspend fun onTapSignIn(): OneTapSignInResponse {
        return try {
            val signInResult = oneTapClient.beginSignIn(signInRequest).await()
            Response.Success(signInResult)
        } catch (e: Exception) {
            try {
                val signUpResult = oneTapClient.beginSignIn(signUpRequest).await()
                Response.Success(signUpResult)
            } catch(e: Exception) {
                Response.Failure(e)
            }
        }
    }

    override suspend fun signInWithGoogle(credential: SignInCredential): FirebaseSignInResponse {
        val googleCredential = GoogleAuthProvider
            .getCredential(credential.googleIdToken, null)
        return authenticateUser(googleCredential)
    }

    private suspend fun authenticateUser(credential: AuthCredential): FirebaseSignInResponse {
        return if (auth.currentUser != null) {
            authLink(credential)
        } else {
            authSignIn(credential)
        }
    }

    private suspend fun authSignIn(credential: AuthCredential): FirebaseSignInResponse {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            Log.i(TAG, "User: ${authResult?.user?.uid}")
            DataProvider.updateAuthState(authResult?.user)
            Response.Success(authResult)
        }
        catch (error: Exception) {
            Response.Failure(error)
        }
    }

    private suspend fun authLink(credential: AuthCredential): FirebaseSignInResponse {
        return try {
            val authResult = auth.currentUser?.linkWithCredential(credential)?.await()
            Log.i(TAG, "User: ${authResult?.user?.uid}")
            DataProvider.updateAuthState(authResult?.user)
            Response.Success(authResult)
        }
        catch (error: FirebaseAuthException) {
            when (error.errorCode) {
                Constants.AuthErrors.CREDENTIAL_ALREADY_IN_USE,
                Constants.AuthErrors.EMAIL_ALREADY_IN_USE -> {
                    Log.e(TAG, "FirebaseAuthError: authLink(credential:) failed, ${error.message}")
                    return authSignIn(credential)
                }
            }
            Response.Failure(error)
        }
        catch (error: Exception) {
            Response.Failure(error)
        }
    }


    override suspend fun signOut(): SignOutResponse {
        return try {
            oneTapClient.signOut().await()
            auth.signOut()
            Response.Success(true)
        }
        catch (e: java.lang.Exception) {
            Response.Failure(e)
        }
    }

    override fun checkNeedsReAuth(): Boolean {
        auth.currentUser?.metadata?.lastSignInTimestamp?.let { lastSignInDate ->
            return !Date(lastSignInDate).isWithinPast(5)
        }
        return false
    }

    override suspend fun authorizeGoogleSignIn(): String? {
        auth.currentUser?.let { user ->
            if (user.providerData.map { it.providerId }.contains("google.com")) {
                try {
                    val account = googleSignInClient.silentSignIn().await()
                    return account.idToken
                } catch (e: ApiException) {
                    Log.e(TAG, "Error: ${e.message}")
                }
            }
        }
        return null
    }

    private suspend fun reauthenticate(googleIdToken: String) {
        val googleCredential = GoogleAuthProvider
            .getCredential(googleIdToken, null)
        auth.currentUser?.reauthenticate(googleCredential)?.await()
    }

    override suspend fun deleteUserAccount(googleIdToken: String?): DeleteAccountResponse {
        return try {
            auth.currentUser?.let { user ->
                if (user.providerData.map { it.providerId }.contains("google.com")) {
                    // Re-authenticate if needed
                    if (checkNeedsReAuth() && googleIdToken != null) {
                        reauthenticate(googleIdToken)
                    }
                    // Revoke
                    googleSignInClient.revokeAccess().await()
                    oneTapClient.signOut().await()
                }
                // Delete firebase user
                auth.currentUser?.delete()?.await()
                Response.Success(true)
            }
            Log.e(TAG, "FirebaseAuthError: Current user is not available")
            Response.Success(false)
        }
        catch (e: Exception) {
            Log.e(TAG, "FirebaseAuthError: Failed to delete user")
            Response.Failure(e)
        }
    }

    private suspend fun verifyAuthTokenResult(): Boolean {
        return try {
            auth.currentUser?.getIdToken(true)?.await()
            true
        } catch (e: Exception) {
            Log.i(TAG, "Error retrieving id token result. $e")
            false
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}