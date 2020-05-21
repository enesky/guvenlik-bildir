package com.enesky.guvenlikbildir.adapter

import android.annotation.SuppressLint
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.custom.StatefulRecyclerView
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Enes Kamil YILMAZ on 08.11.2019
 */

@BindingAdapter("isOnlineBackground")
fun isOnlineBackground(view: View, isOnline: Boolean) {
    if (isOnline)
        view.setBackground(R.color.colorPrimary)
    else
        view.setBackground(R.color.colorSecondary)
}

@BindingAdapter("isSelected")
fun isSelected(view: View, isSelected: Boolean) {
    if (isSelected)
        view.setBackground(R.color.fern)
    else
        view.setBackground(android.R.color.white)
}

@BindingAdapter("setAdapter")
fun bindStatefulRecyclerViewAdapter(view: StatefulRecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.setHasFixedSize(true)
    view.layoutAnimation = AnimationUtils.loadLayoutAnimation(view.context, R.anim.layout_animation)
    view.adapter = adapter
}

@BindingAdapter("setAdapterWithAnim")
fun bindRecyclerViewAdapterWithAnim(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.layoutManager = LinearLayoutManager(view.context)
    view.layoutAnimation = AnimationUtils.loadLayoutAnimation(view.context, R.anim.layout_animation)
    view.adapter = adapter
}

@BindingAdapter("setAdapter")
fun bindRecyclerViewAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
    view.apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(view.context)
        setItemViewCacheSize(15)
        isDrawingCacheEnabled = true
        drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
    }

    if (adapter != null) {
        if (!adapter.hasObservers())
            adapter.setHasStableIds(true)
        view.adapter = adapter
    }
}

@BindingAdapter("setPagerAdapter")
fun bindRecyclerViewAdapter(view: ViewPager, adapter: FragmentStatePagerAdapter) {
    view.adapter = adapter
}

@BindingAdapter("textChangedListener")
fun bindTextWatcher(editText: EditText, textWatcher: TextWatcher) {
    editText.addTextChangedListener(textWatcher)
}

@BindingAdapter("isVisible")
fun bindIsVisible(view: View, isVisible: Boolean) {
    if (isVisible)
        view.makeItVisible()
    else
        view.makeItGone()
}

@BindingAdapter("hideWhenListEmpty")
fun hideWhenListEmpty(view: View, list: List<Any>?) {
    if (list.isNullOrEmpty())
        view.makeItGone()
    else
        view.makeItVisible()
}

@BindingAdapter("showWhenListEmpty")
fun showWhenListEmpty(view: View, list: List<Any>?) {
    if (list.isNullOrEmpty())
        view.makeItVisible()
    else
        view.makeItGone()
}

@SuppressLint("SimpleDateFormat")
@BindingAdapter("shortenedDateText")
fun bindShortenedDate(view: TextView, formattedDate: String) {
    val p = PrettyTime()
    p.locale = Locale("tr")
    val simpleDateFormat = SimpleDateFormat(Constants.EARTHQUAKE_LONG_DATE_FORMAT)
    val date = simpleDateFormat.parse(formattedDate)
    view.text = p.format(date)
}

@SuppressLint("SimpleDateFormat")
@BindingAdapter("shortDateText", "shortTimeText")
fun bindShortenedDateTime(view: TextView, dateText: String?, timeText:String?) {
    if (dateText == null || timeText == null) {
        view.text = "-"
    } else {
        val date = SimpleDateFormat(Constants.EARTHQUAKE_LONG_DATE_FORMAT).parse("$dateText, $timeText")
        val p = PrettyTime()
        p.locale = Locale("tr")
        view.text = p.format(date)
    }
}

@SuppressLint("SimpleDateFormat")
@BindingAdapter("formattedDateText")
fun bindFormattedDate(view: TextView, formattedDate: String) {
    val date = SimpleDateFormat(Constants.EARTHQUAKE_LONG_DATE_FORMAT).parse(formattedDate)
    view.text = SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(date!!)
}

@SuppressLint("SimpleDateFormat", "SetTextI18n")
@BindingAdapter("stringKey", "dateText", "timeText")
fun bindFormattedDate(view: TextView, stringKey: String, dateText: String?, timeText:String?) {
    var formattedDateTimeText = ""
    if (dateText == null || timeText == null) {
        view.text = ""
    } else {
        val date = SimpleDateFormat(Constants.EARTHQUAKE_LONG_DATE_FORMAT).parse("$dateText, $timeText")
        formattedDateTimeText = SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(date!!)
        view.text = "$stringKey $formattedDateTimeText"
    }
}

@BindingAdapter("resId", "param")
fun setTextWithParam(view: TextView, @StringRes resId: Int, param: Any) {
    view.text = getString(resId, param)
}

@BindingAdapter("text")
fun setText(view: TextView, text: Number) {
    view.text = text.toString()
}

@BindingAdapter("onBackPressed")
fun onBackPressed(view: View, fragment: Fragment) {
    view.setOnClickListener { fragment.activity!!.onBackPressed() }
}

@BindingAdapter("onBackPressed")
fun dismiss(view: EditText, dismiss: Boolean) {
    view.setOnClickListener {
        (view.context as Fragment).requireActivity().onBackPressed()
    }
}

@BindingAdapter("setImage")
fun setImage(view: ImageView, imageId: Int) {
    view.setImageResource(imageId)
}

@SuppressLint("ResourceAsColor")
@BindingAdapter("setColor")
fun setColor(view: TextView, isColorWhite: Boolean) {
    if (isColorWhite)
        view.setTextColor(android.R.color.white)
    else
        view.setTextColor(R.color.colorPrimary)
}

@SuppressLint("SetTextI18n")
@BindingAdapter("stringKey", "numberValue", "stringValue","secondStringValue", "unitName", requireAll=false)
fun putValues2String(view: TextView, stringKey: String, numberValue: Number?,
                     stringValue: String?,secondStringValue: String?, unitName: String?) {
    var text = ""
    if (numberValue != null)
        text = "$stringKey $numberValue"
    else if (stringValue != null) {
        text = "$stringKey $stringValue"
        if (secondStringValue != null)
            text += " ($secondStringValue)"
    }

    if (unitName != null)
        view.text = "$text $unitName"
    else
        view.text = text

}

@BindingAdapter("filterIndex", "selectedIndex", requireAll=true)
fun changeFilters(view: TextView, filterIndex: Int, selectedIndex: Int) {

    when (selectedIndex) {
        0 -> { // All
            view.background = view.context.getDrawable(R.drawable.bg_left_active)
            view.setTextColorRes(R.color.colorPrimary)
        }
        1 -> { // 0 - 3
            view.setBackground(android.R.color.white)
            view.setTextColorRes(R.color.apple)
        }
        2 -> { // 3 - 4.5
            view.setBackground(android.R.color.white)
            view.setTextColorRes(R.color.colorSecondary)
        }
        3 -> { // > 4.5
            view.background = view.context.getDrawable(R.drawable.bg_right_active)
            view.setTextColorRes(R.color.cinnabar)
        }
    }

    when (filterIndex) {
        0 -> { // All
            if (selectedIndex != filterIndex) {
                view.background = view.context.getDrawable(R.drawable.bg_left_passive)
                view.setTextColorRes(android.R.color.white)
            }
        }
        1, 2 -> { // 0-3, 3-4.5
            if (selectedIndex != filterIndex) {
                view.setBackground(R.color.colorPrimary)
                view.setTextColorRes(android.R.color.white)
            }
        }
        3 -> { // >4.5
            if (selectedIndex != filterIndex) {
                view.background = view.context.getDrawable(R.drawable.bg_right_passive)
                view.setTextColorRes(android.R.color.white)
            }
        }
    }

}

@BindingAdapter("status")
fun setStatus(view: View, status: SmsReportStatus) {
    if (view is ImageView) {
        view.makeItVisible()
        when (status) {
            SmsReportStatus.SUCCESS -> view.setImageResource(R.drawable.ic_success)
            SmsReportStatus.DELIVERED -> view.setImageResource(R.drawable.ic_delivered)
            SmsReportStatus.FAILED -> view.setImageResource(R.drawable.ic_failed)
            else -> view.makeItGone()
        }
    } else {
        if (status == SmsReportStatus.IN_QUEUE)
            view.makeItVisible()
        else
            view.makeItGone()
    }
}

