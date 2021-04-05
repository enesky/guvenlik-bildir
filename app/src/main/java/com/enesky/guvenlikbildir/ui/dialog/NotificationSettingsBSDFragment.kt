package com.enesky.guvenlikbildir.ui.dialog

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.BottomSheetNotificationSettingsBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.isNotificationsEnabled
import com.enesky.guvenlikbildir.others.notificationIconResId
import com.enesky.guvenlikbildir.others.notificationMagLimit
import com.enesky.guvenlikbildir.ui.base.BaseBottomSheetDialogFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsVM

/**
 * Created by Enes Kamil YILMAZ on 09.05.2020
 */

class NotificationSettingsBSDFragment : BaseBottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetNotificationSettingsBinding
    private lateinit var optionsVM: OptionsVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_notification_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        optionsVM = getViewModel()
        App.mAnalytics.setCurrentScreen(activity!!, "bottom_sheet", this.javaClass.simpleName)

        refreshViews(notificationMagLimit.toDouble())

        binding.seekbar.apply {
            max = 55
            progress = ((notificationMagLimit / 0.1) - 25).toInt()
                setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        refreshViews((p1 + 25) * 0.1)
                    }
                    override fun onStartTrackingTouch(p0: SeekBar?) = Unit
                    override fun onStopTrackingTouch(p0: SeekBar?) {
                        notificationMagLimit = binding.tvMag.text.toString().toFloat()
                    }
                })
        }

        binding.switcher.apply {
            setChecked(isNotificationsEnabled, true)
            setOnCheckedChangeListener { isChecked ->
                isNotificationsEnabled = isChecked
                notificationIconResId = if (isNotificationsEnabled)
                    R.drawable.ic_notifications_active
                else
                    R.drawable.ic_notifications_off
                optionsVM.notificationResIdLive.value = notificationIconResId

                if (isChecked)
                    App.startWorker()
                else
                    App.stopWorker()
            }
        }
    }

    fun refreshViews(currentMag: Double) {
        binding.tvMag.text = currentMag.toString().subSequence(0,3)
        binding.tvInfo.text = getString(R.string.label_notification_info, currentMag.toString().subSequence(0,3))
        setMagBackgroundTint(binding.tvMag, binding.seekbar, currentMag)
    }

    private fun setMagBackgroundTint(view: View, seekbar: SeekBar, magnitude: Double) {
        val color = when {
            magnitude < 3.5 -> R.color.apple to "#388e3c"
            (magnitude >= 3.5) && (magnitude < 5) -> R.color.colorSecondary to "#f9aa33"
            else -> R.color.cinnabar to "#e53935"
        }
        view.setBackgroundTint(color.first)
        seekbar.progressTintList = ColorStateList.valueOf(Color.parseColor(color.second))
        seekbar.thumbTintList = ColorStateList.valueOf(Color.parseColor(color.second))
    }

}
