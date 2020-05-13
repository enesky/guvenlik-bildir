package com.enesky.guvenlikbildir.ui.fragment.options.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentContactsBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.trendyol.medusalib.navigator.transitionanimation.TransitionAnimationType
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.coroutines.ObsoleteCoroutinesApi

class ContactsFragment : BaseFragment() {

    private lateinit var binding: FragmentContactsBinding
    private val contactsVM by lazy {
        getViewModel { SharedContactsVM() }
    }
    private var selectedList: MutableList<Contact> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false)
        binding.apply {
            viewModel = contactsVM
            lifecycleOwner = this@ContactsFragment
        }
        contactsVM.init(binding, mutableListOf())

        //getUsersContactList { prepareViews(it) }
        //TODO: room ile listeyi çek

        contactsVM.isSelectedListChanged.observe(viewLifecycleOwner, Observer {
            contactsVM.contactAdapter.value!!.update(contactsVM.selectedContactList.value!!)
            if (contactsVM.selectedContactList.value.isNullOrEmpty())
                placeholder.makeItVisible()
            else
                placeholder.makeItGone()
        })

        contactsVM.onClick.observe(viewLifecycleOwner, Observer { any ->
            if (any is Contact) {
                pb_loading.makeItVisible()
                //removeFromContactList(any) { deleteAndRefresh(any) }
                //getUsersContactList { prepareViews(it) }

                //TODO: room ile çek
            }
        })

        return binding.root
    }

    @ObsoleteCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pb_loading.makeItVisible()

        fab_add_contact.setOnClickListener {
            requireContext().requireReadContactsPermission {
                multipleStackNavigator!!.start(AddContactsFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        rv_contacts.scheduleLayoutAnimation()
    }

    private fun prepareViews(any: Any?) {
        pb_loading.makeItVisible()
        if (any is MutableList<*>) {
            if ((any as MutableList<Contact>).isNullOrEmpty()) {
                selectedList.clear()
                contactsVM.selectedContactList.value = selectedList
                contactsVM.contactAdapter.value!!.update(selectedList)
                rv_contacts.scheduleLayoutAnimation()
                placeholder.makeItVisible()
            } else {
                selectedList.clear()
                selectedList.addAll(any)
                contactsVM.selectedContactList.value = selectedList
                contactsVM.contactAdapter.value!!.update(selectedList)
                rv_contacts.scheduleLayoutAnimation()
                placeholder.makeItGone()
            }
        } else {
            requireContext().showToast(any.toString())
        }
        pb_loading.makeItGone()
    }

    private fun deleteAndRefresh(contact: Contact) {
        val selectedCList = contactsVM.selectedContactList.value
        selectedCList?.remove(contact)
        contactsVM.selectedContactList.value = selectedCList
        contactsVM.contactAdapter.value!!.update(selectedList)

        if (!contactsVM.contactList.value!!.contains(contact))
            contactsVM.contactList.value!!.add(contact)

        if(rv_contacts != null && pb_loading != null) {
            rv_contacts.scheduleLayoutAnimation()
            pb_loading.makeItGone()
            if (contactsVM.selectedContactList.value.isNullOrEmpty())
                placeholder.makeItVisible()
            else
                placeholder.makeItGone()
        }
    }

}
