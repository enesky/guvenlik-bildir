package com.enesky.guvenlikbildir.ui.fragment.options.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.BuildConfig
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentLoginBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.redmadrobot.inputmask.MaskedTextChangedListener
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LoginFragment : BaseFragment() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var binding: FragmentLoginBinding
    private var verification: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private val loginVM by lazy {
        getViewModel { LoginVM() }
    }

    @SuppressLint("TimberArgCount")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<FragmentLoginBinding>(activity!!, R.layout.fragment_login)
                .apply {
                    viewModel = loginVM
                    lifecycleOwner = this@LoginFragment
                }
        loginVM.init(binding)

        val listener = MaskedTextChangedListener("+90 ([000]) [000] [00] [00]", binding.etPhoneNumber)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Timber.tag("LoginFragment").d( "onVerificationCompleted: %s", credential)

                if (credential.smsCode != null) {
                    Timber.tag("onVerificationCompleted").d("Kodu havada yakaladın. -> %s", credential.smsCode)
                    context!!.showToast(getString(R.string.label_code_caught))
                    //signInWithPhoneAuthCredential(credential)
                } else {
                    if (binding.etPhoneNumber.text.toString().isPhoneNumberValid()) {
                        loginVM.setInputsEnabled(false)
                        //openVerifyCodeActivity(et_phone_number.text.toString(), verification!!, resendingToken!!)
                    } else {
                        loginVM.setInputsEnabled(true)
                        binding.tilPhoneNumber.error = getString(R.string.label_invalid_phone_number)
                        binding.tilPhoneNumber.isErrorEnabled = true
                    }
                }

            }

            override fun onVerificationFailed(e: FirebaseException) {
                Timber.tag("LoginFragment").w("onVerificationFailed: %s", e.message)
                context!!.showToast(e.message)
                loginVM.setInputsEnabled(true)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Timber.tag("LoginFragment").d("onCodeSent: %1s, token: %2s", verification, token)
                verification = verificationId
                resendingToken = token

                if (BuildConfig.LOG_ENABLED && binding.etPhoneNumber.text.toString() == Constants.testUserPhoneNumber)
                    activity!!.openVerifyCodeActivity(Constants.testUserPhoneNumber, verificationId, token)
            }
        }

        binding.etPhoneNumber.apply {
            addTextChangedListener(listener)
            onFocusChangeListener = listener

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    startPhoneNumberVerification(text.toString())
                false
            }
        }

        binding.btnSendCode.setOnClickListener {
            startPhoneNumberVerification(binding.etPhoneNumber.text.toString())
        }

        binding.tvContinue.setOnClickListener {
           /*if (activity!!.checkInternet()) {
                App.mAuth.signInAnonymously().addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        Timber.tag("LoginFragment").d("signInAnonymously:success")
                        activity!!.openMainActivity()
                    } else {
                        Timber.tag("LoginFragment").d(task.exception, "signInAnonymously:failure: %s")
                        activity!!.showToast(getString(R.string.label_login_failed))
                    }
                }
            } else {
                activity!!.showToast(getString(R.string.label_connection_not_found))
            }*/
        }

    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        if (phoneNumber.isPhoneNumberValid()) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, 60, TimeUnit.SECONDS, activity!!, callbacks
            )
            loginVM.setInputsEnabled(false)
        } else {
            val errorMessage: String =
                if (phoneNumber.isEmpty())
                    getString(R.string.label_empty_input)
                else
                    getString(R.string.label_invalid_phone_number)
            binding.tilPhoneNumber.error = errorMessage
            binding.tilPhoneNumber.isErrorEnabled = true
            loginVM.setInputsEnabled(true)
        }
    }

}
