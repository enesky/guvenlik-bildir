package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.adapter.EarthquakePagingAdapter
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.FragmentLatestEarthquakesBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.google.android.gms.maps.model.LatLng
import com.hadilq.liveevent.LiveEvent

class LatestEarthquakesVM : BaseViewModel(), EarthquakePagingAdapter.EarthquakeItemListener{

    val filterIndex = MutableLiveData(0)

    val whereTo = LiveEvent<Any>()
    val onFilterIndexChange = LiveEvent<Int>()

    fun init(binding: FragmentLatestEarthquakesBinding) {
        setViewDataBinding(binding)
    }

    override fun onOptionsClick(earthquake: Earthquake) {
        whereTo.value = earthquake
    }

    override fun onMapClick(earthquake: Earthquake) {
        whereTo.value = earthquake
    }

    fun onFilterIndexChange(newIndex: Int) {
        onFilterIndexChange.value = newIndex
        filterIndex.value = newIndex
    }

}