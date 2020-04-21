package com.enesky.guvenlikbildir.custom

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Nullable
import com.google.android.material.card.MaterialCardView
import android.animation.ValueAnimator
import android.view.ViewTreeObserver
import com.enesky.guvenlikbildir.extensions.Constants

/**
 * Created by Enes Kamil YILMAZ on 05.02.2020
 */

class CollapsibleCardView : MaterialCardView {

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle)

    private var compactHeight = 0
    internal var expandedHeight = 0
    internal var isExpanded = false

    init {
        viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    compactHeight = height
                    if(expandedHeight == 0)
                        expandedHeight = compactHeight * 2
                    return true
                }
            })
    }

    fun toggleView() {
        if (isExpanded) {
            collapseView(null)
        } else {
            expandView(250, null)
        }
    }

    fun collapseView(function: (() -> Unit)?) {
        val anim = ValueAnimator.ofInt(measuredHeightAndState, compactHeight)
        anim.duration = Constants.defaultAnimationDuration

        anim.addUpdateListener { valueAnimator ->
            val mLayoutParams = layoutParams
            mLayoutParams.height = valueAnimator.animatedValue as Int
            layoutParams = mLayoutParams
            isExpanded = false
        }

        anim.start()
        function?.invoke()
    }

    fun expandView(height: Int, function: (() -> Unit)?) {
        val anim = ValueAnimator.ofInt(measuredHeightAndState, height)
        anim.duration = Constants.defaultAnimationDuration

        anim.addUpdateListener { valueAnimator ->
            val mLayoutParams = layoutParams
            mLayoutParams.height = valueAnimator.animatedValue as Int
            layoutParams = mLayoutParams
            isExpanded = true
        }

        anim.start()
        function?.invoke()
    }

}