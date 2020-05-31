package com.enesky.guvenlikbildir.ui.fragment.options.modifySms

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentModifySmsBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.*
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_modify_sms.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ModifySMSFragment: BaseFragment(), OnMapReadyCallback {

    private lateinit var modifySmsVM: ModifySmsVM
    private lateinit var binding: FragmentModifySmsBinding
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_modify_sms, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)

        modifySmsVM = getViewModel()
        modifySmsVM.init(binding)
        binding.viewModel = modifySmsVM
        binding.lifecycleOwner = this

        var supportMapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment?
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction().apply {
                add(R.id.mapContainer, supportMapFragment, "mapContainer")
                commit()
            }
            childFragmentManager.executePendingTransactions()
            mapContainer.setViewParent(nsv_sheet)
        }
        supportMapFragment?.getMapAsync(this)

        et_safe_sms.setText(safeSms)
        et_unsafe_sms.setText(unsafeSms)

        tv_safe_location.setOnClickListener {
            openInfoCountDownDialog(Constants.locationMapLink)
        }

        tv_unsafe_location.setOnClickListener {
            openInfoCountDownDialog(Constants.locationMapLink)
        }

        btn_save.setOnClickListener {
            safeSms = et_safe_sms.text.toString()
            modifySmsVM.setSafeSms(safeSms!!)

            unsafeSms = et_unsafe_sms.text.toString()
            modifySmsVM.setUnsafeSms(unsafeSms!!)

            activity!!.onBackPressed()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Timer().schedule(kotlin.concurrent.timerTask {
            modifySmsVM.lastLocation.postValue(locationMapWithLink)

            GlobalScope.launch(Dispatchers.Main) {
                if (googleMap != null) {
                    val latlng = lastKnownLocation!!.split(",")
                    val loc = LatLng(latlng[0].toDouble(), latlng[1].toDouble())
                    googleMap!!.clear()
                    googleMap!!.addMarker(MarkerOptions().position(loc))
                }
            }
        }, 0, Constants.MIN_TIME_BW_LOCATION_UPDATE)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
        if (googleMap == null) return

        pb_loading.makeItGone()

        val latlng = lastKnownLocation!!.split(",")
        val loc = LatLng(latlng[0].toDouble(), latlng[1].toDouble())

        activity!!.requireLocationPermission {
            googleMap!!.isMyLocationEnabled = true
            googleMap!!.uiSettings.setMyLocationButtonEnabled(true)
        }

        googleMap!!.setOnMapClickListener {}
        googleMap!!.setOnMarkerClickListener { true }

        googleMap!!.setOnMapLoadedCallback {
            googleMap!!.addMarker(MarkerOptions().position(loc))
            googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17f))
        }
    }

}
