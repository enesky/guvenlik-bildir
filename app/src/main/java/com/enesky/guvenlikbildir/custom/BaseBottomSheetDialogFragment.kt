package com.enesky.guvenlikbildir.custom

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.enesky.guvenlikbildir.extensions.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by Enes Kamil YILMAZ on 27.12.2019
 */

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        setupOnShowListener()
        return bottomSheetDialog as BottomSheetDialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //No call for super(). Bug on API Level > 11.
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null)
            try {
                super.show(manager, tag)
            } catch (e: IllegalStateException) {
                manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
            }
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
        hideKeyboard()
    }

    private fun setupOnShowListener() {

        bottomSheetDialog!!.setOnShowListener { dialog ->

            val frameLayout =
                (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

            if (frameLayout != null) {
                frameLayout.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                bottomSheetBehavior!!.skipCollapsed = true
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            }

        }
    }



}