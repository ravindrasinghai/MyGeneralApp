package com.rwt.mygeneral.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rwt.mygeneral.R
import com.rwt.mygeneral.data.SharedPref
import com.rwt.mygeneral.interfaces.IAuthClient
import com.rwt.mygeneral.view.login.LoginActivity
import com.rwt.mygeneral.viewmodel.MainViewModel
import com.rwt.mygeneral.viewmodel.MainViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/*
 *      MainActivity
 *      - opens our fragment which has the UI
 */
@AndroidEntryPoint
class MainActivity : OptionsMenuActivity() {

    val TAG = "MyGeneral::MainActivity"
    private lateinit var mainViewModel: MainViewModel
    @Inject lateinit var iAuthClient: IAuthClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, " onCreate <")
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this, MainViewModelFactory())
            .get(MainViewModel::class.java)

        if (savedInstanceState == null) {
            // Initialize SharedPreferences
            SharedPref.init(getApplicationContext());

            // Adds our fragment
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<MainFragment>(R.id.fragment_container_view)
            }
        }
        Log.i(TAG, " initializing MainViewModel")
        // Start authentication
        applicationOnCreate();

        mainViewModel.sessionResult.observe(this@MainActivity, Observer {
            Log.i(TAG, "sessionResult.observe <")
            var sessionResult = it ?: return@Observer

            if (sessionResult.success != null) {
                Log.i(TAG, " sessionResult value : success")

                if (sessionResult.success!!.sessionState == "user_not_signedin") {
                    launchLoginActivity()
                } else if (sessionResult.success!!.sessionState == "user_already_signedin") {
                    launchHomeActivity()
                }
            }

            if (sessionResult.error != null) {
                Log.e(TAG, " sessionResult error value : $sessionResult")
                launchLoginActivity()
            }
            setResult(Activity.RESULT_OK)
            Log.i(TAG, "sessionResult.observe >")
        })
        mainViewModel.signoutSessionResult.observe(this@MainActivity, Observer {
            Log.i(TAG, "signoutSessionResult.observe <")
            var signoutSessionResult = it ?: return@Observer

            if (signoutSessionResult.success != null) {
                Log.i(TAG, " signoutSessionResult value : success")

                if (signoutSessionResult.success!!.sessionState == "user_signedout_success") {
                    Log.i(TAG, " User signed-out so take user to login screen now")
                    launchLoginActivity()
                } else if (signoutSessionResult.success!!.sessionState == "user_already_signedin") {
                    // TODO probably show a toast here that user is already signout
                }
            }

            if (signoutSessionResult.error != null) {
                Log.e(TAG, " signoutSessionResult error value : $signoutSessionResult")
                // no-op for now
            }

            setResult(Activity.RESULT_OK)

            Log.i(TAG, "signoutSessionResult.observe >")
        })
        Log.i(TAG, " onCreate >")
    } // end of onCreate


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, " onOptionsItemSelected <")
        // Handle item selection.
        return when (item.itemId) {
            R.id.menu_signout -> {
                Log.i(TAG, " onOptionsItemSelected - menu_signout 1")
                signoutUser()
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signoutUser() {
        Log.i(TAG, " signoutUser")
        mainViewModel.signoutUser(iAuthClient);
    }

    private fun applicationOnCreate() {
        Log.i(TAG, " applicationOnCreate")
        mainViewModel.applicationOnCreate(iAuthClient);
    }

    private fun launchHomeActivity() {
        Log.i(TAG, " launchHomeActivity")
    }

    private fun launchLoginActivity() {
        Log.i(TAG, " launchLoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}

