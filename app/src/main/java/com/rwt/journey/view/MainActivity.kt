package com.rwt.mygeneral.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.kotlin.core.Amplify
import com.rwt.mygeneral.view.theme.MyGeneralTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.rwt.mygeneral.R
import com.rwt.mygeneral.view.login.LoginActivity

/*
 *      MainActivity
 *      - opens our fragment which has the UI
 */
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {

            // Adds our fragment
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<MainFragment>(R.id.fragment_container_view)
            }
        }
        Log.i("MyGeneral", " MainActivity: start authentication")
        // Start authentication
        performAuthentication();
    }


    private fun performAuthentication() {
        Log.i("MyGeneral", " MainActivity: launchHomeActivity")

        runBlocking {
            withContext(Dispatchers.IO) {
                Log.i("MyGeneral", " MainActivity: calling getUser")

                try {
                    val session = fetchAuthSession()
                    Log.i("MyGeneral", "Auth session = $session")
                    if (!session.isSignedIn) {
                        launchLoginActivity();
                    } else {
                        launchHomeActivity();
                    }

                } catch (error: AuthException) {
                    Log.e("MyGeneral", "Failed to fetch auth session", error)
                }
            }
        }
    }

    private fun getCurrentUserDetails() {
        var state : Int = 0;
        runBlocking {
            withContext(Dispatchers.IO) {
                Log.i("MyGeneral", " MainActivity: calling getUser")
                try {
                    var curAuthUser: AuthUser = getUser();
                    state = 1;
                } catch (exception : Exception) {
                    state = 0;
                    Log.e("MyGeneral", " Exception : failed to getUser")
                }
            }
        }
    }

    private fun launchHomeActivity() {
        //TODO("Not yet implemented")
        Log.i("MyGeneral", " MainActivity: launchHomeActivity")
    }

    private fun launchLoginActivity() {
        //TODO("Not yet implemented")
        Log.i("MyGeneral", " MainActivity: launchLoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        //intent.putExtra("key", value)
        startActivity(intent)
    }

    suspend fun getUser(): AuthUser {
        Log.i("MyGeneral", " getUser details")
        return Amplify.Auth.getCurrentUser();
    }

    suspend fun fetchAuthSession(): AuthSession {
        Log.i("MyGeneral", "fetchAuthSession details")
        return Amplify.Auth.fetchAuthSession();
    }

}

