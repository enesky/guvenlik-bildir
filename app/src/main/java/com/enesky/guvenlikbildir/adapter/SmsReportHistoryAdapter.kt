package com.enesky.guvenlikbildir.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
import com.enesky.guvenlikbildir.databinding.ItemSmsReportHistoryBinding
import com.enesky.guvenlikbildir.extensions.getString
import com.enesky.guvenlikbildir.extensions.getStrings
import com.enesky.guvenlikbildir.others.Constants
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

class SmsReportHistoryAdapter(
    private var smsReportList: List<SmsReport>?,
    private val smsReportHistoryListener: SmsReportHistoryListener)
: RecyclerView.Adapter<SmsReportHistoryAdapter.SmsReportHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsReportHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSmsReportHistoryBinding.inflate(inflater, parent, false)
        return SmsReportHistoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (smsReportList == null)
            0
        else
            smsReportList!!.size
    }

    override fun onBindViewHolder(holder: SmsReportHistoryViewHolder, pos: Int) = holder.bind(pos, smsReportList!![pos])

    inner class SmsReportHistoryViewHolder(
        private val binding: ItemSmsReportHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int, smsReport: SmsReport) {

            binding.tvReportType.text =
                if (smsReport.isSafeSms)
                    getStrings(R.string.label_sms_report_type, R.string.label_im_safe)
                else
                    getStrings(R.string.label_sms_report_type, R.string.label_im_not_safe)

            binding.tvReportSuccessRate.text = calculateSuccessRate(smsReport)
            binding.tvDate.text = getString(R.string.label_report_date, smsReport.sendingDate)
            binding.tvShortDate.text = getShortDate(smsReport.sendingDate)

            binding.clHistoryItem.setOnClickListener {
                smsReportHistoryListener.onItemClick(pos, smsReport)
            }
            binding.executePendingBindings()
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    fun update(items: List<SmsReport>) {
        this.smsReportList = items
        notifyDataSetChanged()
    }

    @SuppressLint("SimpleDateFormat")
    fun getShortDate(reportDate: String): String {
        val date = SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).parse(reportDate)
        val p = PrettyTime()
        p.locale = Locale("tr")
        return p.format(date)
    }

    fun calculateSuccessRate(smsReport: SmsReport): String {
        var successStatusCount = 0
        for (contactStatus in smsReport.contactReportList)
            if ((contactStatus.smsReportStatus == SmsReportStatus.SUCCESS) ||
                (contactStatus.smsReportStatus == SmsReportStatus.DELIVERED))
                successStatusCount++

        val percentage = (successStatusCount.toDouble() / smsReport.contactReportList.size) * 100

        return getString(R.string.label_sms_report_success_rate) + percentage.toString()
    }

    interface SmsReportHistoryListener {
        fun onItemClick(pos: Int, smsReport: SmsReport)
    }

}