package com.enesky.guvenlikbildir.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 28.02.2020
 */

@Parcelize
data class User(
    val uid: String? = null,
    val phoneNumber: String? = null,
    val contactList: MutableList<Contact> = mutableListOf()
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (uid != other.uid) return false
        if (phoneNumber != other.phoneNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid?.hashCode() ?: 0
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        return result
    }
}