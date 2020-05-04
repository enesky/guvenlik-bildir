package com.enesky.guvenlikbildir.ui.fragment.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.OptionAdapter
import com.enesky.guvenlikbildir.databinding.FragmentOptionsBinding
import com.enesky.guvenlikbildir.extensions.getString
import com.enesky.guvenlikbildir.model.OptionItem
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent

class OptionsVM : BaseViewModel(), OptionAdapter.OptionListListener {

    private val _optionListAdapter = MutableLiveData<OptionAdapter>().apply {
        value = OptionAdapter(
            listOf(
                OptionItem(R.drawable.ic_contact, getString(R.string.item_option_0)),
                OptionItem(R.drawable.ic_feedback, getString(R.string.item_option_1)),
                //OptionItem(R.drawable.ic_info, getString(R.string.item_option_2)),
                //OptionItem(R.drawable.ic_phone, getString(R.string.item_option_3)),
                OptionItem(R.drawable.ic_about, getString(R.string.item_option_4)),
                OptionItem(R.drawable.ic_share_grey, getString(R.string.item_option_5)),
                OptionItem(R.drawable.ic_mail, getString(R.string.item_option_6)),
                OptionItem(R.drawable.ic_github, getString(R.string.item_option_7)),
                OptionItem(R.drawable.ic_about, getString(R.string.item_option_8)),
                OptionItem(R.drawable.ic_exit_to_app, getString(R.string.item_option_9))
            ), this@OptionsVM
        )
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