package com.enesky.guvenlikbildir.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

@Parcelize
data class EarthquakeOA(
    val mag: Float,
    val lng: Float,
    val lat: Float,
    val lokasyon: String,
    val depth: Float,
    val coordinates: List<Float>,
    val title: String,
    val rev: String?,
    val timestamp: Long,
    val date_stamp: String,
    val date: String,
    val hash: String,
    val hash2: String
) : Parcelable