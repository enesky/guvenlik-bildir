package com.enesky.guvenlikbildir.ui.fragment.options.contacts

import android.annotation.SuppressLint
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
import com.enesky.guvenlikbildir.adapter.AddContactAdapter
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.databinding.FragmentAddContactsBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.ui.activity.main.MainVM
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import kotlinx.android.synthetic.main.fragment_add_contacts.*
import kotlinx.coroutines.*
import java.text.Collator
import java.util.*

class AddContactsFragment : BaseFragment() {

    private lateinit var binding: FragmentAddContactsBinding
    private lateinit var addContactAdapter : AddContactAdapter
    private val mainVM by lazy {
        getViewModel {
            MainVM(AppDatabase.getDatabaseManager(activity!!.application))
        }
    }

    private var contactList: MutableList<Contact> = mutableListOf()
    private var selectedMap: MutableMap<Int, Contact> = mutableMapOf()

    override fun onStart() {
        super.onStart()
        if (mainVM.contactList.value.isNullOrEmpty()) {
            requireActivity().requireReadContactsPermission {
                GlobalScope.launch(Dispatchers.IO) {
                    contactList = getContactsList().toMutableList()

                    activity!!.runOnUiThread {
                        if (contactList.isNullOrEmpty()) {
                            context!!.showToast("Rehberinizde kayıtlı kişi bulunamadı.")
                            activity!!.onBackPressed()
                        } else {
                            mainVM.getChosenContactList().value?.let { contactList.removeAll(it) }
                            mainVM.contactList.value = contactList
                        }

                        pb_loading.makeItGone()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_contacts, container, false)
        binding.viewModel = mainVM
        mainVM.init(binding)
        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pb_loading.makeItVisible()

        addContactAdapter = AddContactAdapter(contactList, mainVM)
        addContactAdapter.setHasStableIds(true)

        if (!contactList.isNullOrEmpty())
            setupFastScroller()

        //TODO: Placeholder item yükleyebilirsin. -> Broccoli()

        rv_contacts.apply {
            addSelectedContactWatcher(selectedMap)
            setHasFixedSize(true)
            setItemViewCacheSize(10)
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            rv_contacts.adapter = addContactAdapter
        }

        tv_save.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                mainVM.addContactToList(selectedMap.values.toList())
                mainVM.contactList.value?.removeAll(selectedMap.values.toList())
            }
            activity!!.onBackPressed()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainVM.contactList.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                contactList = it
                setupFastScroller()
            }
        })

        mainVM.onClick.observe(viewLifecycleOwner, Observer {
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

    }

    private fun setupFastScroller() {
        if (!mainVM.isViewsLoaded.value!!) {
            addContactAdapter.update(contactList)
            rv_contacts.scheduleLayoutAnimation()
            fastScroller.setupWithRecyclerView(
                rv_contacts, { position ->
                    FastScrollItemIndicator.Text(
                        contactList[position].name.substring(0, 1).toUpperCase(Locale.getDefault())
                    )
                }
            )
            fastScrollerThumb.setupWithFastScroller(fastScroller)
            mainVM.isViewsLoaded.value = true
            pb_loading.makeItGone()
        }
    }

    private fun getContactsList() : List<Contact> {
        val contactList = mutableListOf<Contact>()

        val phones: Cursor? = context!!.contentResolver.query(
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

        return contactList
    }

}
