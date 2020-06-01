package com.enesky.guvenlikbildir.ui.fragment.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.OptionAdapter
import com.enesky.guvenlikbildir.databinding.FragmentOptionsBinding
import com.enesky.guvenlikbildir.extensions.getString
import com.enesky.guvenlikbildir.others.notificationIconResId
import com.enesky.guvenlikbildir.model.OptionItem
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent

class OptionsVM : BaseViewModel(), OptionAdapter.OptionListListener {

    val notificationResIdLive = MutableLiveData(notificationIconResId)

    val optionList = MutableLiveData(
        listOf(
            OptionItem(R.drawable.ic_contact, getString(R.string.item_option_0)),
            OptionItem(R.drawable.ic_feedback, getString(R.string.item_option_1)),
            OptionItem(R.drawable.ic_report_24dp, getString(R.string.label_sms_report_history)),
            OptionItem(notificationResIdLive.value!!, getString(R.string.item_option_10)),
            //OptionItem(R.drawable.ic_info, getString(R.string.item_option_2)),
            //OptionItem(R.drawable.ic_phone, getString(R.string.item_option_3)),
            OptionItem(R.drawable.ic_shop, getString(R.string.item_option_4)),
            OptionItem(R.drawable.ic_share_grey, getString(R.string.item_option_5)),
            OptionItem(R.drawable.ic_mail, getString(R.string.item_option_6)),
            OptionItem(R.drawable.ic_github, getString(R.string.item_option_7)),
            OptionItem(R.drawable.ic_about, getString(R.string.item_option_8))
            //OptionItem(R.drawable.ic_exit_to_app, getString(R.string.item_option_9))
        )
    )

    private val _optionListAdapter = MutableLiveData<OptionAdapter>()
    val optionAdapter: LiveData<OptionAdapter> = _optionListAdapter

    private val _whereTo = LiveEvent<Int>()
    val whereTo: LiveEvent<Int> = _whereTo

    fun init(binding: FragmentOptionsBinding) {
        _optionListAdapter.value = OptionAdapter(optionList.value!!, this@OptionsVM)
        setViewDataBinding(binding)
    }

    override fun onItemClick(pos: Int, optionItem: OptionItem) {
        _whereTo.value = pos
    }

}