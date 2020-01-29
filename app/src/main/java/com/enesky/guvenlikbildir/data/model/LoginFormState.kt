package com.enesky.guvenlikbildir.data.model

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val phoneNumberError: Int? = null,
    val isDataValid: Boolean = false
)
