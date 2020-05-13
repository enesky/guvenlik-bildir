package com.enesky.guvenlikbildir.others

object Constants {

    const val odDepremUrl = "https://api.orhanaydogdu.com.tr/deprem/"
    const val kandilliBaseUrl = "http://www.koeri.boun.edu.tr/scripts/"
    const val kandilliUrl = "http://www.koeri.boun.edu.tr/scripts/lst0.asp"

    const val googlePlayUrl = "https://play.google.com/store/apps/details?id=com.enesky.guvenlikbildir"
    const val githubUrl = "https://github.com/EnesKy/guvenlik-bildir"

    const val READ_TIMEOUT = 120
    const val CONNECT_TIMEOUT = 120

    const val EARTHQUAKE_LIST_SIZE = 500

    //Notification Topic
    const val LANGUAGE_TURKISH = "Türkçe"
    const val LANG_TURKISH_CODE = "tr"

    //Formats - Orhan Aydoğdu
    const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss"
    const val EARTHQUAKE_LONG_DATE_FORMAT = "yyyy.MM.dd, HH:mm:ss"

    //Formats - Kandilli
    const val DEFAULT_K_DATE_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss"
    const val EARTHQUAKE_K_TIME_FORMAT = "HH:mm:ss"
    const val EARTHQUAKE_K_LONG_DATE_FORMAT = "HH:mm:ss dd/MM/yyyy"

    //FirebaseAuthentication
    const val testUserPhoneNumber = "+90 (555) 555 55 55"
    const val testUserVerifyCode = "123456"

    //Firestore
    const val usersCollection = "users"
    const val usersCollectionUid = "uid"
    const val usersCollectionPhoneNumber = "phoneNumber"
    const val usersContactList = "contactList"

    //SharedPref
    const val appName = "Güvenlik Bildir"

    //Numbers
    const val polis = "155"
    const val acilYardım = "112"
    const val itfaiye = "110"

    const val defaultAnimationDuration = 250L
    const val map = "map"

    //SharedPref Keys
    const val isFirstTime = "isFirstTime"
    const val lastKnownLocation = "lastKnownLocation"
    const val locationMapLink = "locationMapLink"
    const val safeSms = "safeSms"
    const val unsafeSms = "unsafeSms"
    const val lastLoadedEarthquake = "lastLoadedEarthquake"
    const val notificationIconResId = "notificationIconResId"
    const val notificationMagLimit = "notificationMagLimit"
    const val isNotificationsEnabled = "isNotificationsEnabled"

    //Notifications
    const val notificationChannel = "Deprem Bildirimleri"
    const val NOTIFICATION_EARTHQUAKE = "earthquake"
    const val workerRepeat = 15L
    const val wrokerFlex = 5L

}