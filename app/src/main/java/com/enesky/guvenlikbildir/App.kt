package com.enesky.guvenlikbildir

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.work.*
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.worker.NotifierWorker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Enes Kamil YILMAZ on 26.01.2020
 */

class App : Application() {

    companion object {
        private lateinit var instance: App
        val mInstance: App
            get() = instance

        private lateinit var firebaseAuth: FirebaseAuth
        val mAuth: FirebaseAuth
            get() = firebaseAuth

        private lateinit var firebaseFirestore: FirebaseFirestore
        val mFirestore: FirebaseFirestore
            get() = firebaseFirestore

        private lateinit var firebaseAnalytics: FirebaseAnalytics
        val mAnalytics: FirebaseAnalytics
            get() = firebaseAnalytics

        private lateinit var firebaseCrashlytics: FirebaseCrashlytics
        val mCrashlytics: FirebaseCrashlytics
            get() = firebaseCrashlytics

        private lateinit var sharedPreferences: SharedPreferences
        val mPrefs: SharedPreferences
            get() = sharedPreferences

        private lateinit var workManager: WorkManager

        fun stopWorker() {
            workManager.cancelAllWork()
            Timber.tag("NotifierWorker").d("Worker stopped.")
        }

        fun startWorker() {
            Timber.tag("NotifierWorker").d("Worker started.")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val work = PeriodicWorkRequestBuilder<NotifierWorker>(
                Constants.workerRepeat, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "REFRESH_EARTHQUAKES",
                ExistingPeriodicWorkPolicy.REPLACE,
                work
            )

        }

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPreferences = getSharedPreferences(Constants.appName, Context.MODE_PRIVATE)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseCrashlytics = FirebaseCrashlytics.getInstance()
        //firebaseAuth = FirebaseAuth.getInstance()
        //firebaseFirestore = FirebaseFirestore.getInstance()
        workManager = WorkManager.getInstance(this)
        AndroidThreeTen.init(this)

        if(BuildConfig.DEBUG)
            Timber.plant(DebugTree())

        createNotificationChannel()
        Locale.setDefault(Locale("tr"))
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
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

}