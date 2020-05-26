package com.enesky.guvenlikbildir.ui.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.extensions.showDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by Enes Kamil YILMAZ on 30.04.2020
 */

abstract class BaseBottomSheetDialogFragment: BottomSheetDialogFragment() {

    var bsDialog: BottomSheetDialog? = null
    var bottomSheet: FrameLayout? = null
    var behavior: BottomSheetBehavior<*>? = null
    var outsideOfSheet: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            bsDialog = dialog as BottomSheetDialog
            bottomSheet = bsDialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            outsideOfSheet = bsDialog?.window?.decorView?.findViewById(com.google.android.material.R.id.touch_outside)

            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
            //bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT

            behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            behavior?.skipCollapsed = true

            bsDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN
        dialog?.window?.statusBarColor = resources.getColor(R.color.transparent)
    }

    fun setAreYouSureDialog(isCancellable: Boolean) {
        if (outsideOfSheet != null && isCancellable) {
            outsideOfSheet!!.setOnClickListener {
                if (dialog!!.isShowing) {
                    activity?.showDialog(
                        title = "İşlemi iptal et ?",
                        message = "Sms gönderme işlemi tamamlanmadan çıkmak istiyor musunuz ?",
                        positiveButtonText = "Devam Et",
                        positiveButtonFunction = { },
                        negativeButtonFunction = {
                            dismiss()
                        },
                        countDownOnNegative = false
                    )
                }
            }

            activity!!.onBackPressedDispatcher.
            addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (dialog!!.isShowing) {
                        activity?.showDialog(
                            title = "İşlemi iptal et ?",
                            message = "Sms gönderme işlemini iptal edip çıkmak istiyor musunuz?",
                            positiveButtonText = "Devam Et",
                            positiveButtonFunction = { },
                            negativeButtonFunction = {
                                dismiss()
                            },
                            countDownOnNegative = false
                        )
                    }
                }
            })

            behavior?.isHideable = false
        }
    }

}