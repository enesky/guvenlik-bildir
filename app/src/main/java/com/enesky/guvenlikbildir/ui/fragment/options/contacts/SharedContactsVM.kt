package com.enesky.guvenlikbildir.ui.fragment.options.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.adapter.ContactAdapter
import com.enesky.guvenlikbildir.databinding.FragmentAddContactsBinding
import com.enesky.guvenlikbildir.databinding.FragmentContactsBinding
import com.enesky.guvenlikbildir.model.Contact
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent

class SharedContactsVM : BaseViewModel(), ContactAdapter.ContactListener {

    private val _contactAdapter = MutableLiveData<ContactAdapter>()
    val contactAdapter : LiveData<ContactAdapter> = _contactAdapter

    private val _addContactAdapter = MutableLiveData<ContactAdapter>()
    val addContactAdapter: LiveData<ContactAdapter> = _addContactAdapter

    val contactList = MutableLiveData<MutableList<Contact>>().apply {
        value = mutableListOf()
    }
    val selectedContactList = MutableLiveData<MutableList<Contact>>().apply {
        value = mutableListOf()
    }

    val isViewsLoaded = MutableLiveData<Boolean>()

    val onClick = LiveEvent<HashMap<Int, Contact>>()

    fun init(binding: FragmentContactsBinding, contactList: List<Contact>) {
        setViewDataBinding(binding)
        _contactAdapter.value = ContactAdapter(contactList,this, false)
    }

    fun init(binding: FragmentAddContactsBinding) {
        setViewDataBinding(binding)
        _addContactAdapter.value = ContactAdapter(mutableListOf(), this, true)
        isViewsLoaded.value = false
    }

    override fun onDeleteClick(pos: Int, contact: Contact) {
        onClick.value = hashMapOf(pos to contact)
    }

    override fun onItemClick(pos: Int, contact: Contact) {
        onClick.value = hashMapOf(pos to contact)
    }

}
