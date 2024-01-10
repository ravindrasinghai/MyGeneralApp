package com.rwt.mygeneral.view.login

import android.app.Activity
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
import com.rwt.mygeneral.databinding.ActivityConfirmsignupBinding
import com.rwt.mygeneral.interfaces.IAuthClient
import com.rwt.mygeneral.viewmodel.ConfirmSignUpViewModel
import com.rwt.mygeneral.viewmodel.ConfirmSignUpViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmSignUpActivity : AppCompatActivity() {

    @Inject lateinit var iAuthClient: IAuthClient
    private lateinit var confirmSignUpViewModel: ConfirmSignUpViewModel
    private lateinit var binding: ActivityConfirmsignupBinding

    val TAG:String = "MyGeneral::ConfirmSignUpActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "onCreate")
        binding = ActivityConfirmsignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val confirmcode = binding.confirmcode
        val confirm_button = binding.confirmButton
        val loading = binding.loading

        confirmSignUpViewModel = ViewModelProvider(this, ConfirmSignUpViewModelFactory())
            .get(ConfirmSignUpViewModel::class.java)

        confirm_button.isEnabled = true

        confirmSignUpViewModel.loginResult.observe(this@ConfirmSignUpActivity, Observer {
            var loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            Log.i(TAG, "confirmSignUpViewModel.loginResult.observe <")
            //if (loginResult.error != null && loginResult.error == R.string.user_not_found) {
            //    loginResult = userConfirmSignUp(getUserName(), confirmcode.text.toString())
            //}
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error!!)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success!!)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
            Log.i(TAG, "confirmSignUpViewModel.loginResult.observe >")
        })

        confirmcode.apply {
            afterTextChanged {
                confirmSignUpViewModel.confirmationDataChanged(
                    confirmcode.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        userConfirmSignUp(
                            confirmcode.text.toString())
                }
                false
            }

            confirm_button.setOnClickListener {
                loading.visibility = View.VISIBLE
                userConfirmSignUp(
                    confirmcode.text.toString())
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

    private fun userConfirmSignUp(confirmcode : String) : LoginResult {
        Log.i(TAG, "userConfirmSignUp $confirmcode")
        return confirmSignUpViewModel.confirmSignUp(iAuthClient, confirmcode)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Log.i(TAG, "showLoginFailed")
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}