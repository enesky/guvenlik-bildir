package com.enesky.guvenlikbildir.ui.fragment.options.addContacts

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentAddContactsBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.requireReadContactsPermission
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import io.github.farhad.contactpicker.ContactPicker
import kotlinx.android.synthetic.main.fragment_add_contacts.*

class AddContactsFragment : BaseFragment() {

    private lateinit var addContactsVM: AddContactsVM
    private lateinit var binding: FragmentAddContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_contacts, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        addContactsVM = getViewModel()
        binding.apply {
            viewModel = addContactsVM
            lifecycleOwner = this@AddContactsFragment
        }
        addContactsVM.init(binding)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn.setOnClickListener {
            requireContext().requireReadContactsPermission {

                /*val picker: ContactPicker? = ContactPicker.create(
                    activity = requireActivity() as AppCompatActivity,
                    onContactPicked = { Log.d("TAG", it.name + ": " + it.number) },
                    onFailure = { Log.d("TAG", it.localizedMessage) })

                picker!!.pick()*/

                getContactsList(requireContext())
            }
        }

    }

    data class Contact(val number: String, val name: String?)

    fun getContactsList(context: Context) {
        val contacts: ArrayList<Contact> = ArrayList()
        val phones: Cursor? = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null)

        while (phones!!.moveToNext()) {
            val name: String = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber: String = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "")
            val itype = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
            val isMobile = itype == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE || itype == ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE

            if(name.isNotEmpty() && phoneNumber.isNotEmpty())
                if (isMobile && !contacts.contains(Contact(name, phoneNumber)))
                    contacts.add(Contact(name, phoneNumber))
        }
        phones.close()
        //return contacts
    }

}
