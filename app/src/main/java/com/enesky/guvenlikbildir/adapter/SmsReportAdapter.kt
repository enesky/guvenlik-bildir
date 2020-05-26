package com.enesky.guvenlikbildir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.ContactStatus
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
import com.enesky.guvenlikbildir.databinding.ItemSmsReportBinding

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

class SmsReportAdapter(private var contactStatusList: MutableList<ContactStatus> = mutableListOf())
: RecyclerView.Adapter<SmsReportAdapter.SmsReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsReportViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSmsReportBinding.inflate(inflater, parent, false)
        return SmsReportViewHolder(binding)
    }

    override fun getItemCount(): Int = contactStatusList.size

    override fun onBindViewHolder(holder: SmsReportViewHolder, pos: Int) = holder.bind()

    inner class SmsReportViewHolder(
        private val binding: ItemSmsReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.contactStatus = contactStatusList[adapterPosition]
            binding.executePendingBindings()
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    fun update(smsReport: SmsReport) {
        this.contactStatusList = smsReport.contactReportList.toMutableList()
        notifyDataSetChanged()
    }

    fun updateItem(contact: Contact?, status: SmsReportStatus): Boolean {

        if (contact == null) return false

        var updatedItemIndex = -1
        contactStatusList.forEachIndexed { index, contactStatus ->
            if (contactStatus.contact == contact) {
                updatedItemIndex = index
                contactStatusList.toMutableList()[index].smsReportStatus = status
            }
        }

        return if (updatedItemIndex != -1) {
                    notifyItemChanged(updatedItemIndex)
                    true
               } else
                    false
    }

}