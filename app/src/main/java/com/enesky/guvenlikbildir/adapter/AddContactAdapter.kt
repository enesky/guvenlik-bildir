package com.enesky.guvenlikbildir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.databinding.ItemContactBinding
import com.enesky.guvenlikbildir.model.Contact

/**
 * Created by Enes Kamil YILMAZ on 11.02.2020
 */

class AddContactAdapter(private var contactList: MutableList<Contact>,
                        private val addContactListener: AddContactListener)
    : RecyclerView.Adapter<AddContactAdapter.AddContactViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContactBinding.inflate(inflater, parent, false)
        return AddContactViewHolder(binding)
    }

    override fun getItemCount(): Int = contactList.size

    override fun onBindViewHolder(holder: AddContactViewHolder, pos: Int) = holder.bind(pos, contactList[pos])

    fun update( items: MutableList<Contact>) {
        this.contactList = items
        notifyDataSetChanged()
    }

    inner class AddContactViewHolder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int, contact: Contact) {
            binding.contact = contact
            binding.clContact.setOnClickListener {
                addContactListener.onItemClick(pos, contact)
            }
            binding.executePendingBindings()
        }
    }

    interface AddContactListener {
        fun onItemClick(pos: Int, contact: Contact)
    }
}