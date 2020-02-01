package com.enesky.guvenlikbildir.ui.activity.login.verify

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ActivityVerifyCodeBinding
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.utils.Constants
import com.enesky.guvenlikbildir.utils.getViewModel
import com.enesky.guvenlikbildir.utils.showToast
import com.enesky.guvenlikbildir.utils.signInWithPhoneAuthCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_verify_code.*
import java.util.concurrent.TimeUnit

class VerifyCodeActivity: BaseActivity() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var binding: ActivityVerifyCodeBinding
    private var phoneNumber: String? = null
    private var verification: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private val verifyCodeViewModel by lazy {
        getViewModel { VerifyCodeVM() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityVerifyCodeBinding>(this, R.layout.activity_verify_code).apply {
            viewModel = verifyCodeViewModel
            lifecycleOwner = this@VerifyCodeActivity
        }
        verifyCodeViewModel.init(binding)

        phoneNumber = intent.getStringExtra("phoneNumber")
        verification = intent.getStringExtra("verificationId")
        resendingToken = intent.getParcelableExtra("token")

        if (phoneNumber == Constants.testUserPhoneNumber)
            et_verify_code.setText(Constants.testUserVerifyCode)

        startCountDown()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("VerifyCodeActivity", "onVerificationCompleted:$credential")
                if (credential.smsCode != null) {
                    et_verify_code.setText(credential.smsCode)
                    verifyCodeViewModel.setInputsEnabled(false)
                }

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w("VerifyCodeActivity", "onVerificationFailed:${e.message}")
                showToast(e.message)
                verifyCodeViewModel.setInputsEnabled(true)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d("VerifyCodeActivity", "onCodeSent:$verificationId")
                resendingToken = token
                verification = verificationId
            }

        }

        et_verify_code.apply {
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        verifyPhoneNumberWithCode(verification, et_verify_code.text.toString())
                    }
                }
                false
            }
        }

        btn_sign_in.setOnClickListener {
            verifyPhoneNumberWithCode(verification, et_verify_code.text.toString())
        }

        btn_resend_code.setOnClickListener {
            if (resendingToken != null)
                resendVerificationCode(phoneNumber!!, resendingToken)
        }

    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        verifyCodeViewModel.setInputsEnabled(false)
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, 60, TimeUnit.SECONDS, this, callbacks, token
        )
        startCountDown()
        verifyCodeViewModel.setInputsEnabled(true)
    }

    private fun startCountDown() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_countdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                tv_countdown.text = "0"
                verifyCodeViewModel.setInputsEnabled(false)
            }
        }.start()
    }

}
