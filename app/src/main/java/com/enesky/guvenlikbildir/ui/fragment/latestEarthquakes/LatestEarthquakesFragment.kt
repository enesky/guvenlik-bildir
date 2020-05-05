package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.EarthquakePagingAdapter
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.FragmentLatestEarthquakesBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.enesky.guvenlikbildir.ui.dialog.EarthquakeOptionsDialog
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_latest_earthquakes.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class LatestEarthquakesFragment : BaseFragment(), CoroutineScope,
    AppBarLayout.OnOffsetChangedListener,
    ViewTreeObserver.OnGlobalLayoutListener, SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentLatestEarthquakesBinding
    private lateinit var latestEarthquakesVM: LatestEarthquakesVM
    private var isAppBarExpanded: Boolean = false

    private val loadingDuration: Long = (600L / 0.8).toLong()
    var textChangedJob: Job? = null

    var lastQuery = ""
    var lastMinMag = 0.0
    var lastMaxMag = 12.0

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_latest_earthquakes, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        latestEarthquakesVM = getViewModel()
        binding.apply {
            viewModel = latestEarthquakesVM
            lifecycleOwner = this@LatestEarthquakesFragment
        }

        latestEarthquakesVM.init(binding)

        val earthquakePagingAdapter = EarthquakePagingAdapter(
            context = requireContext(),
            earthquakeItemListener = latestEarthquakesVM
        )

        (requireActivity() as MainActivity).earthquakeVM.earthquakes.observe( viewLifecycleOwner,
            Observer { earthquakes ->
                earthquakes?.let {
                    pb_loading.makeItGone()

                    if (earthquakes.isEmpty())
                        tv_placeholder.makeItVisible()
                    else
                        tv_placeholder.makeItGone()

                    rv_earthquakes.adapter = earthquakePagingAdapter
                    earthquakePagingAdapter.submitList(it)
                    rv_earthquakes.smoothScrollToPosition(0)
            }
        })

        (requireActivity() as MainActivity).earthquakeVM.responseHandler.addObserver { _, response ->
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    if (response != null && response is Result<*>) {
                        when (response) {
                            Status.SUCCESS -> ""
                            Status.FAILURE, Status.EXCEPTION -> requireContext().showToast(response.data.toString())
                        }
                    }
                }
            }
        }

        (requireActivity() as MainActivity).earthquakeVM.filterText.observe(viewLifecycleOwner, Observer {
            lastQuery = it
        })

        (requireActivity() as MainActivity).earthquakeVM.minMag.observe(viewLifecycleOwner, Observer {
            lastMinMag = it
        })

        (requireActivity() as MainActivity).earthquakeVM.maxMag.observe(viewLifecycleOwner, Observer {
            lastMaxMag = it
        })

        latestEarthquakesVM.whereTo.observe(viewLifecycleOwner, Observer {
            if (it is String)
                openInfoCountDownDialog(Constants.map + it)
        })

        latestEarthquakesVM.onOptionClick.observe(viewLifecycleOwner, Observer {
            if (it is Earthquake)
                EarthquakeOptionsDialog.newInstance(it).show(parentFragmentManager,"EarthquakeOptionsDialog")
        })

        latestEarthquakesVM.onFilterIndexChange.observe(viewLifecycleOwner, Observer {
            when(it) {
                0 -> (requireActivity() as MainActivity).earthquakeVM.getEarthquakeList(lastQuery,0.0,12.0)
                1 -> (requireActivity() as MainActivity).earthquakeVM.getEarthquakeList(lastQuery,0.0,3.0)
                2 -> (requireActivity() as MainActivity).earthquakeVM.getEarthquakeList(lastQuery,3.0,4.5)
                3 -> (requireActivity() as MainActivity).earthquakeVM.getEarthquakeList(lastQuery, 4.5, 12.0)
            }
        })

        app_bar_layout.addOnOffsetChangedListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val snapHelper = GravitySnapHelper(Gravity.TOP)
        snapHelper.attachToRecyclerView(rv_earthquakes)

        updateRecyclerViewAnimDuration()

        srl_refresh.setOnRefreshListener {
            refresh()
        }

        iv_filter.setOnClickListener {
            if (!isAppBarExpanded) {
                if (app_bar_layout.isVisible)
                    app_bar_layout.setExpanded(true, true)
                else {
                    GlobalScope.launch(Dispatchers.Main) {
                        app_bar_layout.makeItVisible()
                        delay(50)
                        app_bar_layout.setExpanded(true, true)
                    }
                }
            } else {
                app_bar_layout.setExpanded(false, true)
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500)
                    app_bar_layout.makeItGone()
                }
            }
        }

        sv_earthquake.viewTreeObserver.addOnGlobalLayoutListener(this)
        sv_earthquake.setOnQueryTextListener(this)
    }

    private fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            (requireActivity() as MainActivity).earthquakeVM.getEarthquakes()
            delay(500)
            srl_refresh.isRefreshing = false
        }
    }

    private fun updateRecyclerViewAnimDuration() = rv_earthquakes.itemAnimator?.run {
        removeDuration = loadingDuration * 60 / 100
        addDuration = loadingDuration
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (verticalOffset == 0)
            isAppBarExpanded = true
        else if (abs(verticalOffset) >= appBarLayout!!.totalScrollRange)
            isAppBarExpanded = false
    }

    override fun onGlobalLayout() {
        sv_earthquake.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText.isNullOrEmpty()) {
            (requireActivity() as MainActivity).earthquakeVM.getEarthquakeList("",lastMinMag,lastMaxMag)
        } else {
            val searchText = newText.trim()
            if (searchText != (requireActivity() as MainActivity).earthquakeVM.filterText.value) {
                (requireActivity() as MainActivity).earthquakeVM.filterText.value = searchText
                textChangedJob?.cancel()
                textChangedJob = launch {
                    delay(300L)
                    (requireActivity() as MainActivity).earthquakeVM.getEarthquakeList(searchText,lastMinMag,lastMaxMag)
                }
            }
        }
        return true
    }

}