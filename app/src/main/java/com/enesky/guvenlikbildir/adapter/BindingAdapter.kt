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

@BindingAdapter("setAdapter")
fun bindStatefulRecyclerViewAdapter(view: StatefulRecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.setHasFixedSize(true)
    view.layoutManager = LinearLayoutManager(view.context)
    view.layoutAnimation = AnimationUtils.loadLayoutAnimation(view.context, R.anim.layout_animation)
    view.adapter = adapter
}

@BindingAdapter("setAdapter")
fun bindRecyclerViewAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.setHasFixedSize(true)
    view.layoutManager = LinearLayoutManager(view.context)
    view.layoutAnimation = AnimationUtils.loadLayoutAnimation(view.context, R.anim.layout_animation)
    view.adapter = adapter
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

@BindingAdapter("beGoneIfListEmpty")
fun beGoneIfListEmpty(view: View, list: List<Any>) {
    if (list.isNullOrEmpty())
        view.makeItGone()
    else
        view.makeItVisible()
}

@SuppressLint("SimpleDateFormat")
@BindingAdapter("formattedDateText")
fun bindFormattedDate(view: TextView, formattedDate: String) {
    val p = PrettyTime()
    val simpleDateFormat = SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT)
    val date = simpleDateFormat.parse(formattedDate)
    view.text = p.format(date)
}

@BindingAdapter("text")
fun setText(view: TextView, text: Int) {
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

@BindingAdapter("dismiss")
fun dismiss(view: EditText, dismiss: Boolean) {
    view.setOnClickListener {
        //TODO: dismiss() ?
    }
}

@BindingAdapter("setImage")
fun setImage(view: ImageView, imageId: Int) {
    view.setImageResource(imageId)
}