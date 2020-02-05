package com.enesky.guvenlikbildir

import android.app.Application
import android.util.Log
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
        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            val earthquakeResponseTypeToken = object : TypeToken<GenericResponse<EarthquakeOA>>() {}.type
            val earthquakeResponse = getResponseFromJson<GenericResponse<EarthquakeOA>>("mockservices/orhanaydogduMock.json", earthquakeResponseTypeToken)
            mockEarthquakeList = earthquakeResponse
            Log.d("mockEarthquakeList", earthquakeResponse.toString())
        }

    }

}