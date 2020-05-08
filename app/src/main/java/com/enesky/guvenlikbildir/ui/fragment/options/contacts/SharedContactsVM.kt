package com.enesky.guvenlikbildir.ui.fragment.options.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.adapter.AddContactAdapter
import com.enesky.guvenlikbildir.adapter.ContactAdapter
import com.enesky.guvenlikbildir.databinding.FragmentAddContactsBinding
import com.enesky.guvenlikbildir.databinding.FragmentContactsBinding
import com.enesky.guvenlikbildir.model.Contact
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent

class SharedContactsVM : BaseViewModel(), ContactAdapter.ContactListener,
    AddContactAdapter.AddContactListener {

    private val _contactAdapter = MutableLiveData<ContactAdapter>()
    val contactAdapter : LiveData<ContactAdapter> = _contactAdapter
    private val _addContactAdapter = MutableLiveData<AddContactAdapter>()
    val addContactAdapter: LiveData<AddContactAdapter> = _addContactAdapter

    val contactList = MutableLiveData<MutableList<Contact>>().apply {
        value = mutableListOf()
    }
    val selectedContactList = MutableLiveData<MutableList<Contact>>().apply {
        value = mutableListOf()
    }

    val isViewsLoaded = MutableLiveData<Boolean>()
    val onClick = LiveEvent<Any>()
    val isSelectedListChanged = LiveEvent<Any>()

    fun init(binding: FragmentContactsBinding, contactList: MutableList<Contact>) {
        setViewDataBinding(binding)
        selectedContactList.value = contactList
        _contactAdapter.value = ContactAdapter(contactList,this)
    }

    fun init(binding: FragmentAddContactsBinding) {
        setViewDataBinding(binding)
        _addContactAdapter.value = AddContactAdapter(mutableListOf(), this)
        isViewsLoaded.value = false
    }

    override fun onDeleteClick(pos: Int, contact: Contact) {
        onClick.value = contact
    }

    override fun onItemClick(pos: Int) {
        onClick.value = pos
    }

    override fun onItemClick(pos: Int, contact: Contact) {
        onClick.value = pos to contact
    }

}
