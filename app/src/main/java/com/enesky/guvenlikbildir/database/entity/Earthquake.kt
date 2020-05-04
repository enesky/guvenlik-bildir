package com.enesky.guvenlikbildir.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 23.04.2020
 * Earthquake is a data model made according to http://www.koeri.boun.edu.tr/scripts/lst0.asp
 */

@Parcelize
@Entity
data class Earthquake(
	@PrimaryKey(autoGenerate = true) val id: Int,
	val date: String, // YYYY.MM.dd
	val time: String, // HH:mm:ss
	val dateTime: String, // date + time
	val lat: String,
	val lng: String,
	val depth: String,
	val magMD: String, //duration magnitude (don't use it)
	val magML: Double, //local (richter) magnitude
	val magMW: String, //moment magnitude (if it exists use this one)
	val locationOuter: String,
	val locationInner: String,
	val location: String, // locationInner + locationOuter
	val quality: String //don't use it
) : Parcelable