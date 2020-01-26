package com.enesky.guvenlikbildir

import android.app.Application
import com.google.firebase.auth.FirebaseAuth

/**
 * Created by Enes Kamil YILMAZ on 26.01.2020
 */

class App : Application() {

    companion object {

        private lateinit var instance: App
        val managerInstance: App
            get() = instance

        private lateinit var mAuth: FirebaseAuth
        val managerAuth: FirebaseAuth
            get() = mAuth
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        mAuth = FirebaseAuth.getInstance()

    }

}