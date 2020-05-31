package com.enesky.guvenlikbildir.others

import androidx.core.content.edit
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by Enes Kamil YILMAZ on 07.02.2020
 */

var isFirstTime: Boolean
    get() = App.mPrefs.getBoolean(Constants.isFirstTime, true)
    set(value) = App.mPrefs.edit { putBoolean(Constants.isFirstTime, value) }

var lastKnownLocation: String?
    get() = App.mPrefs.getString(Constants.lastKnownLocation, Constants.defaultLastKnownLocation)
    set(value) = App.mPrefs.edit { putString(Constants.lastKnownLocation,  value) }

var safeSms: String?
    get() = App.mPrefs.getString(Constants.safeSms, App.mInstance.getString(R.string.label_default_safe_sms))
    set(value) = App.mPrefs.edit { putString(Constants.safeSms, value) }

var unsafeSms: String?
    get() = App.mPrefs.getString(Constants.unsafeSms, App.mInstance.getString(R.string.label_default_unsafe_sms))
    set(value) = App.mPrefs.edit { putString(Constants.unsafeSms, value) }

var locationMapWithLink: String?
    get() = App.mPrefs.getString(Constants.locationMapLink, App.mInstance.getString(R.string.label_map_link, lastKnownLocation))
    set(value) = App.mPrefs.edit { putString(Constants.locationMapLink, value) }

var lastLoadedEarthquake: Earthquake?
    get() = Gson().fromJson<Earthquake>(App.mPrefs.getString(Constants.lastLoadedEarthquake, "")!!, object : TypeToken<Earthquake>() {}.type)
    set(template) = App.mPrefs.edit { putString(Constants.lastLoadedEarthquake, Gson().toJson(template)) }

var notificationIconResId: Int
    get() = App.mPrefs.getInt(Constants.notificationIconResId, R.drawable.ic_notifications_active)
    set(value) = App.mPrefs.edit { putInt(Constants.notificationIconResId, value) }

var notificationMagLimit: Float
    get() = App.mPrefs.getFloat(Constants.notificationMagLimit, Constants.defaultNotificationMagLimit)
    set(value) = App.mPrefs.edit { putFloat(Constants.notificationMagLimit, value) }

var isNotificationsEnabled: Boolean
    get() = App.mPrefs.getBoolean(Constants.isNotificationsEnabled, true)
    set(value) = App.mPrefs.edit { putBoolean(Constants.isNotificationsEnabled, value) }

var isWorkerStarted: Boolean
    get() = App.mPrefs.getBoolean(Constants.isWorkerStarted, false)
    set(value) = App.mPrefs.edit { putBoolean(Constants.isWorkerStarted, value) }

fun clearAll() {
    App.mPrefs.edit().apply{
        clear()
        apply()
    }
}