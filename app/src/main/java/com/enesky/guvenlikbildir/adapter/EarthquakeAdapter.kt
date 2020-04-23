package com.enesky.guvenlikbildir.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Filter
import android.widget.Filterable
import android.widget.ProgressBar
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ItemEarthquakeBinding
import com.enesky.guvenlikbildir.extensions.dp
import com.enesky.guvenlikbildir.extensions.getValueAnimator
import com.enesky.guvenlikbildir.extensions.makeItGone
import com.enesky.guvenlikbildir.extensions.screenWidth
import com.enesky.guvenlikbildir.model.EarthquakeOA
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

class EarthquakeAdapter(
    context: Context,
    private var earthquakeList: MutableList<EarthquakeOA>,
    private val earthquakeListener: EarthquakeListener
) : RecyclerView.Adapter<EarthquakeAdapter.EarthquakeViewHolder>(), Filterable {

    private lateinit var recyclerView: RecyclerView
    private var expandedItemPos: Int? = null

    private val originalWidth = context.screenWidth - 32.dp
    private val expandedWidth = context.screenWidth - 8.dp
    private var originalHeight = -1 // will be calculated dynamically
    private var expandedHeight = -1 // will be calculated dynamically
    private val listItemExpandDuration = (300L / 0.85).toLong()
    private val listItemPadding = context.resources.getDimension(R.dimen.default_margin_16)
    private var isScaledDown = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarthquakeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEarthquakeBinding.inflate(inflater, parent, false)
        return EarthquakeViewHolder(binding)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemCount(): Int = earthquakeList.size

    override fun onBindViewHolder(holder: EarthquakeViewHolder, pos: Int) = holder.bind(earthquakeList[pos])

    inner class EarthquakeViewHolder(private val binding: ItemEarthquakeBinding) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {
        var map: GoogleMap? = null
        var mEarthquakeOA: EarthquakeOA? = null
        val cvMap = binding.cvMap
        val ivShowMap = binding.ivShowMap
        val progressBar = binding.pbLoading
        val cardContainer = binding.cardContainer
        val scaleContainer = binding.scaleContainer

        fun bind(earthquakeOA: EarthquakeOA) {
            toggleItem(this, adapterPosition == expandedItemPos, animate = false)
            scaleDownItem(this, adapterPosition, isScaledDown)

            binding.rootLayout.setOnClickListener {
                earthquakeListener.onItemClick(earthquakeOA)
                when (expandedItemPos) {
                    null -> {
                        // expand clicked item
                        toggleItem(this, expand = true, animate = true)
                        setupMap(map, earthquakeOA, binding.pbLoading)
                        expandedItemPos = adapterPosition
                    }
                    adapterPosition -> {
                        //collapse clicked item
                        toggleItem(this, expand = false, animate = true)
                        expandedItemPos = null
                    }
                    else -> {
                        // collapse previously expanded item
                        val oldViewHolder = recyclerView.findViewHolderForAdapterPosition(expandedItemPos!!) as? EarthquakeViewHolder
                        if (oldViewHolder != null) toggleItem(oldViewHolder, expand = false, animate = true)

                        // expand clicked item
                        toggleItem(this, expand = true, animate = true)
                        setupMap(map, earthquakeOA, binding.pbLoading)
                        expandedItemPos = adapterPosition
                    }
                }
            }

            binding.rootLayout.setOnLongClickListener {
                earthquakeListener.onLongPressed(earthquakeOA)
                return@setOnLongClickListener true
            }

            mEarthquakeOA = earthquakeOA
            binding.earthquake = earthquakeOA
            binding.map.onCreate(null)
            binding.map.onResume()
            binding.map.getMapAsync(this)
            binding.executePendingBindings()
        }

        override fun onMapReady(googleMap: GoogleMap?) {
            MapsInitializer.initialize(binding.root.context)
            map = googleMap
            //setupMap(map!!, mEarthquakeOA!!, progressBar)
        }
    }

    override fun onViewAttachedToWindow(holder: EarthquakeViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (expandedItemPos == holder.adapterPosition)
            setupMap(holder.map, holder.mEarthquakeOA!!, holder.progressBar)

        // get originalHeight & expandedHeight if not gotten before
        if (expandedHeight < 0) {
            expandedHeight = 0 // so that this block is only called once

            holder.cardContainer.doOnLayout { view ->
                originalHeight = view.height
                holder.cvMap.isVisible = true
                view.doOnPreDraw {
                    expandedHeight = view.height
                    holder.cvMap.isVisible = false
                }
            }
        }
    }

    fun setupMap(map: GoogleMap?, earthquake: EarthquakeOA, progressBar: ProgressBar) {
        if (map != null) {
            val loc = LatLng(earthquake.lat.toDouble(), earthquake.lng.toDouble())
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            map.uiSettings.isMapToolbarEnabled = false
            map.addMarker(MarkerOptions().position(loc))
            map.moveCamera(CameraUpdateFactory.newLatLng(loc))

            map.setOnMapClickListener {
                earthquakeListener.onMapClick(loc, earthquake.title)
            }
            map.setOnMapLoadedCallback {
                progressBar.makeItGone()
            }
        }
    }

    private fun toggleItem(holder: EarthquakeViewHolder, expand: Boolean, animate: Boolean) {
        if (animate) {
            val animator = getValueAnimator(
                expand, listItemExpandDuration, AccelerateDecelerateInterpolator()
            ) { progress -> setExpandProgress(holder, progress) }

            if (expand)
                animator.doOnStart {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(100)
                        holder.cvMap.isVisible = true
                    }
                }
            else
                animator.doOnStart {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(100)
                        holder.cvMap.isVisible = false
                    }
                }

            animator.start()
        } else {
            // show expandView only if we have expandedHeight (onViewAttached)
            holder.cvMap.isVisible = expand && expandedHeight >= 0
            setExpandProgress(holder, if (expand) 1f else 0f)
        }
    }

    private fun setExpandProgress(holder: EarthquakeViewHolder, progress: Float) {
        if (expandedHeight > 0 && originalHeight > 0)
            holder.cardContainer.layoutParams.height = (originalHeight + (expandedHeight - originalHeight) * progress).toInt()

        holder.cardContainer.layoutParams.width = (originalWidth + (expandedWidth - originalWidth) * progress).toInt()
        holder.cardContainer.requestLayout()

        holder.ivShowMap.rotation = 180 * progress
    }

    private inline val LinearLayoutManager.visibleItemsRange: IntRange
        get() = findFirstVisibleItemPosition()..findLastVisibleItemPosition()

    private fun setScaleDownProgress(holder: EarthquakeViewHolder, position: Int, progress: Float) {
        val itemExpanded = position >= 0 && position == expandedItemPos
        holder.cardContainer.layoutParams.apply {
            width = ((if (itemExpanded) expandedWidth else originalWidth) * (1 - 0.1f * progress)).toInt()
            height = ((if (itemExpanded) expandedHeight else originalHeight) * (1 - 0.1f * progress)).toInt()
        }
        holder.cardContainer.requestLayout()

        holder.scaleContainer.scaleX = 1 - 0.05f * progress
        holder.scaleContainer.scaleY = 1 - 0.05f * progress

        holder.scaleContainer.setPadding(
            (listItemPadding * (1 - 0.2f * progress)).toInt(),
            (listItemPadding * (1 - 0.2f * progress)).toInt(),
            (listItemPadding * (1 - 0.2f * progress)).toInt(),
            (0 * (1 - 0.2f * progress)).toInt()
        )
    }

    /** Convenience method for calling from onBindViewHolder */
    private fun scaleDownItem(holder: EarthquakeViewHolder, position: Int, isScaleDown: Boolean) {
        setScaleDownProgress(holder, position, if (isScaleDown) 1f else 0f)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getFilter(): Filter = filter

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
                for (earthquake in earthquakeList)
                    if (earthquake.lokasyon.toLowerCase().contains(filterPattern))
                        filteredList.add(earthquake)
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
        fun onItemClick(earthquakeOA: EarthquakeOA)
        fun onLongPressed(earthquakeOA: EarthquakeOA)
        fun onMapClick(latlng: LatLng, header: String)
    }

}