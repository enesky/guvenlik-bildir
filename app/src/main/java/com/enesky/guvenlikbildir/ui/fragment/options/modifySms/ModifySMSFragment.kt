package com.enesky.guvenlikbildir.ui.fragment.options.modifySms

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentModifySmsBinding
import com.enesky.guvenlikbildir.extensions.Constants
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.safeSms
import com.enesky.guvenlikbildir.extensions.unsafeSms
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_modify_sms.*

class ModifySMSFragment: BaseFragment() {

    private lateinit var modifySmsVM: ModifySmsVM
    private lateinit var binding: FragmentModifySmsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_modify_sms, container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        modifySmsVM = getViewModel()
        binding.apply {
            viewModel = modifySmsVM
            lifecycleOwner = this@ModifySMSFragment
        }
        modifySmsVM.init(binding)

        modifySmsVM.safeSmsUpdated.observe(viewLifecycleOwner, Observer { isSafeSmsUpdated ->
            if (isSafeSmsUpdated) {
                safeSms = et_safe_sms.text.toString()
                modifySmsVM.setSafeSms(safeSms!!)
            }
        })

        modifySmsVM.unsafeSmsUpdated.observe(viewLifecycleOwner, Observer { isUnsafeSmsUpdated ->
            if (isUnsafeSmsUpdated) {
                unsafeSms = et_unsafe_sms.text.toString()
                modifySmsVM.setUnsafeSms(unsafeSms!!)
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_safe_sms.setText(safeSms)
        et_unsafe_sms.setText(unsafeSms)

        et_safe_sms.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                modifySmsVM.setSafeSmsUpdated(true)
            }
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        et_unsafe_sms.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                modifySmsVM.setUnsafeSmsUpdated(true)
            }
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        tv_safe_location.setOnClickListener {
            openInfoCountDownDialog(Constants.locationMapLink)
        }

        tv_unsafe_location.setOnClickListener {
            openInfoCountDownDialog(Constants.locationMapLink)
        }

        btn_save.setOnClickListener {
           //TODO: ?
        }

    }

}
