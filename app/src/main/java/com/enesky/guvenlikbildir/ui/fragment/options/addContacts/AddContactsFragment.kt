package com.enesky.guvenlikbildir.ui.fragment.options.addContacts

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment

class AddContactsFragment: BaseFragment() {

    private lateinit var viewModel: AddContactsVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_contacts, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddContactsVM::class.java)
        // TODO: Use the ViewModel
    }

}
