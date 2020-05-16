package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.EarthquakePagingAdapter
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.FragmentLatestEarthquakesBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.activity.main.MainVM
import com.enesky.guvenlikbildir.ui.dialog.EarthquakeItemOptionsDialog
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
    private val mainVM by lazy {
        getViewModel {
            MainVM(AppDatabase.getDatabaseManager(activity!!.application))
        }
    }
    private var isAppBarExpanded: Boolean = false

    private val loadingDuration: Long = (600L / 0.8).toLong()
    var textChangedJob: Job? = null

    var lastQuery = ""
    var lastMinMag = 0.0
    var lastMaxMag = 12.0
    var earthquakeFromNotification: Earthquake? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_latest_earthquakes, container, false)
        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)
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

        mainVM.earthquakes.observe( viewLifecycleOwner, Observer { earthquakes ->
                earthquakes?.let {
                    pb_loading.makeItGone()

                    if (earthquakes.isEmpty())
                        tv_placeholder.makeItVisible()
                    else
                        tv_placeholder.makeItGone()

                    rv_earthquakes.adapter = earthquakePagingAdapter
                    earthquakePagingAdapter.submitList(earthquakes)

                    GlobalScope.launch(Dispatchers.Main) {
                        delay(750)

                        if (earthquakes.isNotEmpty()) {
                            if (earthquakeFromNotification != null) {
                                rv_earthquakes.smoothScrollToPosition(earthquakePagingAdapter.currentList?.indexOf(earthquakeFromNotification)!!)
                                earthquakeFromNotification = null
                            }
                            if (((rv_earthquakes.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() != 0 ||
                                        (rv_earthquakes.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() > 0)) {
                                fab_scroll_to_top.show()
                            }
                        }

                    }
            }
        })

        mainVM.responseHandler.addObserver { _, response ->
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    if (response != null && response is Result<*>) {
                        when (response) {
                            Status.SUCCESS -> ""
                            Status.FAILURE -> requireContext().showToast(response.data.toString())
                        }
                    }
                }
            }
        }

        mainVM.filterText.observe(viewLifecycleOwner, Observer {
            lastQuery = it
        })

        mainVM.minMag.observe(viewLifecycleOwner, Observer {
            lastMinMag = it
        })

        mainVM.maxMag.observe(viewLifecycleOwner, Observer {
            lastMaxMag = it
        })

        mainVM.earthquakeFromNotification.observe(viewLifecycleOwner, Observer {
            earthquakeFromNotification = it
            if (earthquakePagingAdapter.currentList?.indexOf(it) != null)
                rv_earthquakes.smoothScrollToPosition(earthquakePagingAdapter.currentList?.indexOf(it)!!)
        })

        latestEarthquakesVM.whereTo.observe(viewLifecycleOwner, Observer {
            if (it is String)
                openInfoCountDownDialog(Constants.map + it)
        })

        latestEarthquakesVM.onOptionClick.observe(viewLifecycleOwner, Observer {
            if (it is Earthquake)
                EarthquakeItemOptionsDialog.newInstance(it).show(parentFragmentManager,"EarthquakeOptionsDialog")
        })

        latestEarthquakesVM.onFilterIndexChange.observe(viewLifecycleOwner, Observer {
            when(it) {
                0 -> mainVM.getEarthquakeList(lastQuery,0.0,12.0)
                1 -> mainVM.getEarthquakeList(lastQuery,0.0,3.0)
                2 -> mainVM.getEarthquakeList(lastQuery,3.0,4.5)
                3 -> mainVM.getEarthquakeList(lastQuery, 4.5, 12.0)
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

        iv_kandilli.setOnClickListener {
            requireActivity().openWebView(Constants.kandilliUrl)
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

        rv_earthquakes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    fab_scroll_to_top.show()
                else
                    fab_scroll_to_top.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if ((recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() != 0 ||
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() > 0) {
                        fab_scroll_to_top.show()
                }
            }
        })

        fab_scroll_to_top.setOnClickListener {
            fab_scroll_to_top.makeItGone()
            rv_earthquakes.smoothScrollToPosition(0)
        }

        sv_earthquake.viewTreeObserver.addOnGlobalLayoutListener(this)
        sv_earthquake.setOnQueryTextListener(this)
    }

    private fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            mainVM.getEarthquakes()
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
            mainVM.getEarthquakeList("",lastMinMag,lastMaxMag)
        } else {
            val searchText = newText.trim()
            if (searchText != mainVM.filterText.value) {
                mainVM.filterText.value = searchText
                textChangedJob?.cancel()
                textChangedJob = launch {
                    delay(300L)
                    mainVM.getEarthquakeList(searchText,lastMinMag,lastMaxMag)
                }
            }
        }
        return true
    }

}