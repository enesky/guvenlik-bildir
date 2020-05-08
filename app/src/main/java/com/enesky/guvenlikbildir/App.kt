package com.enesky.guvenlikbildir

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import com.enesky.guvenlikbildir.model.EarthquakeOA
import com.enesky.guvenlikbildir.model.GenericResponse
import com.enesky.guvenlikbildir.others.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*

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
            get() = firebaseAnalytics

        private lateinit var firebaseCrashlytics: FirebaseCrashlytics
        val mCrashlytics: FirebaseCrashlytics
            get() = firebaseCrashlytics

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
        firebaseCrashlytics = FirebaseCrashlytics.getInstance()
        AndroidThreeTen.init(this)

        if(BuildConfig.DEBUG)
            Timber.plant(DebugTree())

        createNotificationChannel()
        Locale.setDefault(Locale("tr"))

        /*
        if (BuildConfig.DEBUG) {
            val earthquakeResponseTypeToken = object : TypeToken<GenericResponse<EarthquakeOA>>() {}.type
            val earthquakeResponse = getResponseFromJson<GenericResponse<EarthquakeOA>>("mockservices/orhanaydogduMock.json", earthquakeResponseTypeToken)
            mockEarthquakeList = earthquakeResponse
            //Log.d("mockEarthquakeList", earthquakeResponse.toString())
        }
        */
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Constants.appName, Constants.notificationChannel, importance)
            channel.enableVibration(true)
            channel.enableLights(true)
            channel.setShowBadge(true)
            channel.lightColor = Color.GRAY
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            notificationManager.createNotificationChannel(channel)
        }
    }

}