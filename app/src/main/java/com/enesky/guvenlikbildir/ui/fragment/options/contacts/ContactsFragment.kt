package com.enesky.guvenlikbildir.ui.fragment.options.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentContactsBinding
import com.enesky.guvenlikbildir.extensions.getUserInfo
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.makeItGone
import com.enesky.guvenlikbildir.model.Contact
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.trendyol.medusalib.navigator.transitionanimation.TransitionAnimationType
import kotlinx.android.synthetic.main.fragment_contacts.*

class ContactsFragment : BaseFragment() {

    private lateinit var binding: FragmentContactsBinding
    private val contactsVM by lazy {
        getViewModel { SharedContactsVM() }
    }

    private var contactList: List<Contact>? = null
    private var selectedList: MutableList<Contact> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false)
        binding.apply {
            viewModel = contactsVM
            lifecycleOwner = this@ContactsFragment
        }

        contactList = getUserInfo(App.mAuth.currentUser?.uid)?.contactList
        if (contactList == null)
            contactList = listOfNotNull()
        else
            placeholder.makeItGone()

        val selectedList = mutableListOf<Contact>()
        for (contact in contactList!!)
            selectedList.add(contact)

        contactsVM.init(binding, selectedList)

        contactsVM.onClick.observe(viewLifecycleOwner, Observer {

        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab_add_contact.setOnClickListener {
            multipleStackNavigator!!.start(AddContactsFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            contactList = getUserInfo(App.mAuth.currentUser?.uid)?.contactList
            if (contactList == null)
                contactList = listOfNotNull()
            else
                placeholder.makeItGone()

            val selectedList = mutableListOf<Contact>()
            for (contact in contactList!!)
                selectedList.add(contact)

            if (selectedList.isNotEmpty())
                contactsVM.contactAdapter.value?.update(selectedList)
        }


    }

}
