package com.enesky.guvenlikbildir.ui.activity.login.verify

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.utils.makeItVisible
import com.enesky.guvenlikbildir.utils.showToast
import com.enesky.guvenlikbildir.utils.signInWithPhoneAuthCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_verify_code.*
import java.util.concurrent.TimeUnit

class VerifyCodeActivity : AppCompatActivity() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("Login", "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                showToast(e.message)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("Login", "onCodeSent:$verificationId")
            }
        }

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_countdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                tv_countdown.text = "0"
                tv_timeup.makeItVisible()
            }
        }.start()

        et_verify_code.apply {
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        verifyPhoneNumberWithCode(
                            intent.getStringExtra("verificationId")!!,
                            et_verify_code.text.toString()
                        )
                    }
                }
                false
            }
        }

        btn_sign_in.setOnClickListener {
            verifyPhoneNumberWithCode(
                intent.getStringExtra("verificationId")!!,
                et_verify_code.text.toString()
            )
        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, 60, TimeUnit.SECONDS, this, callbacks, token
        )
    }

}
