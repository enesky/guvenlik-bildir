package com.enesky.guvenlikbildir.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 11.02.2020
 */

@Parcelize
@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val number: String
) : Parcelable