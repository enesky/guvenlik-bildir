package com.enesky.guvenlikbildir.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.ItemEarthquakeBinding
import com.enesky.guvenlikbildir.extensions.*
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
import me.samlss.broccoli.Broccoli


/**
 * Created by Enes Kamil YILMAZ on 25.04.2020
 */

class EarthquakePagingAdapter(context: Context,
                              val earthquakeItemListener: EarthquakeItemListener
) : PagedListAdapter<Earthquake, EarthquakePagingAdapter.EarthquakeViewHolder>(DIFF_CALLBACK) {

    private lateinit var recyclerView: RecyclerView
    private var expandedItemPos: Int? = null

    private val originalWidth = context.screenWidth - 24.dp
    private val expandedWidth = context.screenWidth - 8.dp
    private var originalHeight = -1
    private var expandedHeight = -1
    private val listItemExpandDuration = (300L / 0.85).toLong()
    private val listItemPadding = context.resources.getDimension(R.dimen.default_margin_16)
    private var isScaledDown = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarthquakeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEarthquakeBinding.inflate(inflater, parent, false)
        return EarthquakeViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: EarthquakeViewHolder,
        position: Int
    ) = holder.bind(getItem(position))

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    inner class EarthquakeViewHolder(
        val binding: ItemEarthquakeBinding
    ) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {
        var map: GoogleMap? = null
        var mEarthquake: Earthquake? = null
        val cvMap = binding.cvMap
        val ivIndicator = binding.ivIndicator
        val pbLoading = binding.pbLoading
        val cardContainer = binding.cardContainer
        val scaleContainer = binding.scaleContainer

        val broccoli = Broccoli()

        fun bind(earthquake: Earthquake?) {

            when (earthquake) {
                null -> {
                    broccoli.addPlaceholders(
                        binding.tvLocation,
                        binding.tvDepth,
                        binding.tvDate,
                        binding.tvMag,
                        binding.tvShortDate,
                        binding.map)
                    broccoli.show()

                    binding.map.onCreate(null)
                    binding.map.onResume()
                }
                else -> {
                    broccoli.removeAllPlaceholders()

                    mEarthquake = earthquake
                    binding.earthquake = earthquake
                    binding.map.onCreate(null)
                    binding.map.onResume()
                    binding.map.getMapAsync(this)

                    setMagBackgroundTint(binding.tvMag, earthquake.magML.toFloat())
                    toggleItem(this, adapterPosition == expandedItemPos, animate = false)
                    scaleDownItem(this, adapterPosition, isScaledDown)

                    binding.rootLayout.setOnClickListener {
                        earthquakeItemListener.onItemClick(earthquake)
                        when (expandedItemPos) {
                            null -> {
                                // expand clicked item
                                toggleItem(this, expand = true, animate = true)
                                setupMap(map, earthquake, binding.pbLoading)
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
                                setupMap(map, earthquake, binding.pbLoading)
                                expandedItemPos = adapterPosition
                            }
                        }
                    }

                    binding.ivOptions.setOnClickListener {
                        earthquakeItemListener.onOptionsClick(earthquake)
                    }

                }
            }

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
            setupMap(holder.map, holder.mEarthquake!!, holder.pbLoading)

        // get originalHeight & expandedHeight if not gotten before
        if (expandedHeight < 0) {
            expandedHeight = 0 // so that this block is only called once

            holder.cardContainer.doOnLayout { view ->
                originalHeight = view.height
                holder.cvMap.makeItVisible()
                view.doOnPreDraw {
                    expandedHeight = view.height
                    holder.cvMap.makeItGone()
                }
            }
        }
    }

    fun setupMap(map: GoogleMap?, earthquake: Earthquake, progressBar: ProgressBar) {
        if (map != null) {
            val loc = LatLng(earthquake.lat.toDouble(), earthquake.lng.toDouble())
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            map.uiSettings.isMapToolbarEnabled = false
            map.addMarker(MarkerOptions().position(loc))
            map.moveCamera(CameraUpdateFactory.newLatLng(loc))

            map.setOnMapClickListener {
                earthquakeItemListener.onMapClick(loc, earthquake.location)
            }
            map.setOnMapLoadedCallback {
                progressBar.makeItGone()
            }
        }
    }

    fun toggleItem(holder: EarthquakeViewHolder, expand: Boolean, animate: Boolean) {
        if (animate) {
            val animator = getValueAnimator(
                expand, listItemExpandDuration, AccelerateDecelerateInterpolator()
            ) { progress -> setExpandProgress(holder, progress) }

            if (expand)
                animator.doOnStart {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(200)
                        holder.cvMap.makeItVisible()
                    }
                }
            else
                animator.doOnStart {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(100)
                        holder.cvMap.makeItGone()
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

        holder.ivIndicator.rotation = 180 * progress
    }

    inline val LinearLayoutManager.visibleItemsRange: IntRange
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
    fun scaleDownItem(holder: EarthquakeViewHolder, position: Int, isScaleDown: Boolean) {
        setScaleDownProgress(holder, position, if (isScaleDown) 1f else 0f)
    }

    fun setMagBackgroundTint(view: View, magnitude: Float) {
        val color = when {
            magnitude < 3.5 -> android.R.color.white
            3.5 >= magnitude && magnitude < 4.5 -> R.color.colorSecondary
            else -> R.color.red
        }
        view.setBackgroundTint(color)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Earthquake>() {
            override fun areItemsTheSame(oldEarthquake: Earthquake,
                                         newEarthquake: Earthquake) =
                oldEarthquake.dateTime == newEarthquake.dateTime &&
                        oldEarthquake.id == newEarthquake.id

            override fun areContentsTheSame(oldEarthquake: Earthquake,
                                            newEarthquake: Earthquake) =
                oldEarthquake.dateTime == newEarthquake.dateTime &&
                        oldEarthquake.id == newEarthquake.id
        }
    }

    interface EarthquakeItemListener {
        fun onItemClick(earthquake: Earthquake)
        fun onOptionsClick(earthquake: Earthquake)
        fun onMapClick(latlng: LatLng, header: String)
    }

}