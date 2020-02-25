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
import com.enesky.guvenlikbildir.model.Contact
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import kotlinx.android.synthetic.main.fragment_add_contacts.*
import kotlinx.coroutines.*
import java.text.Collator
import java.util.*

@ObsoleteCoroutinesApi
class AddContactsFragment : BaseFragment() {

    private lateinit var binding: FragmentAddContactsBinding
    private val addContactsVM by lazy {
        getViewModel { SharedContactsVM() }
    }

    private val scope = CoroutineScope(newSingleThreadContext("setList"))
    private var contactList: MutableList<Contact> = mutableListOf()
    private var selectedMap: MutableMap<Int, Contact> = mutableMapOf()

    override fun onStart() {
        super.onStart()
        if (addContactsVM.contactList.value.isNullOrEmpty()) {
            requireContext().requireReadContactsPermission {
                scope.launch {
                    getContactsList(requireContext())
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_contacts, container, false)
        binding.viewModel = addContactsVM

        addContactsVM.init(binding)

        contactList = addContactsVM.contactList.value!!

        addContactsVM.contactList.observe(viewLifecycleOwner, Observer {
            if ((it as MutableList<Contact>).isNotEmpty()) {
                contactList = it
                if (!addContactsVM.isViewsLoaded.value!!)
                    setupViews()
            }
        })

        addContactsVM.onClick.observe(viewLifecycleOwner, Observer {
            /*if (it is Int) {
                selectedContactList = addContactsVM.addContactAdapter.value!!.getSelectedItems()
                if (selectedContactList.size > 0) {
                    tv_save.makeItVisible()
                    tv_save.text = "Ekle (${selectedContactList.size})"
                } else {
                    tv_save.makeItGone()
                }

            }*/
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
        if (!contactList.isNullOrEmpty() && !addContactsVM.isViewsLoaded.value!!)
            setupViews()

        rv_contacts.addSelectedItemWatcher(selectedMap)

        tv_save.setOnClickListener {
            addContactsVM.selectedContactList.value = selectedMap.values.toMutableList()
            addContactList(selectedMap.values.toMutableList(), App.mAuth.currentUser!!)
            requireActivity().onBackPressed()
        }

    }

    private fun setupViews() {
        addContactsVM.addContactAdapter.value!!.update(contactList)
        rv_contacts.scheduleLayoutAnimation()
        fastScroller.setupWithRecyclerView(
            rv_contacts, { position ->
                FastScrollItemIndicator.Text(
                    contactList[position].name.substring(0, 1).toUpperCase()
                )
            }
        )
        fastScrollerThumb.setupWithFastScroller(fastScroller)
        fastScroller.makeItVisible()
        pb_loading.makeItGone()
        addContactsVM.isViewsLoaded.value = true
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

            //TODO: seçili olan kişileri listede gösterme
            if (name.isNotEmpty() && phoneNumber.isNotEmpty())
                if (isMobile && !contactList.contains(Contact(name, phoneNumber)))
                    contactList.add(Contact(name, phoneNumber))
        }
        phones.close()

        val turkishLocale = Locale("tr", "TR")
        contactList.sortWith(Comparator { o1, o2 ->
            Collator.getInstance(turkishLocale).compare(o1.name, o2.name)
        })

        requireActivity().runOnUiThread {
            addContactsVM.contactList.value = contactList
        }
    }

}