package com.rwt.mygeneral.view.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rwt.mygeneral.R
import com.rwt.mygeneral.databinding.ActivityLoginBinding
import com.rwt.mygeneral.interfaces.IAuthClient
import com.rwt.mygeneral.viewmodel.LoginViewModel
import com.rwt.mygeneral.viewmodel.LoginViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject lateinit var iAuthClient: IAuthClient
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    val TAG:String = "MyGeneral::LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "onCreate")
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login_button = binding.login
        val signup_button = binding.signup
        val loading = binding.loading

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login_button.isEnabled = loginState.isDataValid
            if (signup_button != null) {
                signup_button.isEnabled = loginState.isDataValid
            }

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            Log.i(TAG, "loginViewModel.loginResult.observe <")
            var loginResult = it ?: return@Observer

            loading.visibility = View.GONE

            Log.i(TAG, "checking result value now...")
            if (loginResult.error != null) {
                Log.i(TAG, " result value : error")
                showLoginFailed(loginResult.error!!)
            }
            if (loginResult.success != null) {
                Log.i(TAG, " result value : success")
                updateUiWithUser(loginResult.success!!)
                //Complete and destroy login activity once successful
                finish()
            }
            setResult(Activity.RESULT_OK)

            Log.i(TAG, "loginViewModel.loginResult.observe >")
        })

        username.afterTextChanged1 {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged1 {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            iAuthClient,
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login_button.setOnClickListener {
                loading.visibility = View.VISIBLE
                Log.i(TAG, "setOnClickListener : login < ")
                loginViewModel.login(iAuthClient, username.text.toString(), password.text.toString())
                Log.i(TAG, "setOnClickListener : login > ")
            }

            if (signup_button != null) {
                signup_button.setOnClickListener {
                    loading.visibility = View.VISIBLE
                    Log.i(TAG, "setOnClickListener : signUp < ")
                    val loginResult =loginViewModel.signUp(iAuthClient, username.text.toString(), password.text.toString())

                    if (loginResult.error != null && loginResult.error == R.string.user_is_not_confirmed) {
                        Log.i(TAG, "setOnClickListener : launchConfirmSignUpActivity ")
                        launchConfirmSignUpActivity("loginResult.success.accountGuid", username.text.toString())
                    }
                    finish();
                }
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        Log.i(TAG, "updateUiWithUser")
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun launchConfirmSignUpActivity(accountGuid: String, userEmail : String) {
        Log.i(TAG, " launchConfirmSignUpActivity")
        val intent = Intent(this, ConfirmSignUpActivity::class.java)
        intent.putExtra("accountGuid", accountGuid)
        intent.putExtra("userEmail", userEmail)
        startActivity(intent)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Log.i(TAG, "showLoginFailed")
        if (errorString == R.string.user_not_found) {
            Log.i(TAG, "showLoginFailed - user not found")
            Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
        }  else if (errorString == R.string.user_is_not_confirmed) {
            Log.i(TAG, "showLoginFailed - user is not confirmed")
            Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged1(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}