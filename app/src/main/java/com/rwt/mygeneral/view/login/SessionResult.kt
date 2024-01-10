package com.rwt.mygeneral.view.login

/**
 * Authentication result : success (user details) or error message.
 */
data class SessionResult(
    val success: SessionStateView? = null,
    val error: Int? = null
)