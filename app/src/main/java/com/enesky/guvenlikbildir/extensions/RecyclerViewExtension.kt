package com.enesky.guvenlikbildir.extensions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.others.Constants.loadingDuration

/**
 * Created by Enes Kamil YILMAZ on 23.02.2020
 */

fun RecyclerView.addSelectedContactWatcher(selectedItemList: MutableMap<Int, Contact>) {
    this.addOnChildAttachStateChangeListener(object: RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewDetachedFromWindow(view: View) {
            //ignored
        }

        override fun onChildViewAttachedToWindow(view: View) {
            if (selectedItemList.containsKey(getChildViewHolder(view).adapterPosition))
                view.setBackground(R.color.fern)
            else
                view.setBackground(android.R.color.white)
        }
    })
}

fun RecyclerView.updateRecyclerViewAnimDuration() = this.itemAnimator?.run {
    removeDuration = loadingDuration * 60 / 100
    addDuration = loadingDuration
}