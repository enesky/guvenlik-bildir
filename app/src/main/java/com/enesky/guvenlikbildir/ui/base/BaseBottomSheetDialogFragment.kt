package com.enesky.guvenlikbildir.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.enesky.guvenlikbildir.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by Enes Kamil YILMAZ on 30.04.2020
 */

abstract class BaseBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        setupOnShowListener()
        return bottomSheetDialog as BottomSheetDialog
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)

        val dialog = dialog as BottomSheetDialog?
        dialog!!.setCanceledOnTouchOutside(cancelable)

        //val bottomSheetView = dialog.window!!.decorView.findViewById<View>(R.id.design_bottom_sheet)
        //BottomSheetBehavior.from(bottomSheetView).isHideable = cancelable
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            try {
                super.show(manager, tag)
            } catch (e: IllegalStateException) {
                manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
            }
        }
    }

    private fun setupOnShowListener() {

        bottomSheetDialog!!.setOnShowListener { dialog ->

            val frameLayout =
                (dialog as BottomSheetDialog).findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?

            if (frameLayout != null) {
                frameLayout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                bottomSheetBehavior!!.isDraggable = true
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior!!.skipCollapsed = true

                bottomSheetBehavior!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_HIDDEN,
                                BottomSheetBehavior.STATE_COLLAPSED,
                                    BottomSheetBehavior.STATE_DRAGGING,
                                        BottomSheetBehavior.STATE_SETTLING -> {
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {}
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {}
                        }

                    }

                })

            }
        }

    }

}