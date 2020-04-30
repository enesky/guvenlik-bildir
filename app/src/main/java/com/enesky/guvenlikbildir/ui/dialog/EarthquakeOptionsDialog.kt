package com.enesky.guvenlikbildir.ui.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.OptionAdapter
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.DialogEarthquakeBottomSheetBinding
import com.enesky.guvenlikbildir.extensions.showToast
import com.enesky.guvenlikbildir.model.OptionItem
import com.enesky.guvenlikbildir.others.Constants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_earthquake_bottom_sheet.*
import java.text.SimpleDateFormat

class EarthquakeOptionsDialog : BottomSheetDialogFragment(), OptionAdapter.OptionListListener {

    companion object {
        private const val earthquakeTag = "earthquakeTag"

        fun newInstance(earthquake: Earthquake): EarthquakeOptionsDialog {
            val args = Bundle()
            args.putParcelable(earthquakeTag, earthquake)
            val fragment = EarthquakeOptionsDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private var earthquake : Earthquake? = null
    private lateinit var binding: DialogEarthquakeBottomSheetBinding
    private var earthquakeDetail = ""
    private var earthquakeDetailWithLinks = ""

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        setupOnShowListener()
        return bottomSheetDialog as BottomSheetDialog
    }

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
                "Tarih: " + formatDateTime(earthquake!!.dateTime) + " TSİ\n"

        earthquakeDetailWithLinks =
                "# Deprem - Kandilli Rasathanesi #\n" +
                "Yer: ${earthquake!!.location}\n" +
                "Büyüklük: ${earthquake!!.magML}\n" +
                "Derinlik: ${earthquake!!.depth} km\n" +
                "Tarih: " + formatDateTime(earthquake!!.dateTime) + " TSİ\n"
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

    override fun onSaveInstanceState(outState: Bundle) {
        //No call for super(). Bug on API Level > 11.
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            try {
                super.show(manager, tag)
            } catch (e: IllegalStateException) {
                manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
            }
        }
    }

    private fun setupOnShowListener() {

        bottomSheetDialog!!.setOnShowListener { dialog ->

            val frameLayout =
                (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

            if (frameLayout != null) {
                frameLayout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                bottomSheetBehavior!!.skipCollapsed = true
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            }

        }
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

    fun formatDateTime(dateTime: String): String {
        val date = SimpleDateFormat(Constants.DEFAULT_K_DATE_TIME_FORMAT).parse(dateTime)
        return SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(date!!)
    }

}
