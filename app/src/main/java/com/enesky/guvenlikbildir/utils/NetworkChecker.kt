package com.enesky.guvenlikbildir.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

/**
 * Created by Enes Kamil YILMAZ on 29.01.2020
 */

class NetworkChecker() : BroadcastReceiver() {

    var isOnline: Boolean = true
    var networkStatusChangeListener: NetworkStatusChangeListener? = null

    constructor(networkStatusChangeListener: NetworkStatusChangeListener) : this() {
        this.networkStatusChangeListener = networkStatusChangeListener
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (networkStatusChangeListener != null) {
            if (!context.checkInternet() && isOnline) {
                isOnline = false
                networkStatusChangeListener!!.onNetworkStatusChange(isOnline)
            } else if (context.checkInternet() && !isOnline) {
                isOnline = true
                networkStatusChangeListener!!.onNetworkStatusChange(isOnline)
            }
        }
    }

}

interface NetworkStatusChangeListener {
    fun onNetworkStatusChange(isOnline: Boolean)
}