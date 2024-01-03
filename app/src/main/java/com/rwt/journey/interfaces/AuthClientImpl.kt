package com.rwt.mygeneral.interfaces

import android.content.Context
import android.util.Log
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.auth.result.step.AuthSignInStep
import com.amplifyframework.core.Amplify
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class AuthClientImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    override val ConfirmSignInWithNewPasswordException: Throwable
) : IAuthClient {

    override fun init() {
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("AuthClientImpl", "AmplifySsoClient Amplify initializing")
        } catch (e: Exception) {
            Log.e("AuthClientImpl", "Amplify init error", e)
        }
    }

    override suspend fun getUser(): AuthUser {
        return suspendCoroutine { continuation ->
            try {
                Amplify.Auth.getCurrentUser( {
                    continuation.resume(it)
                }, { error ->
                    Log.i("AuthClientImpl", "Amplify Update password confirm signIn error", error)
                    continuation.resume()
                })
            } catch (exception: Exception) {
                Log.i("AuthClientImpl", "Password update failed unexpected exception", exception)
                continuation.resume()
            }
        }
    }


    override suspend fun signIn(userName: String, userPassword: String): Result<Unit> {
        return suspendCoroutine { continuation ->
            try {
                Amplify.Auth.signIn(userName, userPassword, { signInResult ->
                    when {
                        signInResult.isSignedIn -> {
                            continuation.resume(Result.success(Unit))
                        }
                        signInResult.nextStep.signInStep == AuthSignInStep.CONFIRM_SIGN_IN_WITH_NEW_PASSWORD -> {
                            continuation.resume(Result.failure(ConfirmSignInWithNewPasswordException))
                        }
                        else -> {
                            Log.i("AuthClientImpl", "SignIn", WrongCredentialException("Unexpected login next step - ${signInResult.nextStep.signInStep}"))
//                            continuation.resume(Result.failure(WrongCredentialException("Unexpected login next step - ${signInResult.nextStep.signInStep}")))
                        }
                    }
                }, { authException ->
/*
                    if (authException is AuthException.NotAuthorizedException) {
                        continuation.resume(Result.failure(WrongCredentialException(message = "Wrong credential exception")))
                    } else {
                        Log.i("AuthClientImpl", "Amplify signIn auth", authException)
                        continuation.resume(Result.failure(UnexpectedAuthException(error = authException)))
                    }
*/

                })
            } catch (authException: Exception) {
                Log.i("AuthClientImpl", "Unexpected signIn exception", authException)
                continuation.resume(Result.failure(UnexpectedException(error = authException)))
            }
        }
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return suspendCoroutine { continuation ->
            try {
                Amplify.Auth.confirmSignIn(newPassword, {
                    continuation.resume(Result.success(Unit))
                }, { error ->
                    Log.i("AuthClientImpl", "Amplify Update password confirm signIn error", error)
                    continuation.resume(Result.failure(UpdatePasswordException(error = error)))
                })
            } catch (exception: Exception) {
                Log.i("AuthClientImpl", "Password update failed unexpected exception", exception)
                continuation.resume(Result.failure(UnexpectedException(error = exception)))
            }
        }
    }

    override suspend fun getAccessToken(): Result<String> {
        return suspendCoroutine { continuation ->
            try {
                Amplify.Auth.fetchAuthSession(
                    { result: AuthSession ->
                        val cognitoAuthSession = result as AWSCognitoAuthSession

                        if (cognitoAuthSession.userPoolTokensResult.type == AuthSessionResult.Type.FAILURE) {
                            // Handling no session here.
                            Log.i("AuthClientImpl", "Amplify no session error", GetAccessTokenException("Amplify no session"))
                            //continuation.resume(Result.failure(GetAccessTokenException("Amplify no session")))
                        }

                        cognitoAuthSession.userPoolTokensResult.value?.accessToken?.let { token ->
                            continuation.resume(Result.success(token))
                        } ?: run {
                            Log.i("AuthClientImpl", "Amplify token is empty", GetAccessTokenException("Token is empty"))
                            //continuation.resume(Result.failure(GetAccessTokenException("Token is empty")))
                        }
                    }, { error ->
                        Log.i("AuthClientImpl", "Amplify get access token exception", error)
                        //continuation.resume(Result.failure(GetAccessTokenException(error = error)))
                    })
            } catch (exception: Exception) {
                Log.i("AuthClientImpl", "Amplify get access token failed unexpected exception", exception)
                continuation.resume(Result.failure(UnexpectedAuthException(error = exception)))
            }
        }
    }

    override suspend fun fetchUserAttributes(): Result<UserAttributes> {
        return suspendCoroutine { continuation ->
            try {
                Amplify.Auth.fetchUserAttributes({ userAttributesList ->
                    val userAttributes = UserAttributes(
                        //userName = userAttributesList.getByKey(AuthUserAttributeKey.name()),
                        //userFamilyName = userAttributesList.getByKey(AuthUserAttributeKey.familyName()),
                        //systemId = userAttributesList.getByKey(AuthUserAttributeKey.custom(SYSTEM_ID_KEY))
                    )
                    continuation.resume(Result.success(userAttributes))
                }, { exception ->
                    Log.i("AuthClientImpl", "Fetch user Attributes exception", exception)
                    continuation.resume(Result.failure(FetchUserAttributesException("Fetch user Attributes exception", exception)))

                })
            } catch (exception: Exception) {
                Log.i("AuthClientImpl", "Fetch user attributes exception", exception)
                //continuation.resume(Result.failure(UnexpectedAuthException("Fetch user attributes exception", exception)))
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return suspendCoroutine { continuation ->
            try {
                Log.i("AuthClientImpl", "Logout- Not implemented")
            } catch (error: Exception) {
                Log.i("AuthClientImpl", "Amplify logout unexpected exception", error)
            }
        }
    }

    override fun <AuthException> UnexpectedAuthException(error: AuthException): Throwable {
        TODO("Not yet implemented")
    }

    override fun UnexpectedException(error: Exception): Throwable {
        TODO("Not yet implemented")
    }

    override fun UpdatePasswordException(error: AuthException): Throwable {
        TODO("Not yet implemented")
    }

    override fun WrongCredentialException(s: String): Throwable? {
        TODO("Not yet implemented")
    }

    override fun GetAccessTokenException(s: String): Throwable? {
        TODO("Not yet implemented")
    }

    override fun LogoutException(): Throwable {
        TODO("Not yet implemented")
    }

    override fun FetchUserAttributesException(s: String, exception: AuthException): Throwable {
        TODO("Not yet implemented")
    }

    private fun List<AuthUserAttribute>.getByKey(key: AuthUserAttributeKey): String? {
        return this.firstOrNull { it.key == key }?.value
    }
}

private fun Any.resume() {
    TODO("Not yet implemented")
}
