package com.enesky.guvenlikbildir

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.enesky.guvenlikbildir.extensions.Constants
import com.enesky.guvenlikbildir.extensions.getResponseFromJson
import com.enesky.guvenlikbildir.model.EarthquakeOA
import com.enesky.guvenlikbildir.model.GenericResponse
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.reflect.TypeToken
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Created by Enes Kamil YILMAZ on 26.01.2020
 */

class App : Application() {

    lateinit var mockEarthquakeList : GenericResponse<EarthquakeOA>

    companion object {
        private lateinit var instance: App
        val mInstance: App
            get() = instance

        private lateinit var firebaseAuth: FirebaseAuth
        val mAuth: FirebaseAuth
            get() = firebaseAuth

        private lateinit var firebaseAnalytics: FirebaseAnalytics
        val mAnalytics: FirebaseAnalytics
            get() = mAnalytics

        private lateinit var firebaseFirestore: FirebaseFirestore
        val mFirestore: FirebaseFirestore
            get() = firebaseFirestore

        private lateinit var sharedPreferences: SharedPreferences
        val mPrefs: SharedPreferences
            get() = sharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPreferences = getSharedPreferences(Constants.appName, Context.MODE_PRIVATE)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseFirestore = FirebaseFirestore.getInstance()
        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            val earthquakeResponseTypeToken = object : TypeToken<GenericResponse<EarthquakeOA>>() {}.type
            val earthquakeResponse = getResponseFromJson<GenericResponse<EarthquakeOA>>("mockservices/orhanaydogduMock.json", earthquakeResponseTypeToken)
            mockEarthquakeList = earthquakeResponse
            Log.d("mockEarthquakeList", earthquakeResponse.toString())
        }

    }

}