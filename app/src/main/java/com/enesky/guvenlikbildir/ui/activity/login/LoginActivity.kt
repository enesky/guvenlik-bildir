package com.enesky.guvenlikbildir.ui.activity.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ActivityLoginBinding
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.activity.login.verify.VerifyCodeActivity
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.enesky.guvenlikbildir.utils.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class LoginActivity: BaseActivity() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var binding: ActivityLoginBinding
    private var verification: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private val loginViewModel by lazy {
        getViewModel { LoginActivityVM() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login).apply {
                    viewModel = loginViewModel
                    lifecycleOwner = this@LoginActivity
                }
        loginViewModel.init(binding)

        val listener = MaskedTextChangedListener("+90 ([000]) [000] [00] [00]", et_phone_number)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("Login", "onVerificationCompleted:$credential")

                if (credential.smsCode != null)
                    signInWithPhoneAuthCredential(credential)
                else
                    startVerifyCodeActivity(et_phone_number.text.toString(),
                                            verification!!,
                                            resendingToken!!)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("Login", "onVerificationFailed:${e.message}")
                showToast(e.message)
                loginViewModel.setInputsEnabled(true)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d("Login", "onCodeSent:$verificationId")
                verification = verificationId
                resendingToken = token
            }
        }

        et_phone_number.apply {
            addTextChangedListener(listener)
            onFocusChangeListener = listener
        }

        btn_send_code.setOnClickListener {
            startPhoneNumberVerification(et_phone_number.text.toString())
        }

        tv_continue.setOnClickListener {
            if (checkInternet()) {
                App.managerAuth.signInAnonymously().addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("Login", "signInAnonymously:success")
                        val user = App.managerAuth.currentUser
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    } else {
                        Log.w("Login", "signInAnonymously:failure", task.exception)
                        showToast("Authentication failed.")
                    }
                }
            } else {
                showToast("Internet bağlantısı bulunamadı.\nBazı fonksiyonlar pasif durumda olacaktır.")
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = App.managerAuth.currentUser
        if (currentUser != null) {
            loginViewModel.setInputsEnabled(false)
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }

    fun startVerifyCodeActivity(phoneNumber: String,
                                verificationId: String,
                                token: PhoneAuthProvider.ForceResendingToken) {
        if (phoneNumber.isPhoneNumberValid()) {
            loginViewModel.setInputsEnabled(false)
            val intent = Intent(this, VerifyCodeActivity::class.java)
            intent.putExtra("phoneNumber", phoneNumber)
            intent.putExtra("verificationId", verificationId)
            intent.putExtra("token", token)
            startActivity(intent)
        } else {
            til_phone_number.error = "Geçersiz telefon numarası"
            til_phone_number.isErrorEnabled = true
            loginViewModel.setInputsEnabled(true)
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        if (phoneNumber.isPhoneNumberValid()) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, 60, TimeUnit.SECONDS, this, callbacks
            )
            loginViewModel.setInputsEnabled(false)
        } else {
            til_phone_number.error = "Geçersiz telefon numarası"
            til_phone_number.isErrorEnabled = true
            loginViewModel.setInputsEnabled(true)
        }

    }

}
