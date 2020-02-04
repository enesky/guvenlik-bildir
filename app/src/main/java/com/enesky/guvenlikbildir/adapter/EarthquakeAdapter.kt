package com.enesky.guvenlikbildir.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.databinding.ItemEarthquakeBinding
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

    private lateinit var unfilteredList: MutableList<EarthquakeOA>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EarthquakeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEarthquakeBinding.inflate(inflater, parent, false)
        return EarthquakeViewHolder(binding)
    }

    override fun getItemCount(): Int = earthquakeList.size

    override fun onBindViewHolder(holder: EarthquakeViewHolder, pos: Int) = holder.bind(pos, earthquakeList[pos])

    inner class EarthquakeViewHolder(private val binding: ItemEarthquakeBinding) :
        RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {

        var map: GoogleMap? = null

        fun bind(pos: Int, earthquakeOA: EarthquakeOA) {
            binding.earthquake = earthquakeOA
            binding.cvEarthquake.setOnClickListener {
                earthquakeListener.onItemClick(pos, earthquakeOA)
                ObjectAnimator.ofFloat(binding.ivShowMap, "rotation", 180f, 0f)
                    .setDuration(750)
                    .start()

                //TODO: Expand-collapse
            }

            if (map != null) {
                val loc: LatLng? =
                    if (binding.earthquake?.lat != null && binding.earthquake?.lng != null)
                        LatLng(binding.earthquake!!.lat.toDouble(), binding.earthquake!!.lng.toDouble())
                    else
                        LatLng(binding.earthquake!!.coordinates[0].toDouble(), binding.earthquake!!.coordinates[1].toDouble())

                map!!.addMarker(MarkerOptions().position(loc!!))
                map!!.moveCamera(CameraUpdateFactory.newLatLng(loc))
                map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            }

            binding.map.onCreate(null)
            binding.map.onResume()
            binding.map.getMapAsync(this)
            binding.executePendingBindings()
        }

        override fun onMapReady(googleMap: GoogleMap?) {
            MapsInitializer.initialize(binding.root.context)
            map = googleMap

            val loc: LatLng? =
                if (binding.earthquake?.lat != null && binding.earthquake?.lng != null)
                    LatLng(binding.earthquake!!.lat.toDouble(), binding.earthquake!!.lng.toDouble())
                else
                    LatLng(binding.earthquake!!.coordinates[0].toDouble(), binding.earthquake!!.coordinates[1].toDouble())

            map!!.setOnMapClickListener {
                earthquakeListener.onMapClick(loc!!, binding.earthquake!!.title)
            }

            map!!.addMarker(MarkerOptions().position(loc!!))
            map!!.moveCamera(CameraUpdateFactory.newLatLng(loc))
            map!!.uiSettings.isMapToolbarEnabled = false
            map!!.uiSettings.isScrollGesturesEnabled = false
            map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    override fun onViewRecycled(holder: EarthquakeViewHolder) {
        if (holder.map != null) {
            holder.map!!.clear()
            holder.map!!.mapType = GoogleMap.MAP_TYPE_NONE
        }
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