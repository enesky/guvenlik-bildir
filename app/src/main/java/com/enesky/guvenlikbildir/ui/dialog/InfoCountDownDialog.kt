package com.enesky.guvenlikbildir.ui.dialog

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.extensions.Constants
import com.enesky.guvenlikbildir.extensions.openGoogleMaps
import com.enesky.guvenlikbildir.extensions.sendSMS
import kotlinx.android.synthetic.main.dialog_info_count_down.*

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

class InfoCountDownDialog: DialogFragment() {

    private lateinit var timer: CountDownTimer
    private var phoneNumber: String = Constants.polis

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, p0: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_info_count_down, container, false)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        when {
            tag.equals(Constants.polis) -> {
                phoneNumber = Constants.polis
                tv_dialog_title.text = getString(R.string.label_calling_155)
            }
            tag.equals(Constants.acilYardım) -> {
                phoneNumber = Constants.acilYardım
                tv_dialog_title.text = getString(R.string.label_calling_112)
            }
            tag.equals(Constants.itfaiye) -> {
                phoneNumber = Constants.itfaiye
                tv_dialog_title.text = getString(R.string.label_calling_110)
            }
            tag!!.contains(Constants.map) -> {
                phoneNumber = Constants.map
                tv_dialog_title.text = getString(R.string.label_google_maps)
            }
            tag!!.contains("1") -> {
                phoneNumber = "1"
                tv_dialog_title.text = getString(R.string.label_sending_sms)
            }
            tag!!.contains("2") -> {
                phoneNumber = "2"
                tv_dialog_title.text = getString(R.string.label_sending_sms)
            }
        }

        tv_okey.setOnClickListener {
            dismiss()
        }

        startCountDown()

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        timer.cancel()
        Log.d("InfoCountDownDialog", "onDismiss(): $phoneNumber")
    }

    private fun startCountDown() {
        timer = object : CountDownTimer(3200, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_countdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                when(phoneNumber) {
                    Constants.polis, Constants.acilYardım, Constants.itfaiye -> {
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
                        startActivity(intent)
                    }
                    Constants.map -> {
                        val splitTag = tag!!.split(delimiters = *arrayOf("map"))
                        openGoogleMaps(splitTag[1], splitTag[2])
                    }
                    else -> {
                        sendSMS("+905383115141", listOf( "+905383115141"), "hiiii")
                    }
                }
                dismiss()
            }
        }.start()
    }

}