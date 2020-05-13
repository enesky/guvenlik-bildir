package com.enesky.guvenlikbildir.ui.fragment.options.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentAddContactsBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import kotlinx.android.synthetic.main.fragment_add_contacts.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.text.Collator
import java.util.*

@ObsoleteCoroutinesApi
class AddContactsFragment : BaseFragment() {

    private lateinit var binding: FragmentAddContactsBinding
    private val addContactsVM by lazy {
        getViewModel { SharedContactsVM() }
    }
    private var contactList: MutableList<Contact> = mutableListOf()
    private var selectedMap: MutableMap<Int, Contact> = mutableMapOf()
    private val scope = CoroutineScope(newSingleThreadContext("setList"))

    override fun onStart() {
        super.onStart()
        if (addContactsVM.contactList.value.isNullOrEmpty()) {
            requireActivity().requireReadContactsPermission {
                scope.launch {
                    getContactsList(requireActivity())
                }
            }
        } else {
            (addContactsVM.contactList.value as MutableList)
                .removeAll(addContactsVM.selectedContactList.value as MutableList)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_contacts, container, false)
        binding.viewModel = addContactsVM
        addContactsVM.init(binding)

        App.mAnalytics.setCurrentScreen(requireActivity(), this.javaClass.simpleName, null)

        contactList = addContactsVM.contactList.value!!
        val turkishLocale = Locale("tr", "TR")
        contactList.sortWith(Comparator { o1, o2 ->
            Collator.getInstance(turkishLocale).compare(o1.name, o2.name)
        })

        addContactsVM.contactList.observe(viewLifecycleOwner, Observer {
            if ((it as MutableList<Contact>).isNotEmpty()) {
                contactList = it
                setupFastScroller()
            }
        })

        addContactsVM.onClick.observe(viewLifecycleOwner, Observer {
            if (it is Pair<*,*>) {
                if (selectedMap.contains(it.first as Int))
                    selectedMap.remove(it.first as Int)
                else
                    selectedMap[it.first as Int] = it.second as Contact

                if (selectedMap.isNotEmpty()) {
                    tv_save.makeItVisible()
                    tv_save.text = "Ekle (${selectedMap.size})"
                } else {
                    tv_save.makeItGone()
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!contactList.isNullOrEmpty())
            setupFastScroller()

        rv_contacts.addSelectedContactWatcher(selectedMap)

        tv_save.setOnClickListener {
            addContactsVM.selectedContactList.value!!.addAll(selectedMap.values.toMutableList())
            addContactsVM.isSelectedListChanged.value = true

            //TODO: do it with room
            //add2ContactList(addContactsVM.selectedContactList.value!!)

            requireActivity().onBackPressed()
        }
    }

    private fun setupFastScroller() {
        if (!addContactsVM.isViewsLoaded.value!!) {
            addContactsVM.addContactAdapter.value!!.update(contactList)
            rv_contacts.scheduleLayoutAnimation()
            fastScroller.setupWithRecyclerView(
                rv_contacts, { position ->
                    FastScrollItemIndicator.Text(
                        contactList[position].name.substring(0, 1).toUpperCase(Locale.getDefault())
                    )
                }
            )
            fastScrollerThumb.setupWithFastScroller(fastScroller)
            addContactsVM.isViewsLoaded.value = true
            pb_loading.makeItGone()
        }
    }

    private fun getContactsList(context: Context) {
        val phones: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        while (phones!!.moveToNext()) {
            val name: String =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber: String =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    .replace(" ", "")
            val itype =
                phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
            val isMobile = itype == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ||
                    itype == ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE

            if (name.isNotEmpty() && phoneNumber.isNotEmpty())
                if (isMobile && !contactList.contains(Contact(name = name, number = phoneNumber)))
                    contactList.add(Contact(name = name, number = phoneNumber))
        }
        phones.close()

        val turkishLocale = Locale("tr", "TR")
        contactList.sortWith(Comparator { o1, o2 ->
            Collator.getInstance(turkishLocale).compare(o1.name, o2.name)
        })

        requireActivity().runOnUiThread {
            if (contactList.isNullOrEmpty()) {
                requireContext().showToast("Rehberinizde kayıtlı kişi bulunamadı.")
                requireActivity().onBackPressed()
            } else {
                contactList.removeAll(addContactsVM.selectedContactList.value as Collection<Contact>)
                addContactsVM.contactList.value = contactList
            }
        }
    }

}
