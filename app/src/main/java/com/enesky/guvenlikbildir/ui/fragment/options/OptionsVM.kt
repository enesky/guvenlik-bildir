package com.enesky.guvenlikbildir.ui.fragment.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.OptionListAdapter
import com.enesky.guvenlikbildir.databinding.FragmentOptionsBinding
import com.enesky.guvenlikbildir.model.OptionItem
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent

class OptionsVM : BaseViewModel(), OptionListAdapter.OptionListListener {

    private val _optionListAdapter = MutableLiveData<OptionListAdapter>().apply {
        value = OptionListAdapter(
            listOf(OptionItem(R.drawable.ic_contact, "Kimlere SMS göndereceğini seç"),
                    OptionItem(R.drawable.ic_sms, "Gönderilecek SMS'leri düzenle"),
                    OptionItem(R.drawable.ic_sms, "Afetler Hakkında Bilgilendirmeler"),
                    OptionItem(R.drawable.ic_sms, "Acil Durum Telefon Numaraları"),
                    OptionItem(R.drawable.ic_about, "Uygulama Hakkında"),
                    OptionItem(R.drawable.ic_about, "Çıkış Yap")), this@OptionsVM)
    }
    val optionListAdapter: LiveData<OptionListAdapter> = _optionListAdapter

    private val _whereTo = LiveEvent<Int>()
    val whereTo: LiveEvent<Int> = _whereTo

    fun init(binding: FragmentOptionsBinding) {
        setViewDataBinding(binding)
    }

    override fun onItemClick(pos: Int, optionItem: OptionItem) {
        _whereTo.value = pos
    }

}