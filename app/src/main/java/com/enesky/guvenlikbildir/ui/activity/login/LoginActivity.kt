package com.enesky.guvenlikbildir.ui.activity.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.BuildConfig
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ActivityLoginBinding
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LoginActivity : BaseActivity() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var binding: ActivityLoginBinding
    private var verification: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private val loginVM by lazy {
        getViewModel { LoginVM() }
    }

    @SuppressLint("TimberArgCount")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
                .apply {
                    viewModel = loginVM
                    lifecycleOwner = this@LoginActivity
                }
        loginVM.init(binding)

        val listener = MaskedTextChangedListener("+90 ([000]) [000] [00] [00]", et_phone_number)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Timber.tag("LoginActivity").d( "onVerificationCompleted: %s", credential)

                if (credential.smsCode != null) {
                    Timber.tag("onVerificationCompleted").d("Kodu havada yakaladın. -> %s", credential.smsCode)
                    showToast(getString(R.string.label_code_caught))
                    signInWithPhoneAuthCredential(credential)
                } else {
                    if (et_phone_number.text.toString().isPhoneNumberValid()) {
                        loginVM.setInputsEnabled(false)
                        openVerifyCodeActivity(et_phone_number.text.toString(), verification!!, resendingToken!!)
                    } else {
                        loginVM.setInputsEnabled(true)
                        til_phone_number.error = getString(R.string.label_invalid_phone_number)
                        til_phone_number.isErrorEnabled = true
                    }
                }

            }

            override fun onVerificationFailed(e: FirebaseException) {
                Timber.tag("LoginActivity").w("onVerificationFailed: %s", e.message)
                showToast(e.message)
                loginVM.setInputsEnabled(true)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Timber.tag("LoginActivity").d("onCodeSent: %1s, token: %2s", verification, token)
                verification = verificationId
                resendingToken = token

                if (BuildConfig.DEBUG && et_phone_number.text.toString() == Constants.testUserPhoneNumber)
                    openVerifyCodeActivity(Constants.testUserPhoneNumber, verificationId, token)
            }
        }

        et_phone_number.apply {
            addTextChangedListener(listener)
            onFocusChangeListener = listener

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    startPhoneNumberVerification(text.toString())
                false
            }
        }

        btn_send_code.setOnClickListener {
            startPhoneNumberVerification(et_phone_number.text.toString())
        }

        tv_continue.setOnClickListener {
            if (checkInternet()) {
                App.mAuth.signInAnonymously().addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Timber.tag("LoginActivity").d("signInAnonymously:success")
                        openMainActivity()
                    } else {
                        Timber.tag("LoginActivity").d(task.exception, "signInAnonymously:failure: %s")
                        showToast(getString(R.string.label_login_failed))
                    }
                }
            } else {
                showToast(getString(R.string.label_connection_not_found))
            }
        }

    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        if (phoneNumber.isPhoneNumberValid()) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, 60, TimeUnit.SECONDS, this, callbacks
            )
            loginVM.setInputsEnabled(false)
        } else {
            val errorMessage: String =
                if (phoneNumber.isEmpty())
                    getString(R.string.label_empty_input)
                else
                    getString(R.string.label_invalid_phone_number)
            til_phone_number.error = errorMessage
            til_phone_number.isErrorEnabled = true
            loginVM.setInputsEnabled(true)
        }
    }

}
