package com.rwt.mygeneral.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String, // Email id
    val accountGuid: String // UUID
)