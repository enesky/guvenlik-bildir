package com.enesky.guvenlikbildir.ui.activity.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App
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
                    showToast("Kodu senin için yakaladım. :)")
                    signInWithPhoneAuthCredential(credential)
                } else
                    startVerifyCodeActivity(
                        et_phone_number.text.toString(),
                        verification!!,
                        resendingToken!!
                    )
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
                Timber.tag("LoginActivity").d("onCodeSent: %s", verificationId)
                verification = verificationId
                resendingToken = token

                if (et_phone_number.text.toString() == Constants.testUserPhoneNumber)
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
                        showToast("Giriş başarısız.")
                    }
                }
            } else {
                showToast("İnternet bağlantısı bulunamadı. \n Lütfen tekrar deneyiniz")
            }
        }

    }

    fun startVerifyCodeActivity(
        phoneNumber: String,
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        if (phoneNumber.isPhoneNumberValid()) {
            loginVM.setInputsEnabled(false)
            openVerifyCodeActivity(phoneNumber, verificationId, token)
        } else {
            til_phone_number.error = "Geçersiz telefon numarası"
            til_phone_number.isErrorEnabled = true
            loginVM.setInputsEnabled(true)
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        if (phoneNumber.isPhoneNumberValid()) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, 60, TimeUnit.SECONDS, this, callbacks
            )
            loginVM.setInputsEnabled(false)
        } else {
            til_phone_number.error = "Geçersiz telefon numarası"
            til_phone_number.isErrorEnabled = true
            loginVM.setInputsEnabled(true)
        }

    }

}
