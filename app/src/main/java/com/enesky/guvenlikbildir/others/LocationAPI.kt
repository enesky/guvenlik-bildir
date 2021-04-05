package com.enesky.guvenlikbildir.others

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 18.05.2020
 */

class LocationAPI (
    private val activity: AppCompatActivity
) {

    companion object {
        lateinit var instance: LocationAPI
            private set
    }

    init {
        instance = this
    }

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    fun startLocationUpdates() {
        if (checkPlayServiceAvailability())
            requestLocationUpdatesWithGoogleApi()
        else
            requestLocationUpdatesWithoutGoogleApi()
    }

    fun stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            try {
                val voidTask = fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
                if (voidTask.isSuccessful)
                    Timber.tag("LocationAPI").d("stopLocationUpdates() -> successful")
                else
                    Timber.tag("LocationAPI").d("stopLocationUpdates() -> failed")
            } catch (exp: SecurityException) {
                Timber.tag("LocationAPI").d("Security exception while stopLocationUpdates")
            }
        } else {
            locationManager = null
            locationListener = null
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdatesWithGoogleApi() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)

        locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = Constants.MIN_TIME_BW_LOCATION_UPDATE
            fastestInterval = Constants.MIN_TIME_BW_LOCATION_UPDATE
            smallestDisplacement = Constants.MIN_DISTANCE_BW_LOCATION_UPDATE
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                val mLastKnownLocation = locationResult.lastLocation
                if (mLastKnownLocation != null) {
                    lastKnownLocation = "${mLastKnownLocation.latitude},${mLastKnownLocation.longitude}"
                    Timber.tag("LocationAPI").d("LastKnownLocation: %s", lastKnownLocation)
                }
            }
        }

        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdatesWithoutGoogleApi() {
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = LocationListener { location ->
            lastKnownLocation = "${location.latitude},${location.longitude}"
            Timber.tag("LocationAPI").d("LastKnownLocation: %s", lastKnownLocation)
        }

        val gpsProvider = LocationManager.GPS_PROVIDER
        val networkProvider = LocationManager.NETWORK_PROVIDER
        val finalProvider: String?

        val isNetworkEnabled = locationManager!!.isProviderEnabled(networkProvider)

        val gpsLocation: Location? = locationManager!!.getLastKnownLocation(gpsProvider)
        val networkLocation: Location? = locationManager!!.getLastKnownLocation(networkProvider)

        finalProvider = when {
            isNetworkEnabled -> networkProvider
            else -> gpsProvider
        }

        val lastKnownLoc: Location? = if (networkLocation != null && gpsLocation != null) {
            if (gpsLocation.accuracy > networkLocation.accuracy) //ne kadar küçükse o kadar accurate
                networkLocation
            else
                gpsLocation
        } else {
            networkLocation ?: gpsLocation
        }

        if (lastKnownLoc != null)
            lastKnownLocation = "${lastKnownLoc.latitude},${lastKnownLoc.longitude}"

        locationManager!!.requestLocationUpdates(
            finalProvider,
            Constants.MIN_TIME_BW_LOCATION_UPDATE,
            Constants.MIN_DISTANCE_BW_LOCATION_UPDATE,
            locationListener!!
        )
    }

    private fun checkPlayServiceAvailability(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(activity)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode))
                apiAvailability.getErrorDialog(activity, resultCode, 9000).show()
            return false
        }

        return true
    }

}