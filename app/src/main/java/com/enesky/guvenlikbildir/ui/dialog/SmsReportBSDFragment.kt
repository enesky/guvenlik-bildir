package com.enesky.guvenlikbildir.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.databinding.BottomSheetSmsReportBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.lastKnownLocation
import com.enesky.guvenlikbildir.others.locationMapWithLink
import com.enesky.guvenlikbildir.others.safeSms
import com.enesky.guvenlikbildir.others.unsafeSms
import com.enesky.guvenlikbildir.ui.base.BaseBottomSheetDialogFragment
import com.enesky.guvenlikbildir.ui.fragment.options.smsReports.SmsReportVM
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.bottom_sheet_sms_report.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Enes Kamil YILMAZ on 18.05.2020
 */

class SmsReportBSDFragment : BaseBottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var binding: BottomSheetSmsReportBinding
    private lateinit var smsReportVM: SmsReportVM

    private var smsReport: SmsReport? = null
    private var isSafeSms: Boolean = false
    private var isHistory: Boolean = false
    private var googleMap: GoogleMap? = null
    private var contactList: List<Contact> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_sms_report, container, false)
        smsReportVM = getViewModel()
        binding.lifecycleOwner = this@SmsReportBSDFragment
        binding.viewModel = smsReportVM
        smsReportVM.init(binding)

        if (arguments != null) {
            isSafeSms = arguments!!.getBoolean(isSafeSmsTag)
            isHistory = arguments!!.getBoolean(isHistoryTag)

            if (!isHistory) {
                if (isSafeSms)
                    smsReportVM.smsLive.postValue(safeSms + locationMapWithLink)
                else
                    smsReportVM.smsLive.postValue(unsafeSms + locationMapWithLink)

                smsReportVM.locationLive.postValue(lastKnownLocation)
            } else {
                if (smsReportVM.smsReport.value != null) {

                    smsReport = smsReportVM.smsReport.value
                    smsReportVM.smsReportAdapter.value?.update(smsReport!!)
                    binding.smsReport = smsReport

                    smsReportVM.smsLive.postValue(smsReport!!.sentSms)
                    smsReportVM.locationLive.postValue(smsReport!!.lastKnownLocation)
                }
            }

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

        if (isHistory) {
            btn_approve.makeItGone()
            tv_title_sms_preview.text = getString(R.string.label_history_sent_sms)
            tv_title_last_known_loc.text = getString(R.string.label_location_sent)
        }

        GravitySnapHelper(Gravity.TOP).attachToRecyclerView(rv_sms_report)
        rv_sms_report.updateRecyclerViewAnimDuration()

        btn_approve.setOnClickListener {
            btn_approve.makeItGone()
            ll_sending.makeItVisible()

            GlobalScope.launch(Dispatchers.Main) {
                smsReport = smsReportVM.smsReportRepository.createReport(isSafeSms, contactList, true)
                smsReportVM.updateSmsReport(smsReport!!)
            }

            googleMap!!.uiSettings.isMyLocationButtonEnabled = false
            googleMap!!.isMyLocationEnabled = false
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        smsReportVM.smsReport.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                smsReportVM.smsReportAdapter.value!!.addOneByOne(it, smsReportVM.smsReportRepository)
                binding.smsReport = it
            }
        })

        smsReportVM.getSelectedContactList().observe(viewLifecycleOwner, Observer {
            contactList = it
            GlobalScope.launch(Dispatchers.Main) {
                val tempSmsReport = smsReportVM.smsReportRepository.createReport(isSafeSms, contactList, false)
                smsReportVM.smsReportAdapter.value!!.update(tempSmsReport)
            }
        })

    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
        if (googleMap == null) return

        pb_map.makeItGone()

        val lastKnownLoc =
            if (!isHistory) lastKnownLocation!!.split(",")
            else smsReport!!.lastKnownLocation.split(",")
        val loc = LatLng(lastKnownLoc[0].toDouble(), lastKnownLoc[1].toDouble())

        if (!isHistory) {
            activity!!.requireLocationPermission {
                googleMap!!.isMyLocationEnabled = true
                googleMap!!.uiSettings.setMyLocationButtonEnabled(true)
            }

            googleMap!!.setOnMyLocationButtonClickListener {
                if (googleMap != null) {
                    val locSplit = lastKnownLocation!!.split(",")
                    val lastLatLng = LatLng(locSplit[0].toDouble(), locSplit[1].toDouble())
                    googleMap!!.clear()
                    googleMap!!.addMarker(MarkerOptions().position(lastLatLng))
                    googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 18f))
                }
                true
            }
        }

        googleMap!!.setOnMapLoadedCallback {
            googleMap!!.addMarker(MarkerOptions().position(loc))
            googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18f))
        }

        googleMap!!.setOnMapClickListener { }
    }

    companion object {
        private const val isSafeSmsTag = "isSafeSmsTag"
        private const val isHistoryTag = "isHistoryTag"

        fun newInstance(isHistory:Boolean, isSafeSms: Boolean? = null): SmsReportBSDFragment {
            val args = Bundle()
            if (isSafeSms != null)
                args.putBoolean(isSafeSmsTag, isSafeSms)
            args.putBoolean(isHistoryTag, isHistory)
            val fragment = SmsReportBSDFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
