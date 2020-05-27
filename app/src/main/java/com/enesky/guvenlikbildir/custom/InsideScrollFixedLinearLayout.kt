package com.enesky.guvenlikbildir.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewParent
import android.widget.LinearLayout
import androidx.annotation.Nullable

/**
 * Created by Enes Kamil YILMAZ on 27.05.2020
 */

class InsideScrollFixedLinearLayout : LinearLayout {

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle)

    private lateinit var viewParent: ViewParent

    fun setViewParent(viewParent: ViewParent) {
        this.viewParent = viewParent
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        when(ev?.action) {

            MotionEvent.ACTION_UP -> {
                if (null == viewParent)
                    parent.requestDisallowInterceptTouchEvent(false)
                else
                    viewParent.requestDisallowInterceptTouchEvent(false)
            }

            MotionEvent.ACTION_DOWN -> {
                if (null == viewParent)
                    parent.requestDisallowInterceptTouchEvent(true)
                else
                    viewParent.requestDisallowInterceptTouchEvent(true)
            }

            else -> {}
        }

        return super.onInterceptTouchEvent(ev)
    }

}