package com.enesky.guvenlikbildir.others

import android.annotation.SuppressLint
import android.util.Log
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 30.05.2020
 */

private const val TAG = "GuvenlikBildir"

class CrashReportingTree : Timber.Tree() {
    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG)
            return

        Log.i(tag, message)

        if (t != null) {
            if (priority == Log.ERROR)
                Log.e(TAG, t.toString())
            else if (priority == Log.WARN)
                Log.w(TAG, t.toString())
        }
    }
}