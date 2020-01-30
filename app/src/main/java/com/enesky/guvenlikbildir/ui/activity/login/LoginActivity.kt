package com.enesky.guvenlikbildir.ui.activity.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ActivityLoginBinding
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.enesky.guvenlikbildir.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private val loginViewModel by lazy {
        getViewModel { LoginActivityVM() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login).apply {
                    viewModel = loginViewModel
                }

        loginViewModel.loginFormState.observe(this, Observer {
            if (it.isDataValid)
                showToast("Data valid")
            else
                showToast(getString(it.phoneNumberError!!))
        })

        val listener = MaskedTextChangedListener("+90 ([000]) [000] [00] [00]", et_phone_number)

        et_phone_number.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(et_phone_number.text.toString())
            }

            addTextChangedListener(listener)

            onFocusChangeListener = listener

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> startPhoneNumberVerification(et_phone_number.text.toString())
                }
                false
            }
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
                        finish()
                        //updateUI(user)
                    } else {
                        Log.w("Login", "signInAnonymously:failure", task.exception)
                        showToast("Authentication failed.")
                        //updateUI(null)
                    }
                    // ...
                }
            } else {
                showToast("Internet bağlantısı bulunamadı.\nBazı fonksiyonlar pasif durumda olacaktır.")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("Login", "onVerificationCompleted:$credential")
                verificationInProgress = false

                // Update the UI and attempt sign in with the phone credential
                //updateUI(STATE_VERIFY_SUCCESS, credential)
                //signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("Login", "onVerificationFailed", e)
                verificationInProgress = false

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    //fieldPhoneNumber.error = "Invalid phone number."
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                // Show a message and update the UI
                // updateUI(STATE_VERIFY_FAILED)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("Login", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                // Update UI
                //updateUI(STATE_CODE_SENT)
            }
        }

        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer {
            if (it)
                showToast("Online")
            else
                showToast("Offline")
        })

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser: FirebaseUser? = App.managerAuth.currentUser //todo:
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        // updateUI(currentUser)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks

        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        App.managerAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Login", "signInWithCredential:success")

                    val user = task.result?.user
                    //updateUI(STATE_SIGNIN_SUCCESS, user)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("Login", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        //fieldVerificationCode.error = "Invalid code."
                    }

                    //updateUI(STATE_SIGNIN_FAILED)
                }
            }
    }

}
