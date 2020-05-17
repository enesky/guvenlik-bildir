package com.enesky.guvenlikbildir.ui.fragment.options.modifySms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentModifySmsBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.google.android.gms.location.LocationServices
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
import kotlinx.coroutines.withContext
import java.util.*

class ModifySMSFragment: BaseFragment(), OnMapReadyCallback {

    private lateinit var modifySmsVM: ModifySmsVM
    private lateinit var binding: FragmentModifySmsBinding
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_modify_sms, container,false)
        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)

        GlobalScope.launch(Dispatchers.Default) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            withContext(Dispatchers.Main) {
                mapFragment.getMapAsync(this@ModifySMSFragment)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        modifySmsVM = getViewModel()
        binding.apply {
            viewModel = modifySmsVM
            lifecycleOwner = this@ModifySMSFragment
        }
        modifySmsVM.init(binding)

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
        }, 0,2000)
    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
        if (googleMap == null) return

        val latlng = lastKnownLocation!!.split(",")
        val loc = LatLng(latlng[0].toDouble(), latlng[1].toDouble())

        activity!!.requireLocationPermission {
            googleMap!!.isMyLocationEnabled = true
            googleMap!!.uiSettings.setMyLocationButtonEnabled(true)
        }

        googleMap!!.setOnMapClickListener {
            openInfoCountDownDialog(Constants.locationMapLink)
        }

        googleMap!!.setOnMapLoadedCallback {
            googleMap!!.addMarker(MarkerOptions().position(loc))
            googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17f))
        }
    }

}
