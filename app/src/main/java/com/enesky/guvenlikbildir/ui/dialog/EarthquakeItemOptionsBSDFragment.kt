package com.enesky.guvenlikbildir.ui.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.OptionAdapter
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.BottomSheetEarthquakeOptionsBinding
import com.enesky.guvenlikbildir.extensions.formatDateTime
import com.enesky.guvenlikbildir.extensions.showToast
import com.enesky.guvenlikbildir.model.OptionItem
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.base.BaseBottomSheetDialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.bottom_sheet_earthquake_options.*

class EarthquakeItemOptionsBSDFragment :
    BaseBottomSheetDialogFragment(),
    OptionAdapter.OptionListListener,
    OnMapReadyCallback {

    private var earthquake : Earthquake? = null
    private lateinit var binding: BottomSheetEarthquakeOptionsBinding
    private var earthquakeDetail = ""
    private var earthquakeDetailWithLinks = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_earthquake_options, container, false)

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

        App.mAnalytics.setCurrentScreen(activity!!, "bottom_sheet", this.javaClass.simpleName)

        var supportMapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment?
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction().apply {
                add(R.id.mapContainer, supportMapFragment, "mapContainer")
                commit()
            }
            childFragmentManager.executePendingTransactions()
        }
        supportMapFragment?.getMapAsync(this)

        earthquakeDetail =
                "# Deprem - Kandilli Rasathanesi #\n" +
                "Yer: ${earthquake!!.location}\n" +
                "Büyüklük: ${earthquake!!.mag}\n" +
                "Derinlik: ${earthquake!!.depth} km\n" +
                "Tarih: " + earthquake!!.dateTime.formatDateTime() + " TSİ\n"

        earthquakeDetailWithLinks =
                "# Deprem - Kandilli Rasathanesi #\n" +
                "Yer: ${earthquake!!.location}\n" +
                "Büyüklük: ${earthquake!!.mag}\n" +
                "Derinlik: ${earthquake!!.depth} km\n" +
                "Tarih: " + earthquake!!.dateTime.formatDateTime() + " TSİ\n" +
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

        rv_options.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = optionListAdapter
        }

    }

    override fun onItemClick(pos: Int, optionItem: OptionItem) {
        when(pos) {
            0 -> requireContext().showToast("Yapım aşamasındadır.", false)
            1 -> requireContext().showToast("Yapım aşamasındadır.", false)
            2 -> {
                val share = Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, earthquakeDetailWithLinks)
                    putExtra(Intent.EXTRA_TITLE, "Deprem Detayları")
                    type = "text/plain"
                }, null)
                startActivity(share)
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

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap == null
            || earthquake?.lat.isNullOrEmpty()
            || earthquake?.lng.isNullOrEmpty()) return

        val loc = LatLng(earthquake!!.lat.toDouble(), earthquake!!.lng.toDouble())

        val color = when {
            earthquake!!.mag < 3 -> "#388e3c" to "#66388e3c"
            (earthquake!!.mag >= 3) && (earthquake!!.mag < 4.5) -> "#f9aa33" to "#66f9aa33"
            else -> "#e53935" to "#66e53935"
        }

        val circleRadius = earthquake!!.mag * 10000.0

        val circleOptions = CircleOptions()
            .center(loc)
            .radius(circleRadius)
            .strokeWidth(1f)
            .strokeColor(Color.parseColor(color.first))
            .fillColor(Color.parseColor(color.second))

        googleMap.addCircle(circleOptions)

        googleMap.setOnMapClickListener {}
        googleMap.setOnMarkerClickListener { true }

        googleMap.setOnMapLoadedCallback {
            googleMap.addMarker(MarkerOptions().position(loc))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 8f))
        }
    }

    companion object {
        private const val earthquakeTag = "earthquakeTag"

        fun newInstance(earthquake: Earthquake): EarthquakeItemOptionsBSDFragment {
            val args = Bundle()
            args.putParcelable(earthquakeTag, earthquake)
            val fragment = EarthquakeItemOptionsBSDFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
