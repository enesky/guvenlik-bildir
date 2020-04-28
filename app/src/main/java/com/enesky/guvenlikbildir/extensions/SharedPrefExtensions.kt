package com.enesky.guvenlikbildir.extensions

import androidx.core.content.edit
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.others.Constants

/**
 * Created by Enes Kamil YILMAZ on 07.02.2020
 */

var isFirstTime: Boolean
    get() = App.mPrefs.getBoolean(Constants.isFirstTime, true)
    set(value) = App.mPrefs.edit { putBoolean(Constants.isFirstTime, value) }

var lastKnownLocation: String?
    get() = App.mPrefs.getString(Constants.lastKnownLocation, "41.01844,28.9941283")
    set(value) = App.mPrefs.edit { putString(Constants.lastKnownLocation,  value) }

var safeSms: String?
    get() = App.mPrefs.getString(Constants.safeSms, "Güvendeyim.")
    set(value) = App.mPrefs.edit { putString(Constants.safeSms, value) }

var unsafeSms: String?
    get() = App.mPrefs.getString(Constants.unsafeSms, "Güvende hissetmiyorum.\n" + "Lütfen yardımcı olun.")
    set(value) = App.mPrefs.edit { putString(Constants.unsafeSms, value) }

var locationMapWithLink: String?
    get() = App.mPrefs.getString(
        Constants.locationMapLink, "\nBulunduğum Konum: \n" +
                    "https://www.google.com/maps/place/$lastKnownLocation")
    set(value) = App.mPrefs.edit { putString(Constants.locationMapLink, value) }