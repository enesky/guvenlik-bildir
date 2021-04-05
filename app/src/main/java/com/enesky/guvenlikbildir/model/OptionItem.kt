package com.enesky.guvenlikbildir.model

import android.os.Parcelable
import com.enesky.guvenlikbildir.R
import kotlinx.parcelize.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

@Parcelize
data class OptionItem(
    var imageId: Int,
    val optionText: String,
    var colorRes: Int = R.color.colorPrimary
) : Parcelable