package com.enesky.guvenlikbildir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.databinding.ItemSmsReportBinding

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

class SmsReportAdapter(private var smsReport: SmsReport? = null)
: RecyclerView.Adapter<SmsReportAdapter.SmsReportViewHolder>() {

    private var smsReportList = smsReport?.contactReportList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsReportViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSmsReportBinding.inflate(inflater, parent, false)
        return SmsReportViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (smsReport == null)
            0
        else
            smsReport!!.contactReportList.size
    }

    override fun onBindViewHolder(holder: SmsReportViewHolder, pos: Int) = holder.bind(pos)

    inner class SmsReportViewHolder(
        private val binding: ItemSmsReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int) {

            //TODO: Fill here

            binding.executePendingBindings()
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    fun update(smsReport: SmsReport) {
        this.smsReportList = smsReport.contactReportList
        notifyDataSetChanged()
    }
}