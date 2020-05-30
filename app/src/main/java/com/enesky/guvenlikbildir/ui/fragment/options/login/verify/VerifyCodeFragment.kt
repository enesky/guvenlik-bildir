package com.enesky.guvenlikbildir.ui.fragment.options.login.verify

import android.os.Bundle
import android.os.CountDownTimer
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.BuildConfig
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentVerifyCodeBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_verify_code.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class VerifyCodeFragment: BaseFragment() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var binding: FragmentVerifyCodeBinding
    private var phoneNumber: String? = null
    private var verification: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private val verifyCodeViewModel by lazy {
        getViewModel { VerifyCodeVM() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<FragmentVerifyCodeBinding>(activity!!, R.layout.fragment_verify_code).apply {
            viewModel = verifyCodeViewModel
            lifecycleOwner = this@VerifyCodeFragment
        }
        verifyCodeViewModel.init(binding)

        //phoneNumber = intent.getStringExtra("phoneNumber")
        //verification = intent.getStringExtra("verificationId")
        //resendingToken = intent.getParcelableExtra("token")

        if (BuildConfig.DEBUG && phoneNumber == Constants.testUserPhoneNumber)
            et_verify_code.setText(Constants.testUserVerifyCode)

        startCountDown()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Timber.tag("VerifyCodeFragment").d("onVerificationCompleted: %s", credential)
                if (credential.smsCode != null) {
                    context!!.showToast(getString(R.string.label_code_caught))
                    et_verify_code.setText(credential.smsCode)
                    verifyCodeViewModel.setInputsEnabled(false)
                    //signInWithPhoneAuthCredential(credential)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Timber.tag("VerifyCodeFragment").w("onVerificationFailed: %s", e.message)
                context!!.showToast(e.message)
                verifyCodeViewModel.setInputsEnabled(true)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Timber.tag("VerifyCodeFragment").d("onCodeSent: %s ", verificationId)
                resendingToken = token
                verification = verificationId
                context!!.showToast("Kod gönderildi.")
            }

        }

        et_verify_code.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    verifyPhoneNumberWithCode(verification, et_verify_code.text.toString())
                false
            }
        }

        btn_sign_in.setOnClickListener {
            verifyPhoneNumberWithCode(verification, et_verify_code.text.toString())
        }

        btn_resend_code.setOnClickListener {
            if (resendingToken != null)
                resendVerificationCode(phoneNumber!!, resendingToken)
            else if (BuildConfig.DEBUG)
                verifyCodeViewModel.setInputsEnabled(true)
        }

    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        verifyCodeViewModel.setInputsEnabled(false)
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        //signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, 60, TimeUnit.SECONDS, activity!!, callbacks, token
        )
        startCountDown()
        verifyCodeViewModel.setInputsEnabled(true)
    }

    private fun startCountDown() {
        tv_timeup.makeItGone()
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_countdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                tv_countdown.text = "0"
                tv_timeup.makeItVisible()
                verifyCodeViewModel.setInputsEnabled(false)
            }
        }.start()
    }

}
