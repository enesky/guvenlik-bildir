package com.enesky.guvenlikbildir.network

import com.enesky.guvenlikbildir.model.EarthquakeOA
import com.enesky.guvenlikbildir.model.GenericResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

interface WebService {

    @GET("index.php")
    suspend fun getLastEarthquakesWithDate(
        @Query("date") date: String,
        @Query("limit") limit: String): Response<GenericResponse<EarthquakeOA>>

    @GET("live.php")
    suspend fun getLastEarthquakes(
        @Query("limit") limit: String): Response<GenericResponse<EarthquakeOA>>

}