package com.enesky.guvenlikbildir.ui.activity.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns

import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.data.model.LoginFormState

class LoginActivityVM : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    fun loginDataChanged(phoneNumber: String) {
        if (!isPhoneNumberValid(phoneNumber))
            _loginForm.value = LoginFormState(phoneNumberError = R.string.invalid_phone_number)
        else
            _loginForm.value = LoginFormState(isDataValid = true)

    }

    private fun isPhoneNumberValid(username: String): Boolean {
        return if (username.length > 15)
            Patterns.PHONE.matcher(username).matches()
        else
            username.isNotBlank()
    }

}
