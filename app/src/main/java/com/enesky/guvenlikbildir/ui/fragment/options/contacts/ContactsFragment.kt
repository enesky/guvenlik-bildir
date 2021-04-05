package com.enesky.guvenlikbildir.ui.fragment.options.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.ContactAdapter
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.databinding.FragmentContactsBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.makeItGone
import com.enesky.guvenlikbildir.extensions.makeItVisible
import com.enesky.guvenlikbildir.extensions.requireReadContactsPermission
import com.enesky.guvenlikbildir.ui.activity.main.MainVM
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.trendyol.medusalib.navigator.transitionanimation.TransitionAnimationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContactsFragment : BaseFragment() {

    private lateinit var binding: FragmentContactsBinding
    private lateinit var contactAdapter: ContactAdapter
    private val mainVM by lazy {
        getViewModel {
            MainVM(AppDatabase.dbInstance!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false)
        binding.apply {
            viewModel = mainVM
            lifecycleOwner = this@ContactsFragment
        }
        mainVM.init(binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)

        binding.placeholder.makeItVisible()
        binding.pbLoading.makeItVisible()

        contactAdapter = ContactAdapter(listOfNotNull(), mainVM)
        contactAdapter.setHasStableIds(true)

        binding.rvContacts.apply {
            adapter = contactAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }

        binding.fabAddContact.setOnClickListener {
            requireContext().requireReadContactsPermission {
                multipleStackNavigator!!.start(AddContactsFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainVM.onClick.observe(viewLifecycleOwner, Observer { contact ->
            GlobalScope.launch(Dispatchers.Default) {
                if (contact is Contact)
                    mainVM.contactRepository.unselectContact(contact)
            }
        })

        mainVM.getSelectedContactList().observe(viewLifecycleOwner, Observer {
            contactAdapter.update(it)
            binding.pbLoading.makeItGone()

            if (it.isNullOrEmpty())
                binding.placeholder.makeItVisible()
            else
                binding.placeholder.makeItGone()
        })

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        binding.rvContacts.scheduleLayoutAnimation()
    }

}
