package com.enesky.guvenlikbildir.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

@Parcelize
data class GenericResponse(
    val status: Boolean,
    val desc: String?,
    val result: List<EarthquakeOA>
) : Parcelable