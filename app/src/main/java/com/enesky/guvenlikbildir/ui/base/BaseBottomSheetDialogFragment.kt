package com.enesky.guvenlikbildir.ui.base

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.extensions.getColor
import com.enesky.guvenlikbildir.extensions.showDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by Enes Kamil YILMAZ on 30.04.2020
 */

abstract class BaseBottomSheetDialogFragment: BottomSheetDialogFragment() {

    var bsDialog: BottomSheetDialog? = null
    var coordinatorLayout: CoordinatorLayout? = null
    var bottomSheet: FrameLayout? = null
    var behavior: BottomSheetBehavior<*>? = null
    var outsideOfSheet: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            bsDialog = dialog as BottomSheetDialog
            coordinatorLayout = bsDialog?.findViewById(com.google.android.material.R.id.coordinator)
            bottomSheet = bsDialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            outsideOfSheet = bsDialog?.findViewById(com.google.android.material.R.id.touch_outside)

            behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior?.isFitToContents = true
            behavior?.isHideable = false
            behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            behavior?.halfExpandedRatio = 0.99f

            bsDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                                        View.SYSTEM_UI_FLAG_FULLSCREEN
        dialog?.window?.statusBarColor = getColor(R.color.transparent)
    }

    /**
     * This should be used when sheet goes to collapse state after ui change
     * Simply it makes sheet fully expanded again
     */
    fun refreshUi() {
        behavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun setUncancellable(isUncancellable: Boolean) {
        if (isUncancellable) {
            outsideOfSheet?.setOnClickListener {
                if (dialog!!.isShowing) {
                    activity?.showDialog(
                        title = "Sms gönderme işlemi devam ediyor...",
                        message = "Gönderme işlemi tamamlanmadan bu ekranı kapatamazsınız.",
                        positiveButtonText = "Devam ediliyor...",
                        positiveButtonFunction = { },
                        countDownOnNegative = false,
                        isNegativeButtonEnabled = false
                    )
                }
            }

            //TODO: Çalışmıyor.
            activity?.onBackPressedDispatcher?.addCallback(activity!!,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (dialog!!.isShowing) {
                            activity?.showDialog(
                                title = "Sms gönderme işlemi devam ediyor...",
                                message = "Gönderme işlemi tamamlanmadan bu ekranı kapatamazsınız.",
                                positiveButtonText = "Devam ediliyor...",
                                positiveButtonFunction = { },
                                countDownOnNegative = false,
                                isNegativeButtonEnabled = false
                            )
                        }
                    }
            })

            behavior?.isHideable = false
            isCancelable = false
        } else {
            isCancelable = true
            outsideOfSheet?.setOnClickListener {
                dismissAllowingStateLoss()
            }
            activity?.onBackPressedDispatcher?.addCallback(activity!!,
                object : OnBackPressedCallback(false){
                    override fun handleOnBackPressed() {
                        dismissAllowingStateLoss()
                    }
                }
            )
        }
    }

}