package com.enesky.guvenlikbildir.ui.fragment.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.OptionAdapter
import com.enesky.guvenlikbildir.databinding.FragmentOptionsBinding
import com.enesky.guvenlikbildir.model.OptionItem
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent

class OptionsVM : BaseViewModel(), OptionAdapter.OptionListListener {

    private val _optionListAdapter = MutableLiveData<OptionAdapter>().apply {
        value = OptionAdapter(
            listOf(OptionItem(R.drawable.ic_contact, "Kimlere SMS göndereceğini seç"),
                    OptionItem(R.drawable.ic_sms, "Gönderilecek SMS'leri düzenle"),
                    OptionItem(R.drawable.ic_info, "Afetler Hakkında Bilgilendirmeler"),
                    OptionItem(R.drawable.ic_phone, "Acil Durum Telefon Numaraları"),
                    OptionItem(R.drawable.ic_about, "Uygulamayı Puanla / Yorumla"),
                    OptionItem(R.drawable.ic_share, "Uygulamayı Paylaş / Öner"),
                    OptionItem(R.drawable.ic_about, "Uygulama Kodunu İncele"),
                    OptionItem(R.drawable.ic_exit_to_app, "Oturumu Kapat")), this@OptionsVM)
    }
    val optionAdapter: LiveData<OptionAdapter> = _optionListAdapter

    private val _whereTo = LiveEvent<Int>()
    val whereTo: LiveEvent<Int> = _whereTo

    fun init(binding: FragmentOptionsBinding) {
        setViewDataBinding(binding)
    }

    override fun onItemClick(pos: Int, optionItem: OptionItem) {
        _whereTo.value = pos
    }

}