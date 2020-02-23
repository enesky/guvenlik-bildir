package com.enesky.guvenlikbildir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.databinding.ItemContactBinding
import com.enesky.guvenlikbildir.extensions.makeItGone
import com.enesky.guvenlikbildir.model.Contact

/**
 * Created by Enes Kamil YILMAZ on 11.02.2020
 */

class ContactAdapter(private var contactList: List<Contact>,
                     private val contactListener: ContactListener,
                     private var isAddContactPage: Boolean = false)
    : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContactBinding.inflate(inflater, parent, false)
        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int = contactList.size

    override fun onBindViewHolder(holder: ContactViewHolder, pos: Int) = holder.bind(pos, contactList[pos])

    inner class ContactViewHolder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pos: Int, contact: Contact) {
            binding.contact = contact
            if (isAddContactPage)
                binding.ivDelete.makeItGone()
/*
                if (isAddContactPage) {
                binding.root.setOnClickListener {
                    binding.clContact.setBackground(R.color.green56)
                    mcontactListener.onItemClick(pos, contact)
                }
                binding.ivDelete.makeItGone()
            } else {
                binding.ivDelete.setOnClickListener {
                    mcontactListener.onDeleteClick(pos, contact)
                }
                binding.ivDelete.makeItVisible()
            }*/

            binding.executePendingBindings()
        }

    }

    fun update( items: MutableList<Contact>) {
        this.contactList = items
        notifyDataSetChanged()
    }

    interface ContactListener {
        fun onDeleteClick(pos: Int, contact: Contact)
        fun onItemClick(pos: Int, contact: Contact)
    }

}