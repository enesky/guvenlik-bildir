package com.enesky.guvenlikbildir.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Enes Kamil YILMAZ on 23.04.2020
 * Earthquake is a data model made according to http://www.koeri.boun.edu.tr/scripts/lst0.asp
 */

@Parcelize
data class Earthquake(
	val date: String, // YYYY.MM.dd
	val time: String, // HH:mm:ss
	val lat: String,
	val lng: String,
	val depth: String,
	val magMD: String, //duration magnitude (don't use it)
	val magML: String, //local (richter) magnitude
	val magMW: String, //moment magnitude (if it exists use this one)
	val locationOuter: String,
	val locationInner: String,
	val quality: String //don't use it
) : Parcelable
