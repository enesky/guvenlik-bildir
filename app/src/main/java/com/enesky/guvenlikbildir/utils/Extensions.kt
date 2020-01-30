package com.enesky.guvenlikbildir.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

/**
 * Created by Enes Kamil YILMAZ on 09.01.2020
 */

fun View.makeItVisible() {
    visibility = View.VISIBLE
}

fun View.makeItInvisible() {
    visibility = View.INVISIBLE
}

fun View.makeItGone() {
    visibility = View.GONE
}

internal fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

internal fun View.setBackground(@ColorRes color: Int) = setBackgroundColor(context.getColorCompat(color))

internal fun View.setBackgroundTint(@ColorRes color: Int) {
    backgroundTintList = ContextCompat.getColorStateList(context, context.getColorCompat(color))
}

internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun Context.showToast(text: String?) {
    val inflater = LayoutInflater.from(this)
    val layout: View = inflater.inflate(R.layout.default_toast, null, false)
    val textView: TextView = layout.findViewById(R.id.toast_tv)
    textView.gravity = Gravity.CENTER
    textView.text = text
    val toast = Toast(this)
    toast.duration = Toast.LENGTH_LONG
    toast.view = layout
    toast.setGravity(Gravity.BOTTOM, 0, 200)
    toast.show()
}

fun Context.checkInternet(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
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

internal fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showKeyboard(context: Context) {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
        InputMethodManager.SHOW_FORCED,
        InputMethodManager.HIDE_IMPLICIT_ONLY
    )
}