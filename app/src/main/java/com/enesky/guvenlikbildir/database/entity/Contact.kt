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
    val number: String,
    var isSelected: Boolean = false
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        return (other is Contact) &&
                name == other.name &&
                number == other.number
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + id
        result = 31 * result + number.hashCode()
        return result
    }

}