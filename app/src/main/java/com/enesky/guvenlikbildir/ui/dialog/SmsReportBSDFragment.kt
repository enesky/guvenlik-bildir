package com.enesky.guvenlikbildir.ui.dialog

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
import com.enesky.guvenlikbildir.databinding.BottomSheetSmsReportBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.SmsAPI
import com.enesky.guvenlikbildir.others.lastKnownLocation
import com.enesky.guvenlikbildir.ui.base.BaseBottomSheetDialogFragment
import com.enesky.guvenlikbildir.ui.fragment.options.smsReports.SmsReportVM
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

private const val SAFE_SMS = "isSafeSmsTag"
private const val HISTORY_TAG = "isHistoryTag"
private const val VISIBLE_RECYCLER_VIEW_ITEM = 3

class SmsReportBSDFragment : BaseBottomSheetDialogFragment(), OnMapReadyCallback, SmsAPI.SmsApiListener {

    private lateinit var binding: BottomSheetSmsReportBinding
    private lateinit var smsReportVM: SmsReportVM

    private var smsReport: SmsReport? = null
    private var isSafeSms: Boolean = false
    private var isHistory: Boolean = false
    private var googleMap: GoogleMap? = null
    private var contactList: List<Contact> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_sms_report, container, false)
        smsReportVM = getViewModel()
        binding.lifecycleOwner = this@SmsReportBSDFragment
        binding.viewModel = smsReportVM
        smsReportVM.init(binding)

        if (arguments != null) {
            isSafeSms = arguments!!.getBoolean(SAFE_SMS)
            isHistory = arguments!!.getBoolean(HISTORY_TAG)
            GlobalScope.launch(Dispatchers.Main) {
                if (isHistory && smsReportVM.smsReport.value != null) {
                    smsReport = smsReportVM.smsReport.value
                    smsReportVM.smsReportAdapter.value?.update(smsReport!!)
                } else {
                    smsReport = smsReportVM.smsReportRepository.createReport(isSafeSms, contactList, false)
                }
                binding.smsReport = smsReport
            }
        } else {
            dismiss()
        }
        return binding.root
    }

    @SuppressLint("MissingPermission")
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
        mapContainer.setViewParent(cl_sheet)

        cv_sms_preview.setOnLongClickListener {
            val myClipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val myClip: ClipData = ClipData.newPlainText(getString(R.string.app_name), smsReport?.sentSms)
            myClipboard.setPrimaryClip(myClip)
            context!!.showToast("Panoya Kopyalandı.", false)
            true
        }

        if (isHistory) {
            setRecyclerViewHeight(true)
            cv_confirm.makeItGone()
            tv_title_sms_preview.text = getString(R.string.label_sent_sms_preview)
            tv_title_last_known_loc.text = getString(R.string.label_location_sent)
        } else {
            cv_confirm.setOnClickListener {
                cv_confirm.isEnabled = false
                activity!!.requireSendSmsPermission {
                    setUncancellable(true)

                    GlobalScope.launch(Dispatchers.Main) {
                        smsReport = smsReportVM.smsReportRepository.createReport(isSafeSms, contactList, true)
                        smsReportVM.updateSmsReport(smsReport!!)

                        SmsAPI.instance.sendSMS(
                            isSafe = isSafeSms,
                            smsApiListener = this@SmsReportBSDFragment
                        )
                    }

                    googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                    googleMap?.isMyLocationEnabled = false
                    tv_sending.text = getString(R.string.label_sending)
                    dots.makeItVisible()
                    setRecyclerViewHeight(true)
                }
                refreshUi()
            }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        smsReportVM.smsReport.observe(viewLifecycleOwner, Observer {
            if (it != null && !isHistory) {
                smsReport = it
                binding.smsReport = it
                setRecyclerViewHeight(false)
            }
        })

        smsReportVM.getSelectedContactList().observe(viewLifecycleOwner, Observer {
            GlobalScope.launch(Dispatchers.Main) {
                if (!isHistory) {
                    contactList = it
                    smsReport = smsReportVM.smsReportRepository.createReport(isSafeSms, contactList, false)
                    smsReportVM.smsReportAdapter.value?.update(smsReport!!)
                }
            }
        })

    }

    override fun onStatusChange(contact: Contact?, status: SmsReportStatus) {
        val index = smsReportVM.smsReportAdapter.value?.updateItem(contact, status)
        if (index != null && index != -1 && status == SmsReportStatus.DELIVERED)
            rv_sms_report?.smoothScrollToPosition(index)
    }

    override fun processFinished() {
        if (isVisible) {
            setUncancellable(false)
            tv_sending?.text = getString(R.string.label_sent)
            dots?.makeItGone()
            refreshUi()
        }
    }

    override fun dismiss() {
        super.dismiss()
        SmsAPI.instance.onDestroy()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
        if (googleMap == null) return

        pb_map.makeItGone()

        val lastKnownLoc =
            if (isHistory) smsReport!!.lastKnownLocation.split(",")
            else lastKnownLocation!!.split(",")

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
        googleMap!!.setOnMarkerClickListener { true }
    }

    private fun setRecyclerViewHeight(makeItVisible : Boolean) {
        val list = smsReportVM.smsReport.value?.contactReportList
        val itemHeight = smsReportVM.smsReportAdapter.value?.itemHeight

        if (itemHeight != 0 && list != null && list.size > VISIBLE_RECYCLER_VIEW_ITEM)
            rv_sms_report?.layoutParams?.height = itemHeight!! * VISIBLE_RECYCLER_VIEW_ITEM
        if (makeItVisible)
            rv_sms_report?.makeItVisible()
    }

    companion object {
        fun newInstance(isHistory: Boolean, isSafeSms: Boolean? = null) =
            SmsReportBSDFragment().apply {
                arguments = Bundle().apply {
                    if (isSafeSms != null)
                        putBoolean(SAFE_SMS, isSafeSms)
                    putBoolean(HISTORY_TAG, isHistory)
                }
            }
    }

}
