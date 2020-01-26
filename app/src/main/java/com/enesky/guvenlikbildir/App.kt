package com.enesky.guvenlikbildir

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        private lateinit var mAnalytics: FirebaseAnalytics
        val managerAnalytics: FirebaseAnalytics
            get() = managerAnalytics

        private lateinit var mFirestore: FirebaseFirestore
        val managerFirestore: FirebaseFirestore
            get() = mFirestore

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        mAuth = FirebaseAuth.getInstance()
        mAnalytics = FirebaseAnalytics.getInstance(this)
        mFirestore = FirebaseFirestore.getInstance()
    }

}