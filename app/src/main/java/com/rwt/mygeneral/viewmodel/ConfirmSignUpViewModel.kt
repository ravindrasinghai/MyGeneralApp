package com.rwt.mygeneral.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.exceptions.service.CodeMismatchException
import com.amplifyframework.auth.cognito.exceptions.service.UserNotFoundException
import com.amplifyframework.auth.result.AuthSignUpResult
import com.rwt.mygeneral.data.ConfirmSignUpRepository

import com.rwt.mygeneral.R
import com.rwt.mygeneral.data.SharedPref
import com.rwt.mygeneral.interfaces.IAuthClient
import com.rwt.mygeneral.view.login.LoggedInUserView
import com.rwt.mygeneral.view.login.LoginFormState
import com.rwt.mygeneral.view.login.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ConfirmSignUpViewModel(private val confirmSignUpRepository: ConfirmSignUpRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    val TAG : String = "MyGeneral::ConfirmSignUpViewModel"

     fun confirmSignUp(iAuthClient: IAuthClient, confirmationCode: String): LoginResult {
        Log.i(TAG, "confirmSignUp, $confirmationCode")
        var resultCode : Int = 0
         val emailId = SharedPref.read(SharedPref.USER_NAME, "")
         val accountGuid = SharedPref.read(SharedPref.ACCOUNT_GUID, "")
         Log.i(TAG, "confirmSignUpAccount store's value is $emailId, $accountGuid")

        runBlocking {
            withContext(Dispatchers.IO) {
                try {

                    // signIn now
                    Log.i(TAG, "confirmSignUpAccount start")
                    val result = confirmSignUpAccount(iAuthClient, emailId.toString(), confirmationCode)
                    Log.i(TAG, "confirmSignUpAccount success, respose= $result")
                    resultCode = 1
                } catch (error : UserNotFoundException) {
                    // handle error here
                    resultCode = 2
                    Log.e(TAG, "Failed to confirmSignUp - User not found", error)
                } catch (error : AuthException) {
                    // handle error here
                    resultCode = 3
                    Log.e(TAG, "Failed to confirmSignUp - AuthException", error)
                } catch (error : CodeMismatchException) {
                    resultCode = 4
                    Log.e(TAG, "Failed to confirmSignUp - CodeMismatchException", error)
                }
            } // end of withContext
        } // end of runBlocking


        if (resultCode == 1) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(
                    displayName = "",
                    username = emailId.toString(),
                    accountGuid = accountGuid.toString()))
            return LoginResult(success = LoggedInUserView(
                displayName = "",
                username = emailId.toString(),
                accountGuid = accountGuid.toString()))
        } else if (resultCode == 2) {
            _loginResult.value = LoginResult(error = R.string.user_not_found)
            return LoginResult(error = R.string.user_not_found)
        } else if (resultCode == 3) {
            _loginResult.value = LoginResult(error = R.string.login_failed)
            return LoginResult(error = R.string.login_failed)
        }  else if (resultCode == 4) {
            _loginResult.value = LoginResult(error = R.string.login_failed)
            return LoginResult(error = R.string.login_failed)
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
            return LoginResult(error = R.string.login_failed)
        }

    }

    suspend fun confirmSignUpAccount(
        iAuthClient: IAuthClient,
        emailValue: String,
        confirmationCode: String
    ): AuthSignUpResult {
        Log.i(TAG, "confirmSignUpAccount")
        return iAuthClient.confirmSignUp(emailValue, confirmationCode)
    }

    fun confirmationDataChanged(confirmationCode: String) {
        Log.i(TAG, "confirmationDataChanged")
        if (!isUserNameValid(confirmationCode)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}