package com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.ui.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String
)