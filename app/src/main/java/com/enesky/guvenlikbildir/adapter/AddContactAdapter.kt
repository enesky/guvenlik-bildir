package com.enesky.guvenlikbildir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ItemContactBinding
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.extensions.setBackground
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 11.02.2020
 */

class AddContactAdapter(private var contactList: List<Contact>,
                        private val addContactListener: AddContactListener)
    : RecyclerView.Adapter<AddContactAdapter.AddContactViewHolder>() {

    var selectedPosList: HashSet<Int> = hashSetOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContactBinding.inflate(inflater, parent, false)
        return AddContactViewHolder(binding)
    }

    override fun getItemCount(): Int = contactList.size

    override fun onBindViewHolder(holder: AddContactViewHolder, pos: Int) = holder.bind(contactList[pos])

    fun update(items: List<Contact>) {
        this.contactList = items
        notifyDataSetChanged()
    }

    inner class AddContactViewHolder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.contact = contact
            binding.root.setOnClickListener {
                Timber.tag("AddContactAdapter").d( "onClick: %s", adapterPosition)

                if (selectedPosList.contains(adapterPosition)) {
                    it.setBackground(android.R.color.white)
                    selectedPosList.remove(adapterPosition)
                } else {
                    it.setBackground(R.color.fern)
                    selectedPosList.add(adapterPosition)
                }

                addContactListener.onItemClick(adapterPosition, contactList[adapterPosition])
            }
            binding.executePendingBindings()
        }

    }

    override fun getItemId(position: Int): Long = position.toLong()

    interface AddContactListener {
        fun onItemClick(pos: Int, contact: Contact)
    }

}