package com.enesky.guvenlikbildir.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential

/**
 * Created by Enes Kamil YILMAZ on 31.01.2020
 */

fun Activity.signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
    App.managerAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
            Log.d("Login", "signInWithCredential:success")
            val user = task.result?.user
            showToast("signInWithCredential:success")
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()

            //TODO: Database'e bilgileri ekle.

        } else {
            Log.w("Login", "signInWithCredential:failure", task.exception)
            if (task.exception is FirebaseAuthInvalidCredentialsException)
                showToast("Hatalı kod")
        }
    }
}