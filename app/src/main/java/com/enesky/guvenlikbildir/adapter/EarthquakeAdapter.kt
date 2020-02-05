package com.enesky.guvenlikbildir.adapter

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.databinding.ItemEarthquakeBinding
import com.enesky.guvenlikbildir.extensions.Constants
import com.enesky.guvenlikbildir.extensions.makeItGone
import com.enesky.guvenlikbildir.extensions.makeItInvisible
import com.enesky.guvenlikbildir.extensions.makeItVisible
import com.enesky.guvenlikbildir.model.EarthquakeOA
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

class EarthquakeAdapter(
    private var earthquakeList: MutableList<EarthquakeOA>,
    private val earthquakeListener: EarthquakeListener
) : RecyclerView.Adapter<EarthquakeAdapter.EarthquakeViewHolder>(), Filterable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarthquakeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEarthquakeBinding.inflate(inflater, parent, false)
        return EarthquakeViewHolder(binding)
    }

    override fun getItemCount(): Int = earthquakeList.size

    override fun onBindViewHolder(holder: EarthquakeViewHolder, pos: Int) = holder.bind(pos, earthquakeList[pos])

    inner class EarthquakeViewHolder(private val binding: ItemEarthquakeBinding) :
        RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {

        var map: GoogleMap? = null
        var mEarthquakeOA: EarthquakeOA ?= null

        fun bind(pos: Int, earthquakeOA: EarthquakeOA) {
            binding.earthquake = earthquakeOA
            mEarthquakeOA = earthquakeOA
            binding.cvEarthquake.setOnClickListener {
                earthquakeListener.onItemClick(pos, earthquakeOA)

                if (binding.cvMap.isVisible) {
                    ObjectAnimator.ofFloat(binding.ivShowMap, "rotation", 180f, 0f).apply {
                        duration = Constants.defaultAnimationDuration
                        this.addListener(object: Animator.AnimatorListener{
                            override fun onAnimationEnd(p0: Animator?) {
                                binding.cvMap.makeItInvisible()
                                map!!.clear()
                                map!!.mapType = GoogleMap.MAP_TYPE_NONE
                            }
                            override fun onAnimationRepeat(p0: Animator?) {}
                            override fun onAnimationCancel(p0: Animator?) {}
                            override fun onAnimationStart(p0: Animator?) {}
                        })
                        start()
                    }
                    binding.cvMap.collapseView()

                } else {
                    ObjectAnimator.ofFloat(binding.ivShowMap, "rotation", 0f, 180f)
                        .setDuration(Constants.defaultAnimationDuration)
                        .start()

                    setupMap(map!!, earthquakeOA)

                    binding.cvMap.makeItVisible()
                    binding.cvMap.expandView(250)
                }

            }

            if (map != null) {
               setupMap(map!!, earthquakeOA)
            }

            binding.map.onCreate(null)
            binding.map.onResume()
            binding.map.getMapAsync(this)
            binding.executePendingBindings()
        }

        override fun onMapReady(googleMap: GoogleMap?) {
            MapsInitializer.initialize(binding.root.context)
            map = googleMap
            setupMap(map!!, mEarthquakeOA!!)
        }
    }

    override fun onViewRecycled(holder: EarthquakeViewHolder) {
        if (holder.map != null) {
            holder.map!!.clear()
            holder.map!!.mapType = GoogleMap.MAP_TYPE_NONE
        }
    }

    fun setupMap(map: GoogleMap, earthquake: EarthquakeOA) {
        val loc: LatLng? =
            if (earthquake.lat != null && earthquake.lng != null)
                LatLng(earthquake.lat.toDouble(), earthquake.lng.toDouble())
            else
                LatLng(earthquake.coordinates[0].toDouble(), earthquake.coordinates[1].toDouble())

        map.setOnMapClickListener {
            earthquakeListener.onMapClick(loc!!, earthquake.title)
        }

        map.addMarker(MarkerOptions().position(loc!!))
        map.moveCamera(CameraUpdateFactory.newLatLng(loc))
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isScrollGesturesEnabled = false
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    override fun getFilter(): Filter {
        return filter
    }

    fun update(items: MutableList<EarthquakeOA>) {
        this.earthquakeList = items
        notifyDataSetChanged()
    }

    private val filter: Filter = object : Filter() {
        @SuppressLint("DefaultLocale")
        override fun performFiltering(constraint: CharSequence): FilterResults {

            val returnList = FilterResults()
            val filteredList: MutableList<EarthquakeOA> = mutableListOf()

            if (constraint.isEmpty())
                returnList.values = earthquakeList
            else {
                val filterPattern = constraint.toString().toLowerCase()
                for (earthquake in earthquakeList) {
                    if (earthquake.lokasyon.toLowerCase().contains(filterPattern) ||
                        earthquake.date.toLowerCase().contains(filterPattern) ||
                        earthquake.mag.toString().toLowerCase().contains(filterPattern))
                        filteredList.add(earthquake)
                }
            }

            returnList.values = filteredList
            return returnList
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            earthquakeList.clear()
            earthquakeList.addAll(results.values as Collection<EarthquakeOA>)
            notifyDataSetChanged()
        }
    }

    interface EarthquakeListener {
        fun onItemClick(pos: Int, earthquakeOA: EarthquakeOA)
        fun onMapClick(latlng: LatLng, header: String)
    }

}