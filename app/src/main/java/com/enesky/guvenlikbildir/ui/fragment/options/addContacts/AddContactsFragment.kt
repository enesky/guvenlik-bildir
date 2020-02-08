package com.enesky.guvenlikbildir.ui.fragment.options.addContacts

import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentAddContactsBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.requireReadContactsPermission
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_add_contacts.*

class AddContactsFragment: BaseFragment() {

    private lateinit var addContactsVM: AddContactsVM
    private lateinit var binding: FragmentAddContactsBinding

    @SuppressLint("InlinedApi")
    private val FROM_COLUMNS: Array<String> = arrayOf(
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        } else {
            ContactsContract.Contacts.DISPLAY_NAME
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_contacts, container,false)
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

                /*val contactPickerIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                startActivityForResult(contactPickerIntent, 1)*/

                launchMultiplePhonePicker()

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode){
            REQUEST_CODE_PICK_CONTACT -> {
                 if (resultCode == Activity.RESULT_OK) {
                     val bundle: Bundle = data!!.extras!!
                     val result = bundle.get("result")
                 }
             }
        }

    }

    val REQUEST_CODE_PICK_CONTACT = 1
    val MAX_PICK_CONTACT = 10

    private fun launchMultiplePhonePicker() {
        val phonebookIntent = Intent("intent.action.INTERACTION_TOPMENU")
        phonebookIntent.putExtra("additional", "phone-multi")
        phonebookIntent.putExtra("maxRecipientCount", MAX_PICK_CONTACT)
        phonebookIntent.putExtra("FromMMS", true)
        startActivityForResult(phonebookIntent, REQUEST_CODE_PICK_CONTACT)
    }

}
