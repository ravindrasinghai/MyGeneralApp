package com.rwt.mygeneral.interfaces

import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.result.AuthSignOutResult
import com.amplifyframework.auth.result.AuthSignUpResult

interface IAuthClient {
    fun initPlugins()

    suspend fun getUser(): AuthUser

    suspend fun fetchAuthSession(): AuthSession

    suspend fun signIn(userName: String, userPassword: String): AuthSignInResult

    suspend fun signUp(userName: String, userPassword: String): AuthSignUpResult

    suspend fun confirmSignUp(userName: String, confirmationCode: String): AuthSignUpResult

    suspend fun signOut(): AuthSignOutResult
}