package com.enesky.guvenlikbildir.adapter

import android.annotation.SuppressLint
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.custom.StatefulRecyclerView
import com.enesky.guvenlikbildir.extensions.Constants
import com.enesky.guvenlikbildir.extensions.makeItGone
import com.enesky.guvenlikbildir.extensions.makeItVisible
import com.enesky.guvenlikbildir.extensions.setBackground
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
        view.setBackground(R.color.green56)
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
    view.setHasFixedSize(true)
    view.layoutManager = LinearLayoutManager(view.context)
    view.layoutAnimation = AnimationUtils.loadLayoutAnimation(view.context, R.anim.layout_animation)
    view.adapter = adapter
}

@BindingAdapter("setAdapter")
fun bindRecyclerViewAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    adapter.setHasStableIds(true)
    view.adapter = adapter

    view.apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(view.context)
        setItemViewCacheSize(10)
        isDrawingCacheEnabled = true
        drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
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
fun hideWhenListEmpty(view: View, list: List<Any>) {
    if (list.isNullOrEmpty())
        view.makeItGone()
    else
        view.makeItVisible()
}

@BindingAdapter("showWhenListEmpty")
fun showWhenListEmpty(view: View, list: List<Any>) {
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
@BindingAdapter("formattedDateText")
fun bindFormattedDate(view: TextView, formattedDate: String) {
    val date = SimpleDateFormat(Constants.EARTHQUAKE_LONG_DATE_FORMAT).parse(formattedDate)
    view.text = SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(date!!)
}

@BindingAdapter("text")
fun setText(view: TextView, text: Number) {
    view.text = text.toString()
}

@BindingAdapter("textWithOneWordEachLine") //TODO: Not working...
fun setTextToOneWordEachLine(view: TextView, text: String) {
    view.text = text.replace(" ", "\n")
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

@SuppressLint("SetTextI18n")
@BindingAdapter("stringKey", "numberValue", "stringValue", "unitName", requireAll=false)
fun putValues2String(view: TextView, stringKey: String, numberValue: Number?,
                     stringValue: String?, unitName: String?) {
    var text = ""
    if (numberValue != null)
        text = "$stringKey $numberValue"
    else if (stringValue != null)
        text = "$stringKey $stringValue"

    if (unitName != null)
        view.text = "$text $unitName"
    else
        view.text = text

}