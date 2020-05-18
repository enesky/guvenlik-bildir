package com.enesky.guvenlikbildir.extensions

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Patterns
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.others.lastKnownLocation
import com.enesky.guvenlikbildir.ui.activity.login.LoginActivity
import com.enesky.guvenlikbildir.ui.activity.login.verify.VerifyCodeActivity
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import com.thefinestartist.finestwebview.FinestWebView
import timber.log.Timber
import java.io.IOException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

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

fun getString(@StringRes resId: Int) = App.mInstance.getString(resId)

fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

fun View.setBackground(@ColorRes color: Int) = setBackgroundColor(context.getColorCompat(color))

fun View.getBackgorund() = (background as ColorDrawable).color

fun View.setBackgroundTint(@ColorRes color: Int) {
    backgroundTintList = ContextCompat.getColorStateList(context, color)
}

private var onMove = false

fun View.setTouchAnimation(function: (() -> Unit)?) {
    this.setOnTouchListener { p0, p1 ->
        when (p1?.action) {
            MotionEvent.ACTION_DOWN -> { startScaleAnimation(p0) }
            MotionEvent.ACTION_MOVE -> {
                cancelScaleAnimation(p0)
                onMove = true
            }
            MotionEvent.ACTION_UP -> {
                cancelScaleAnimation(p0)
                if (!onMove)
                    Handler().postDelayed({ function?.invoke() }, 150)
                onMove = false
            }
        }
        true
    }
}

private fun startScaleAnimation(view: View) {
    val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.75f)
    val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.75f)
    scaleDownX.duration = 150
    scaleDownY.duration = 150
    scaleDownX.start()
    scaleDownY.start()
}

private fun cancelScaleAnimation(view: View) {
    val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
    val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
    scaleDownX.duration = 150
    scaleDownY.duration = 150
    scaleDownX.start()
    scaleDownY.start()
}

// Context Extensions
fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.checkInternet(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun Context.showToast(message: String?, isDurationLong: Boolean = true) {
    val layout = LayoutInflater.from(this).inflate(R.layout.default_toast, null, false)
    val textView = layout.findViewById<TextView>(R.id.toast_tv).apply {
        gravity = Gravity.CENTER
        text = message
    }

    val mDuration = if (isDurationLong)
        Toast.LENGTH_LONG
    else
        Toast.LENGTH_SHORT

    val toast = Toast(this).apply {
        duration = mDuration
        view = layout
        setGravity(Gravity.BOTTOM, 0, 200)
        show()
    }
}

fun View.showSnackbar(text: String) {
    Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).apply {
        duration = 5000
        setAction("Tamam") { dismiss() }
        setBackgroundTint(view.context.getColorCompat(R.color.colorPrimaryDark))
        setActionTextColor(view.context.getColorCompat(android.R.color.white))
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

fun Context.hasNetwork(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null
}

// Activity-Fragment Extensions
fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Activity.openMainActivity() {
    startActivity(Intent(this, MainActivity::class.java))
    finishAffinity()
}

fun Activity.openLoginActivity() {
    startActivity(Intent(this, LoginActivity::class.java))
    finishAffinity()
}

fun Activity.openGooglePlayPage() {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(Constants.googlePlayUrl)
        setPackage("com.android.vending")
    }
    startActivity(intent)
}

fun Activity.shareGooglePlayPage() {
    val sendIntent: Intent = Intent().setAction(Intent.ACTION_SEND)
    sendIntent.putExtra(Intent.EXTRA_TEXT, " # Güvenlik Bildir #\n" + Constants.googlePlayUrl)
    sendIntent.type = "text/plain"
    startActivity(sendIntent)
}

fun Fragment.openGoogleMaps(latlng: String, title: String) {
    val query = "$latlng($title)"
    val encodedQuery = Uri.encode(query)
    val gmmIntentUri: Uri = Uri.parse("geo:$latlng?q=$encodedQuery&z=5")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    startActivity(mapIntent)
}

fun Activity.openVerifyCodeActivity(phoneNumber: String,
                                    verificationId: String,
                                    token: PhoneAuthProvider.ForceResendingToken) {
    val intent = Intent(this, VerifyCodeActivity::class.java)
    intent.putExtra("phoneNumber", phoneNumber)
    intent.putExtra("verificationId", verificationId)
    intent.putExtra("token", token)
    startActivity(intent)
}

fun Activity.openWebView(url: String) {
    FinestWebView.Builder(this).show(url)
}

fun Fragment.openLastKnownLocation() {
    val query = "$lastKnownLocation(Bilinen Son Konum)"
    val encodedQuery = Uri.encode(query)
    val gmmIntentUri: Uri = Uri.parse("geo:$lastKnownLocation?q=$encodedQuery&z=3")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    startActivity(mapIntent)
}


// Extensions
fun getScreenHeight(): Int = Resources.getSystem().displayMetrics.heightPixels

fun String.isPhoneNumberValid(): Boolean {
    return Patterns.PHONE.matcher(this).matches()
}

fun String.formatDateTime(): String {
    val date = SimpleDateFormat(Constants.DEFAULT_K_DATE_TIME_FORMAT).parse(this)
    return SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(date!!)
}


inline fun <reified T> Context.getResponseFromJson(
    fileName: String,
    typeToken: Type
): T {
    var json: String? = null
    try {
        val inputStream = this.assets.open(fileName)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        json = String(buffer, Charsets.UTF_8)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        return Gson().fromJson<T>(json, typeToken)
    }
}

//sendSMS("+905383115141", listOf( "+905383115141"), "hiiii")
fun Fragment.sendSMS(smsTo: String? = null,
                     contactList: List<String>? = null,
                     message: String){
    var uri : Uri? = null

    if(!smsTo.isNullOrEmpty())
        uri = Uri.parse("smsto:$smsTo")
    else if (!contactList.isNullOrEmpty()) {
        val manufacturer = Build.MANUFACTURER

        var numberString = "smsto:"
        for ((index, value) in contactList.withIndex()) {

            numberString = if (manufacturer == "samsung") {
                if (index < (contactList.size - 1))
                    numberString.plus(value).plus(",")
                else
                    numberString.plus(value)
            } else {
                if (index < (contactList.size - 1))
                    numberString.plus(value).plus(";")
                else
                    numberString.plus(value)
            }

        }

        uri = Uri.parse(numberString)
    }

    val sendSmsIntent = Intent().apply {
        action = Intent.ACTION_SENDTO
        data = uri
        putExtra("sms_body", message)
    }

    startActivity(sendSmsIntent)
}

fun Fragment.openBrowser(@StringRes resId: Int): Unit =
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(resId))))

fun Fragment.sendFeedback() {
    val testIntent = Intent(Intent.ACTION_VIEW)
    val data = Uri.parse("mailto:?subject=" + getString(R.string.label_mail_header) +
                    "&body=" + getString(R.string.label_mail_body) + "&to=" + getString(R.string.email))
    testIntent.data = data
    startActivity(testIntent)
}

inline val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

inline val Context.screenWidth: Int
    get() = Point().also { (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(it) }.x

inline fun getValueAnimator(
    forward: Boolean = true,
    duration: Long,
    interpolator: TimeInterpolator,
    crossinline updateListener: (progress: Float) -> Unit
): ValueAnimator {
    val a =
        if (forward) ValueAnimator.ofFloat(0f, 1f)
        else ValueAnimator.ofFloat(1f, 0f)
    a.addUpdateListener { updateListener(it.animatedValue as Float) }
    a.duration = duration
    a.interpolator = interpolator
    return a
}

fun calculateExecutionTime(function: () -> Any) {
    val startTime = System.nanoTime()

    function()

    val endTime = System.nanoTime()
    val timeNs = endTime - startTime
    val timeMs: Long = TimeUnit.NANOSECONDS.toMillis(timeNs)
    val timeSec: Long = TimeUnit.NANOSECONDS.toSeconds(timeNs)
    val timeMin: Long = TimeUnit.NANOSECONDS.toMinutes(timeNs)
    val timeHour: Long = TimeUnit.NANOSECONDS.toHours(timeNs)

    var executionTime = "Execution Time: "
    if (timeHour > 0) executionTime += "$timeHour Hours, "
    if (timeMin > 0) executionTime += (timeMin % 60).toString() + " Minutes, "
    if (timeSec > 0) executionTime += (timeSec % 60).toString() + " Seconds, "
    if (timeMs > 0) executionTime += (timeMs % 1E+3).toString() + " MicroSeconds, "
    if (timeNs > 0) executionTime += (timeNs % 1E+6).toString() + " NanoSeconds"

    Timber.tag(" -- Execution Time -- ").i(executionTime)
}
