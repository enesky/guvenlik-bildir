package com.enesky.guvenlikbildir.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.enesky.guvenlikbildir.R

class ToastUtil private constructor() {
    companion object {
        fun showToast(context: Context, text: String?) {
            val inflater = LayoutInflater.from(context)
            val layout: View = inflater.inflate(R.layout.default_toast, null, false)
            val textView: TextView = layout.findViewById(R.id.toast_tv)
            textView.gravity = Gravity.CENTER
            textView.text = text
            val toast = Toast(context)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layout
            toast.setGravity(Gravity.BOTTOM, 0, 200)
            toast.show()
        }
    }
}