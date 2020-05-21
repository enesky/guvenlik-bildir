package com.enesky.guvenlikbildir.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

@Parcelize
@Entity
data class SmsReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val isSafeSms: Boolean = true,
    val sendingDate: String = "", //dd/MM/yyyy HH:mm:ss
    val sentSms: String = "",
    val lastKnownLocation: String = "",
    val contactReportList: List<ContactStatus> = listOf()
) : Parcelable

@Parcelize
enum class SmsReportStatus(
    val status: Int
) : Parcelable {
    IN_QUEUE(0),
    FAILED(1),
    SUCCESS(2),
    DELIVERED(3)
}

@Parcelize
data class ContactStatus(
    val contact: Contact,
    var smsReportStatus: SmsReportStatus
) : Parcelable

class StatusConverter {

    private val gson = Gson()

    @TypeConverter
    fun stringToList(data: String?): List<ContactStatus> {
        if (data == null)
            return Collections.emptyList()

        val listType = object : TypeToken<List<ContactStatus>>() {}.type

        return gson.fromJson<List<ContactStatus>>(data, listType)
    }

    @TypeConverter
    fun listToString(someObjects: List<ContactStatus>): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun contactToContactStatus(contact: Contact): ContactStatus {
        return ContactStatus(contact, SmsReportStatus.IN_QUEUE)
    }

    @TypeConverter
    fun toSmsReportStatus(value: Int): SmsReportStatus {
        return when(value) {
            0 -> SmsReportStatus.IN_QUEUE
            1 -> SmsReportStatus.FAILED
            2 -> SmsReportStatus.SUCCESS
            else -> SmsReportStatus.DELIVERED
        }
    }

    @TypeConverter
    fun fromStatus(value: SmsReportStatus) = value.ordinal
}