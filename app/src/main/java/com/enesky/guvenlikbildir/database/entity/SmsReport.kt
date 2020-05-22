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
    STAND_BY(0),
    IN_QUEUE(1),
    FAILED(2),
    SUCCESS(3),
    DELIVERED(4)
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

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun listToString(someObjects: List<ContactStatus>): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun contactToContactStatus(contact: Contact): ContactStatus {
        return ContactStatus(contact, SmsReportStatus.STAND_BY)
    }

    @TypeConverter
    fun toSmsReportStatus(value: Int): SmsReportStatus {
        return when(value) {
            1 -> SmsReportStatus.IN_QUEUE
            2 -> SmsReportStatus.FAILED
            3 -> SmsReportStatus.SUCCESS
            4 -> SmsReportStatus.DELIVERED
            else -> SmsReportStatus.STAND_BY
        }
    }

    @TypeConverter
    fun fromStatus(value: SmsReportStatus) = value.ordinal
}