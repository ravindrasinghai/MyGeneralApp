package com.rwt.mygeneral.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.exceptions.SignedOutException
import com.amplifyframework.auth.result.AuthSignOutResult
import com.rwt.mygeneral.R
import com.rwt.mygeneral.data.model.DataModel
import com.rwt.mygeneral.interfaces.IAuthClient
import com.rwt.mygeneral.view.login.SessionResult
import com.rwt.mygeneral.view.login.SessionStateView
import com.rwt.mygeneral.view.login.SignoutSessionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/*
 *      MainViewModel
 *      - viewModel that updates the MainFragment (the visible UI)
 *      - gets the data from model
 */
class MainViewModel : ViewModel() {

    val TAG = "MyGeneral::MainViewModel"

    // Create the model which contains data for our UI
    private val model = DataModel(textForUI = "Here's the updated text!")

    // Create MutableLiveData which MainFragment can subscribe to
    // When this data changes, it triggers the UI to do an update
    val uiTextLiveData = MutableLiveData<String>()

    private val _sessionResult = MutableLiveData<SessionResult>()
    val sessionResult: LiveData<SessionResult> = _sessionResult

    private val _signoutSessionResult = MutableLiveData<SignoutSessionResult>()
    val signoutSessionResult: LiveData<SignoutSessionResult> = _signoutSessionResult

    // Get the updated text from our model and post the value to MainFragment
    fun getUpdatedText() {
        val updatedText = model.textForUI
        uiTextLiveData.postValue(updatedText)
    }

    fun applicationOnCreate(iAuthClient: IAuthClient) {
        Log.i(TAG, " applicationOnCreate")
        // start with user authentication
        performAuthentication(iAuthClient);
    }
    fun signoutUser(iAuthClient: IAuthClient) {
        Log.i(TAG, " signoutUser")
        // start with user SignoutOperation
        performUserSignoutOperation(iAuthClient);
    }
    private fun performAuthentication(iAuthClient: IAuthClient) {
        Log.i(TAG, " performUserSignoutOperation")

        runBlocking {
            withContext(Dispatchers.IO) {
                Log.i(TAG, " calling performAuthentication run <")

                try {
                    val result = iAuthClient.fetchAuthSession()
                    Log.i(TAG, "Fetch Auth session response = $result")
                    if (!result.isSignedIn) {
                        Log.i(TAG, " user not signedin")
                        // launch Login Activity
                        _sessionResult.postValue(SessionResult(
                            success =
                            SessionStateView(sessionState = "user_not_signedin")
                        ))
                    } else {
                        Log.i(TAG, " user already signedin")
                        // launch Home Activity
                        _sessionResult.postValue(SessionResult(
                            success =
                            SessionStateView(sessionState = "user_already_signedin")
                        ))
                    }
                } catch (error: AuthException) {
                    Log.e(TAG, "Failed to fetch auth session", error)
                    _sessionResult.postValue(SessionResult(
                        error = R.string.auth_fetch_failed
                    ))
                } catch (error: SignedOutException) {
                    Log.e(TAG, "SignedOutException", error)
                    _sessionResult.postValue(SessionResult(
                        error = R.string.auth_fetch_failed
                    ))
                } catch (error: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException", error)
                    _sessionResult.postValue(SessionResult(
                        error = R.string.auth_fetch_failed
                    ))
                }
                Log.i(TAG, " calling performAuthentication run >")
            }
        }
    }

    private fun performUserSignoutOperation(iAuthClient: IAuthClient) {
        Log.i(TAG, " performUserSignoutOperation")

        runBlocking {
            withContext(Dispatchers.IO) {
                Log.i(TAG, " calling performUserSignoutOperation run <")

                try {
                    val result = fetchAuthSession(iAuthClient)
                    Log.i(TAG, "Fetch Auth session response = $result")
                    if (result.isSignedIn) {
                        Log.i(TAG, " signing out now...")
                        // launch Login Activity
                        val authSignOutResult = signOut(iAuthClient)
                        Log.i(TAG, " authSignOutResult response = $authSignOutResult")
                        _signoutSessionResult.postValue(SignoutSessionResult(
                            success =
                            SessionStateView(sessionState = "user_signedout_success")
                        ))
                    } else {
                        Log.i(TAG, " user already signed-out")
                        // no-op
                        _signoutSessionResult.postValue(SignoutSessionResult(
                            success =
                            SessionStateView(sessionState = "user_already_signedout")
                        ))
                    }
                } catch (error: AuthException) {
                    Log.e(TAG, "Failed to fetch auth session", error)
                    _signoutSessionResult.postValue(SignoutSessionResult(
                        error = R.string.signout_failed
                    ))
                } catch (error: SignedOutException) {
                    Log.e(TAG, "SignedOutException", error)
                    _signoutSessionResult.postValue(SignoutSessionResult(
                        error = R.string.signout_failed
                    ))
                } catch (error: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException", error)
                    _signoutSessionResult.postValue(SignoutSessionResult(
                        error = R.string.signout_failed
                    ))
                }
                Log.i(TAG, " calling performAuthentication run >")
            }
        }
    }

    private suspend fun fetchAuthSession(iAuthClient: IAuthClient): AuthSession {
        Log.i(TAG, "fetchAuthSession")
        return iAuthClient.fetchAuthSession()
    }

    private suspend fun signOut(iAuthClient: IAuthClient): AuthSignOutResult {
        Log.i(TAG, "signOut")
        return iAuthClient.signOut()
    }
}