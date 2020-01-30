package com.enesky.guvenlikbildir.ui.activity.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ActivityLoginBinding
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.activity.login.verify.VerifyCodeActivity
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.enesky.guvenlikbildir.utils.*
import com.google.firebase.auth.FirebaseUser
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel by lazy {
        getViewModel { LoginActivityVM() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login).apply {
                    viewModel = loginViewModel
                }

        val listener = MaskedTextChangedListener("+90 ([000]) [000] [00] [00]", et_phone_number)

        et_phone_number.apply {

            addTextChangedListener(listener)
            onFocusChangeListener = listener

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> startVerifyCodeActivity(et_phone_number.text.toString())
                }
                false
            }
        }

        btn_send_code.setOnClickListener {
            startVerifyCodeActivity(et_phone_number.text.toString())
        }

        tv_continue.setOnClickListener {
            if (checkInternet()) {
                App.managerAuth.signInAnonymously().addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("Login", "signInAnonymously:success")
                        val user = App.managerAuth.currentUser
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Log.w("Login", "signInAnonymously:failure", task.exception)
                        showToast("Authentication failed.")
                    }
                }
            } else {
                showToast("Internet bağlantısı bulunamadı.\nBazı fonksiyonlar pasif durumda olacaktır.")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
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
        val currentUser: FirebaseUser? = App.managerAuth.currentUser //todo:
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun startVerifyCodeActivity(phoneNumber: String) {
        if (isPhoneNumberValid(phoneNumber)) {
            val intent = Intent(this, VerifyCodeActivity::class.java)
            intent.putExtra("phoneNumber", phoneNumber)
            startActivity(intent)
            finish()
        } else {
            showToast("Geçersiz telefon numarası")
        }
    }

}
