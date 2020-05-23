package com.enesky.guvenlikbildir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.database.entity.ContactStatus
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
import com.enesky.guvenlikbildir.database.repo.SmsReportRepository
import com.enesky.guvenlikbildir.databinding.ItemSmsReportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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

    fun addOneByOne(smsReport: SmsReport,
                    smsReportRepository: SmsReportRepository) {
        GlobalScope.launch(Dispatchers.Main) {
            smsReport.contactReportList.forEachIndexed { index, contactStatus ->
                delay(1000)
                val smsReportStatus = when (Random.nextInt(0,4) % 4) {
                    1 -> SmsReportStatus.SUCCESS
                    2 -> SmsReportStatus.FAILED
                    3 -> SmsReportStatus.DELIVERED
                    else -> SmsReportStatus.IN_QUEUE
                }
                updateItem(
                    smsReport =  smsReport,
                    updatedContactStatus = contactStatus,
                    newStatus =  smsReportStatus,
                    smsReportRepository =  smsReportRepository
                )
            }
        }
    }

    fun updateItem(smsReport: SmsReport,
                   updatedContactStatus: ContactStatus,
                   newStatus: SmsReportStatus,
                   smsReportRepository: SmsReportRepository): Boolean {
        var updatedItemIndex = -1
        contactStatusList.forEachIndexed { index, contactStatus ->
            if (updatedContactStatus.contact == contactStatus.contact) {
                updatedItemIndex = index
                contactStatusList.toMutableList()[index] = updatedContactStatus
                smsReportRepository.updateReport(smsReport, updatedContactStatus, newStatus)
            }
        }

        return if (updatedItemIndex != -1) {
            notifyItemChanged(updatedItemIndex)
            true
        } else
            false
    }

}