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
import com.enesky.guvenlikbildir.adapter.AddContactAdapter
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.databinding.FragmentAddContactsBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.ui.activity.main.MainVM
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import kotlinx.coroutines.*
import java.text.Collator
import java.util.*

class AddContactsFragment : BaseFragment() {

    private lateinit var binding: FragmentAddContactsBinding
    private lateinit var addContactAdapter : AddContactAdapter
    private val mainVM by lazy {
        getViewModel {
            MainVM(AppDatabase.dbInstance!!)
        }
    }

    private var contactList: MutableList<Contact> = mutableListOf()
    private var selectedMap: MutableMap<Int, Contact> = mutableMapOf()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_contacts, container, false)
        binding.viewModel = mainVM
        mainVM.init(binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)
        binding.pbLoading.makeItVisible()

        addContactAdapter = AddContactAdapter(contactList, mainVM)
        addContactAdapter.setHasStableIds(true)

        binding.rvContacts.apply {
            adapter = addContactAdapter
            addSelectedContactWatcher(selectedMap)
            setHasFixedSize(true)
            setItemViewCacheSize(30)
        }

        binding.tvSave.setOnClickListener {
            mainVM.contactRepository.selectContacts(selectedMap.values.toList())
            activity!!.onBackPressed()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainVM.getUnselectedContactList().observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                activity!!.requireReadContactsPermission {
                    GlobalScope.launch(Dispatchers.Default) {
                        contactList = getContactsList(context!!).toMutableList()
                        mainVM.contactRepository.refreshContacts(contactList)

                        withContext(Dispatchers.Main) {
                            if (contactList.isNullOrEmpty()) {
                                context!!.showToast(getString(R.string.label_nothing_found_in_contacts))
                                activity!!.onBackPressed()
                            }
                            binding.pbLoading.makeItGone()
                        }
                    }
                }
            } else {
                contactList = it.toMutableList()
                val turkishLocale = Locale("tr", "TR")
                contactList.sortWith(Comparator { o1, o2 ->
                    Collator.getInstance(turkishLocale).compare(o1.name, o2.name)
                })
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
                    binding.tvSave.makeItVisible()
                    binding.tvSave.text = getString(R.string.label_add_contact_with_number, selectedMap.size)
                } else {
                    binding.tvSave.makeItGone()
                }
            }
        })

    }

    private fun setupFastScroller() {
        if (!mainVM.isViewsLoaded.value!!) {
            addContactAdapter.update(contactList)
            binding.rvContacts.scheduleLayoutAnimation()
            binding.fastScroller.setupWithRecyclerView(
                binding.rvContacts, { position ->
                    FastScrollItemIndicator.Text(
                        contactList[position].name.substring(0, 1).toUpperCase(Locale.getDefault())
                    )
                }
            )
            binding.fastScrollerThumb.setupWithFastScroller(binding.fastScroller)
            mainVM.isViewsLoaded.postValue(true)
            binding.pbLoading.makeItGone()
        }
    }

    companion object {
        suspend fun getContactsList(context: Context) : List<Contact> {
            return withContext(Dispatchers.Default) {
                val contactList = mutableListOf<Contact>()

                val phones: Cursor? = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null
                )

                while (phones!!.moveToNext()) {
                    val name: String = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val phoneNumber: String = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replace(" ", "")
                    val itype = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
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

                return@withContext contactList
            }
        }
    }

}
