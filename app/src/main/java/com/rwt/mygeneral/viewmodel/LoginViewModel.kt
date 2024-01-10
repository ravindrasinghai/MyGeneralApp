package com.rwt.mygeneral.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.exceptions.service.UserNotFoundException
import com.amplifyframework.auth.cognito.exceptions.service.UserNotConfirmedException
import com.amplifyframework.auth.cognito.exceptions.service.UsernameExistsException
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.auth.result.step.AuthSignUpStep
import com.rwt.mygeneral.data.LoginRepository
import com.rwt.mygeneral.data.Result

import com.rwt.mygeneral.R
import com.rwt.mygeneral.data.SharedPref
import com.rwt.mygeneral.data.model.LoggedInUser
import com.rwt.mygeneral.interfaces.IAuthClient
import com.rwt.mygeneral.view.login.LoggedInUserView
import com.rwt.mygeneral.view.login.LoginFormState
import com.rwt.mygeneral.view.login.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    val TAG : String = "MyGeneral::LoginViewModel"

    var accountGuid : String? = null

    fun login(iAuthClient: IAuthClient, username: String, password: String) {
        // can be launched in a separate asynchronous job
        var result = loginRepository.login(username, password)

        if (result is Result.Success) {
            accountGuid = result.data.userId;
        }

        Log.i(TAG, "login, username= " + username
                + " , password= " + password
                + " , accountGuid= " + accountGuid)

        result = loginToAccount(iAuthClient, accountGuid, username, password);

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(
                    displayName = "",
                    username = username,
                    accountGuid =  result.data.accountGuid))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }
    // TODO: Move this code to another class or to some DI interface
    private fun loginToAccount(iAuthClient: IAuthClient, accountGuid : String?, emailValue : String, pwdValue: String): Result<LoggedInUser> {
        Log.i(TAG, "loginToAccount enter, email username= " + emailValue
                + " , password= " + pwdValue
                + " , accountGuid= " + accountGuid)
        var resultCode : Int = 0

        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    // signIn now
                    Log.i(TAG, "signInToAccount start")
                    val result = signInToAccount(iAuthClient, emailValue, pwdValue)
                    Log.i(TAG, "signInToAccount success, response = $result")
                    resultCode = 1
                } catch (error : UserNotFoundException) {
                    // handle error here
                    resultCode = 2
                    Log.e(TAG, "Failed to signIn - User not found", error)
                }  catch (error : UserNotConfirmedException) {
                    // handle error here
                    resultCode = 3
                    Log.e(TAG, "User not confirmed", error)
                } catch (error : AuthException) {
                    // handle error here
                    resultCode = 4
                    Log.e(TAG, "Failed to signIn - AuthException", error)
                }
            } // end of withContext
        } // end of runBlocking

        Log.i(TAG, "saving the values in store - 1")
        // Store values in EncryptedSharedPreferences
        SharedPref.write(SharedPref.USER_NAME, emailValue)
        SharedPref.write(SharedPref.ACCOUNT_GUID, accountGuid)
        SharedPref.write(SharedPref.DISPLAY_NAME, "")

        if (resultCode == 1) {
            Log.i(TAG, "resultCode == 1")
            _loginResult.value =
                LoginResult(success = accountGuid?.let {
                    LoggedInUserView(
                        displayName = "",
                        username = emailValue,
                        accountGuid = it
                    )
                })
            Log.i(TAG, "Returning result now...")
            return Result.Success(LoggedInUser(accountGuid.toString(), emailValue))
        } else if (resultCode == 2) {
            Log.i(TAG, "resultCode == 2")
            _loginResult.value = LoginResult(error = R.string.user_not_found)
            val error = Throwable("User not found")
            Log.i(TAG, "returning resultCode == UserNotFoundException")
            return Result.Error(UserNotFoundException(error))
        } else if (resultCode == 3) {
            Log.i(TAG, "resultCode == 3")
            _loginResult.value = LoginResult(error = R.string.login_failed)
            val error = Throwable("User not confirmed")
            Log.i(TAG, "returning resultCode == UserNotConfirmedException")
            return Result.Error(UserNotConfirmedException(error))
        }  else if (resultCode == 4) {
            Log.i(TAG, "resultCode == 4")
            _loginResult.value = LoginResult(error = R.string.login_failed)
            val error = Throwable("Auth Exception")
            Log.i(TAG, "returning resultCode == Auth Exception")
            return Result.Error(AuthException("Login failed", "Auth Exception", error))
        } else {
            Log.i(TAG, "resultCode == unknown")
            _loginResult.value = LoginResult(error = R.string.login_failed)
            val error = Throwable("Unknown Exception")
            Log.i(TAG, "returning resultCode == Exception")
            return Result.Error(Exception(error))
        }
    }

    fun signUp(iAuthClient: IAuthClient, username: String, password: String): LoginResult {
        var result = loginRepository.login(username, password)
        var resultCode : Int = 0

        if (result is Result.Success) {
            accountGuid = result.data.userId;
        }

        Log.i(
            TAG, "signUp enter, email username= " + username
                    + " , password= " + password
                    + " ,accountGuid= " + accountGuid
        )

        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    // signIn now
                    Log.i(TAG, "signUpToAccount start")
                    val result = signUpToAccount(iAuthClient, username, password)
                    Log.i(TAG, "signUpToAccount success, result response=  $result")

                    Log.i(TAG, "saving the values in store - 1")
                    // Store values in EncryptedSharedPreferences
                    //accountGuid?.let { appPrefsStore?.setAccountDetails(emailValue, it, "") }
                    SharedPref.write(SharedPref.USER_NAME, username)
                    SharedPref.write(SharedPref.ACCOUNT_GUID, accountGuid)
                    SharedPref.write(SharedPref.DISPLAY_NAME, "")

                    if (result.isSignUpComplete == false) {
                        if (result.nextStep.signUpStep == AuthSignUpStep.CONFIRM_SIGN_UP_STEP)
                        {
                            // Confirmation code is sent
                            resultCode = 10
                        } else {
                            // what else ?
                        }
                    } else {
                        resultCode = 1
                    }
                } catch (error : UserNotFoundException) {
                    Log.i(TAG, "resultCode == 2")
                    // handle error here
                    resultCode = 2
                    Log.e(TAG, "Failed to signUp - User not found", error)
                } catch (error : UsernameExistsException) {
                    Log.i(TAG, "resultCode == 3")
                    // handle error here
                    resultCode = 3
                    Log.e(TAG, "Failed to signUp - UsernameExistsException", error)
                }  catch (error : UserNotConfirmedException) {
                    Log.i(TAG, "resultCode == UserNotConfirmedException")
                    // handle error here
                    resultCode = 10
                    Log.e(TAG, "Failed to signUp - UsernameExistsException", error)
                } catch (error : AuthException) {
                    Log.i(TAG, "resultCode == 4")
                    // handle error here
                    resultCode = 3
                    Log.e(TAG, "Failed to signUp - AuthException", error)
                }

            } // end of withContext
        } // end of runBlocking

        if (resultCode == 1) {
            Log.i(TAG, "signUp Returning resultCode == success")
            return LoginResult(success = LoggedInUserView(
                displayName = "",
                username = username,
                accountGuid = accountGuid.toString()))
        } else if (resultCode == 2) {
            Log.i(TAG, "signUp Returning resultCode == user_not_found")
            return LoginResult(error = R.string.user_not_found)
        } else if (resultCode == 3) {
            Log.i(TAG, "signUp Returning resultCode == UsernameExistsException")
            return LoginResult(error = R.string.login_failed)
        } else if (resultCode == 4) {
            Log.i(TAG, "signUp Returning resultCode == AuthException")
            return LoginResult(error = R.string.login_failed)
        }  else if (resultCode == 10) {
            Log.i(TAG, "signUp Returning resultCode == user_is_not_confirmed")
            return LoginResult(error = R.string.user_is_not_confirmed)
        } else {
            Log.i(TAG, "signUp Returning resultCode == default")
            return LoginResult(error = R.string.login_failed)
        }
    }

    suspend fun signInToAccount(iAuthClient: IAuthClient, emailValue : String, pwdValue: String): AuthSignInResult {
        Log.i(TAG, "signInToAccount")
        return iAuthClient.signIn(emailValue, pwdValue)
    }

    suspend fun signUpToAccount(iAuthClient: IAuthClient, emailValue: String, pwdValue: String): AuthSignUpResult {
        Log.i(TAG, "signUpToAccount")
        return iAuthClient.signUp(emailValue, pwdValue);
    }

    fun loginDataChanged(username: String, password: String) {
        Log.i(TAG, "loginDataChanged")
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
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