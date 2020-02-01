package com.enesky.guvenlikbildir.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.WeekFields
import java.io.IOException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Enes Kamil YILMAZ on 09.01.2020
 */

// View Extensions
fun View.makeItVisible() {
    visibility = View.VISIBLE
}

fun View.makeItInvisible() {
    visibility = View.INVISIBLE
}

fun View.makeItGone() {
    visibility = View.GONE
}

fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

fun View.setBackground(@ColorRes color: Int) = setBackgroundColor(context.getColorCompat(color))

fun View.setBackgroundTint(@ColorRes color: Int) {
    backgroundTintList = ContextCompat.getColorStateList(context, context.getColorCompat(color))
}

// Context Extensions
fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.checkInternet(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun Context.showToast(message: String?) {
    val layout = LayoutInflater.from(this).inflate(R.layout.default_toast, null, false)
    val textView = layout.findViewById<TextView>(R.id.toast_tv).apply {
        gravity = Gravity.CENTER
        text = message
    }
    val toast = Toast(this).apply {
        duration = Toast.LENGTH_LONG
        view = layout
        setGravity(Gravity.BOTTOM, 0, 200)
        show()
    }
}

fun View.showSnackbar(text: String) {
    Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).apply {
        duration = 5000
        setAction("Tamam") { dismiss() }
        setBackgroundTint(resources.getColor(R.color.colorPrimaryDark))
        setActionTextColor(resources.getColor(android.R.color.white))
        view.findViewById<View>(R.id.snackbar_action).background = null
        view.setPadding(0,0,0,0)
    }.show()
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showKeyboard() {
    (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
        InputMethodManager.SHOW_FORCED,
        InputMethodManager.HIDE_IMPLICIT_ONLY
    )
}

// Activity-Fragment Extensions
fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

// Extensions
fun getScreenHeight(): Int = Resources.getSystem().displayMetrics.heightPixels

fun String.isPhoneNumberValid(): Boolean {
    return Patterns.PHONE.matcher(this).matches()
}