package com.enesky.guvenlikbildir.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

@Parcelize
data class GenericResponse<T>(
    val status: Boolean,
    val desc: String?,
    val result: @RawValue List<T>
) : Parcelable