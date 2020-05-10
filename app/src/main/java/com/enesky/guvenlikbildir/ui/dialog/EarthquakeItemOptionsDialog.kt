package com.enesky.guvenlikbildir.ui.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.OptionAdapter
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.DialogEarthquakeBottomSheetBinding
import com.enesky.guvenlikbildir.extensions.formatDateTime
import com.enesky.guvenlikbildir.extensions.showToast
import com.enesky.guvenlikbildir.model.OptionItem
import com.enesky.guvenlikbildir.others.Constants
import kotlinx.android.synthetic.main.dialog_earthquake_bottom_sheet.*
import java.text.SimpleDateFormat

class EarthquakeItemOptionsDialog : BaseBottomSheetDialogFragment(), OptionAdapter.OptionListListener {

    companion object {
        private const val earthquakeTag = "earthquakeTag"

        fun newInstance(earthquake: Earthquake): EarthquakeItemOptionsDialog {
            val args = Bundle()
            args.putParcelable(earthquakeTag, earthquake)
            val fragment = EarthquakeItemOptionsDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private var earthquake : Earthquake? = null
    private lateinit var binding: DialogEarthquakeBottomSheetBinding
    private var earthquakeDetail = ""
    private var earthquakeDetailWithLinks = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_earthquake_bottom_sheet, container, false)

        if (arguments != null) {
            earthquake = arguments?.getParcelable(earthquakeTag)
            binding.earthquake = earthquake
        } else {
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        earthquakeDetail =
                "# Deprem - Kandilli Rasathanesi #\n" +
                "Yer: ${earthquake!!.location}\n" +
                "Büyüklük: ${earthquake!!.magML}\n" +
                "Derinlik: ${earthquake!!.depth} km\n" +
                "Tarih: " + earthquake!!.dateTime.formatDateTime() + " TSİ\n"

        earthquakeDetailWithLinks =
                "# Deprem - Kandilli Rasathanesi #\n" +
                "Yer: ${earthquake!!.location}\n" +
                "Büyüklük: ${earthquake!!.magML}\n" +
                "Derinlik: ${earthquake!!.depth} km\n" +
                "Tarih: " + earthquake!!.dateTime.formatDateTime() + " TSİ\n"
                "https://maps.google.com/maps?q=${earthquake!!.lat},${earthquake!!.lng}&ll=${earthquake!!.lat},${earthquake!!.lng}&&z=8\n" +
                "# Güvenlik Bildir #\n" +
                Constants.googlePlayUrl

        tv_earthquake_details.text = earthquakeDetail

        val optionListAdapter = OptionAdapter(
            listOf(
                OptionItem(R.drawable.ic_thumb_up_white_24dp, getString(R.string.label_felt_it), android.R.color.white),
                OptionItem(R.drawable.ic_thumb_down_white_24dp, getString(R.string.label_not_felt_it), android.R.color.white),
                OptionItem(R.drawable.ic_share, getString(R.string.label_share), android.R.color.white),
                OptionItem(R.drawable.ic_content_copy, getString(R.string.label_copy), android.R.color.white)
            ), this
        )

        rv_options.layoutManager = LinearLayoutManager(requireContext())
        rv_options.setHasFixedSize(true)
        rv_options.adapter = optionListAdapter

    }


    override fun onItemClick(pos: Int, optionItem: OptionItem) {
        when(pos) {
            0 -> requireContext().showToast("Yapım aşamasındadır.", false)
            1 -> requireContext().showToast("Yapım aşamasındadır.", false)
            2 -> {
                val sendIntent: Intent = Intent().setAction(Intent.ACTION_SEND)
                sendIntent.putExtra(Intent.EXTRA_TEXT, earthquakeDetailWithLinks)
                sendIntent.type = "text/plain"
                if (sendIntent.resolveActivity(requireContext().packageManager) != null)
                    requireContext().startActivity(sendIntent)
                dismissAllowingStateLoss()
            }
            3 -> {
                val myClipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val myClip: ClipData = ClipData.newPlainText("Deprem Detayları", earthquakeDetailWithLinks)
                myClipboard.setPrimaryClip(myClip)
                requireContext().showToast("Panoya Kopyalandı.", false)
                dismissAllowingStateLoss()
            }
        }
    }


}
