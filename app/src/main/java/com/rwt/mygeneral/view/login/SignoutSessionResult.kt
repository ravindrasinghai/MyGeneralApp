package com.rwt.mygeneral.view.login

/**
 * Authentication signout result : success (user details) or error message.
 */
data class SignoutSessionResult(
    val success: SessionStateView? = null,
    val error: Int? = null
)