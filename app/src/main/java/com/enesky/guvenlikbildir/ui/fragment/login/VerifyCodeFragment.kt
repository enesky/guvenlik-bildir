package com.enesky.guvenlikbildir.ui.fragment.login

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.enesky.guvenlikbildir.R

class VerifyCodeFragment : Fragment() {

    private lateinit var viewModel: VerifyCodeVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_code, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VerifyCodeVM::class.java)
        // TODO: Use the ViewModel
    }

}
