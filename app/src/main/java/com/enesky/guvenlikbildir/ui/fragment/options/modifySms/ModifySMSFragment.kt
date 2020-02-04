package com.enesky.guvenlikbildir.ui.fragment.options.modifySms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentModifySmsBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_safe_sms.setText("Güvendeyim.\nBulunduğum Konum: https://www.google.com/maps/place/41.01844,28.9941283")
        et_unsafe_sms.setText("Güvende değilim.\nBulunduğum Konum: https://www.google.com/maps/place/41.01844,28.9941283")

    }

}
