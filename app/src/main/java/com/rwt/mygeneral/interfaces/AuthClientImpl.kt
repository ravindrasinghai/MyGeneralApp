package com.rwt.mygeneral.interfaces

import android.content.Context
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.result.AuthSignOutResult
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.datastore.AWSDataStorePlugin
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthClientImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : IAuthClient {

    val TAG = "MyGeneral::AuthClientImpl"
    override fun initPlugins() {
        try {
            Log.i(TAG, "initPlugins");
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i(TAG, "Plugins added successfully");
        } catch (e: AmplifyException) {
            Log.e(TAG, "oops, something went wrong");
            e.printStackTrace();
        } catch (e: Exception) {
            Log.e("AuthClientImpl", "Amplify init error", e)
        }
    }

    override suspend fun fetchAuthSession(): AuthSession {
        Log.i(TAG, "fetchAuthSession")
        return Amplify.Auth.fetchAuthSession()
    }

    override suspend fun getUser(): AuthUser {
        Log.i(TAG, " getUser")
        return Amplify.Auth.getCurrentUser()
    }

    override suspend fun signIn(userName: String, userPassword: String): AuthSignInResult {
        Log.i(TAG, "signIn")
        return Amplify.Auth.signIn(userName, userPassword)
    }

    override suspend fun signUp(userName: String, userPassword: String): AuthSignUpResult {
        Log.i(TAG, "signUp")
        return Amplify.Auth.signUp(userName, userPassword)
    }

    override suspend fun confirmSignUp(userName: String, confirmationCode: String): AuthSignUpResult {
        Log.i(TAG, "confirmSignUp")
        return Amplify.Auth.confirmSignUp(userName, confirmationCode)
    }

    override suspend fun signOut(): AuthSignOutResult {
        Log.i(TAG, "signOut")
        return Amplify.Auth.signOut()
    }
}