package com.enesky.guvenlikbildir.extensions

object Constants {

    const val depremUrl = "https://api.orhanaydogdu.com.tr/deprem/"
    const val READ_TIMEOUT = 120
    const val CONNECT_TIMEOUT = 120

    //Notification Topic
    const val LANGUAGE_TURKISH = "Türkçe"
    const val LANG_TURKISH_CODE = "tr"

    //Formats
    const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss"
    const val EARTHQUAKE_SHORT_DATE_FORMAT = "yyyy-MM-dd"
    const val EARTHQUAKE_LONG_DATE_FORMAT = "yyyy.MM.dd HH:mm:ss"

    //FirebaseAuthentication
    const val testUserPhoneNumber = "+90 (555) 555 55 55"
    const val testUserVerifyCode = "123456"

    //Firestore
    const val usersCollection = "users"
    const val usersCollectionUid = "uid"
    const val usersCollectionPhoneNumber = "phoneNumber"

    //Numbers
    const val polis = "155"
    const val acilYardım = "112"
    const val itfaiye = "110"

    const val map = "map"

}