package com.enesky.guvenlikbildir.custom

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Enes Kamil YILMAZ on 20.12.2019
 */

class StatefulRecyclerView : RecyclerView {

    private var mLayoutManagerSavedState: Parcelable? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle)

    companion object {
        private const val SAVED_SUPER_STATE = "super-state"
        private const val SAVED_LAYOUT_MANAGER = "layout-manager-state"
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState())
        bundle.putParcelable(SAVED_LAYOUT_MANAGER, this.layoutManager!!.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = (state).also {
                mLayoutManagerSavedState = it.getParcelable(SAVED_LAYOUT_MANAGER)
                state = it.getParcelable(SAVED_SUPER_STATE)
            }
        }
        super.onRestoreInstanceState(state)
    }

    /**
     * Restores scroll position after configuration change.
     * **NOTE:** Must be called after adapter has been set.
     */
    private fun restorePosition() {
        if (mLayoutManagerSavedState != null) {
            this.layoutManager!!.onRestoreInstanceState(mLayoutManagerSavedState)
            mLayoutManagerSavedState = null
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        restorePosition()
    }

}