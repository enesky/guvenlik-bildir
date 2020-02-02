package com.enesky.guvenlikbildir.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

@Parcelize
data class OptionItem(
    val imageId: Int,
    val optionText: String
) : Parcelable