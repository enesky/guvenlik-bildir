package com.enesky.guvenlikbildir.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 11.02.2020
 */

@Parcelize
data class Contact(
    val name: String,
    val number: String
) : Parcelable