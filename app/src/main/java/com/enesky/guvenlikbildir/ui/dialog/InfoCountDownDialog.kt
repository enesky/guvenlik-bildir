package com.enesky.guvenlikbildir.ui.dialog

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import kotlinx.android.synthetic.main.dialog_info_count_down.*
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

class InfoCountDownDialog : DialogFragment() {

    private lateinit var timer: CountDownTimer
    private var type: String = Constants.polis

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, p0: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_info_count_down, container, false)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.mAnalytics.setCurrentScreen(activity!!, "dialog", this.javaClass.simpleName)

        when {
            tag.equals(Constants.polis) -> {
                type = Constants.polis
                tv_dialog_title.text = getString(R.string.label_calling_155)
            }
            tag.equals(Constants.acilYardım) -> {
                type = Constants.acilYardım
                tv_dialog_title.text = getString(R.string.label_calling_112)
            }
            tag.equals(Constants.itfaiye) -> {
                type = Constants.itfaiye
                tv_dialog_title.text = getString(R.string.label_calling_110)
            }
            tag!!.contains(Constants.map) -> {
                type = Constants.map
                tv_dialog_title.text = getString(R.string.label_google_maps)
            }
            tag.equals(Constants.gpsSetting) -> {
            type = Constants.gpsSetting
            tv_dialog_title.text = getString(R.string.label_no_gps_connection_found)
            }
            tag!!.contains(Constants.locationMapLink) -> {
                type = Constants.locationMapLink
                tv_dialog_title.text = getString(R.string.label_google_maps)
            }
        }

        val params = Bundle().apply {
            putString("action_type", type)
        }

        App.mAnalytics.logEvent("InfoCountDownDialog", params)

        tv_dismiss.setOnClickListener {
            dismiss()
        }

        startCountDown()

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        timer.cancel()
        Timber.tag("InfoCountDownDialog").d("onDismiss(): %s", type)
    }

    private fun startCountDown() {
        timer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_countdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                when (type) {
                    Constants.polis, Constants.acilYardım, Constants.itfaiye -> {
                        requireContext().requireCallPhonePermission {
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$type"))
                            startActivity(intent)

                            val params = Bundle().apply {
                                putString("Dialed_number", type)
                            }
                            App.mAnalytics.logEvent("InfoCountDown_Calling", params)
                        }

                        dismiss()
                    }
                    Constants.map -> {
                        val splitTag = tag!!.split(delimiters = *arrayOf("map"))
                        openGoogleMaps(splitTag[1], splitTag[2])
                        dismiss()
                    }
                    Constants.locationMapLink -> {
                        openLastKnownLocation()
                        dismiss()
                    }
                    Constants.gpsSetting -> {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        dismiss()
                    }
                }

            }
        }.start()
    }

}