package com.enesky.guvenlikbildir.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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

    private fun setupOnShowListener() {
        bottomSheetDialog!!.setOnShowListener { dialog ->
            val frameLayout =
                (dialog as BottomSheetDialog)
                    .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        as FrameLayout?

            if (frameLayout != null) {
                frameLayout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior!!.skipCollapsed = true
                bottomSheetBehavior!!.isFitToContents = true
            }
        }
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)

        val dialog = dialog as BottomSheetDialog?
        dialog!!.setCanceledOnTouchOutside(cancelable)

        //val bottomSheetView = dialog.window!!.decorView.findViewById<View>(R.id.design_bottom_sheet)
        //BottomSheetBehavior.from(bottomSheetView).isHideable = cancelable
    }

}