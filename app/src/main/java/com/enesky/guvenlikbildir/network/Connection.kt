package com.enesky.guvenlikbildir.network

import com.enesky.guvenlikbildir.BuildConfig
import com.enesky.guvenlikbildir.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

val webService: WebService by lazy {
    Retrofit.Builder()
        .baseUrl(Constants.depremUrl)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .client(createHttpClient())
        .build()
        .create(WebService::class.java)
}

fun createHttpClient(): OkHttpClient {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val httpBuilder = OkHttpClient().newBuilder().apply {
        readTimeout(Constants.READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
        connectTimeout(Constants.CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        if (BuildConfig.LOG_ENABLED)
            addInterceptor(logging)
    }
    return httpBuilder.build()
}

class Connection {
    suspend fun getLastEarthquakes(
        limit: String
    ) = webService.getLastEarthquakes(limit)

    suspend fun getLastEarthquakesWithDate(
        date: String,
        limit: String
    ) = webService.getLastEarthquakesWithDate(date, limit)
}