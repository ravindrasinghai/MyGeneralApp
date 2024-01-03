package com.rwt.mygeneral.interfaces

import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUser

interface IAuthClient {
    abstract val ConfirmSignInWithNewPasswordException: Throwable

    fun init()

    suspend fun getUser(): AuthUser

    suspend fun signIn(userName: String, userPassword: String): Result<Unit>

    suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit>

    suspend fun getAccessToken(): Result<String>

    suspend fun fetchUserAttributes(): Result<UserAttributes>

    suspend fun logout(): Result<Unit>
    abstract fun <AuthException> UnexpectedAuthException(error: AuthException): Throwable
    abstract fun UnexpectedException(error: Exception): Throwable
    abstract fun UpdatePasswordException(error: AuthException): Throwable
    abstract fun WrongCredentialException(s: String): Throwable?
    abstract fun GetAccessTokenException(s: String): Throwable?
    abstract fun LogoutException(): Throwable
    abstract fun FetchUserAttributesException(s: String, exception: AuthException): Throwable
}

class UserAttributes {

}
