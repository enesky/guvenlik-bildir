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
import com.enesky.guvenlikbildir.ui.dialog.EarthquakeItemOptionsBSDFragment
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.google.android.material.appbar.AppBarLayout
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
            MainVM(AppDatabase.dbInstance!!)
        }
    }

    private var isAppBarExpanded: Boolean = false
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

        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)

        val earthquakePagingAdapter = EarthquakePagingAdapter(
            context = requireContext(),
            earthquakeItemListener = latestEarthquakesVM
        )

        mainVM.earthquakes.observe( viewLifecycleOwner, Observer { earthquakes ->
                earthquakes?.let {
                    binding.pbLoading.makeItGone()

                    if (earthquakes.isEmpty())
                        binding.tvPlaceholder.makeItVisible()
                    else
                        binding.tvPlaceholder.makeItGone()

                    binding.rvEarthquakes.adapter = earthquakePagingAdapter
                    earthquakePagingAdapter.submitList(earthquakes)

                    GlobalScope.launch(Dispatchers.Main) {
                        delay(1000)

                        if (earthquakes.isNotEmpty()) {
                            if (((binding.rvEarthquakes.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() != 0 ||
                                        (binding.rvEarthquakes.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() > 0)) {
                                binding.fabScrollToTop.show()
                            }
                        }

                    }
            }
        })

        mainVM.responseHandler.addObserver { _, response ->
            GlobalScope.launch(Dispatchers.Main) {
                if (response != null && response is Result<*>) {
                    when (response) {
                        Status.SUCCESS -> return@launch
                        Status.FAILURE -> requireContext().showToast(response.data.toString())
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

        latestEarthquakesVM.whereTo.observe(viewLifecycleOwner, Observer {
            if (it is Earthquake)
                EarthquakeItemOptionsBSDFragment.newInstance(it)
                    .show(activity!!.supportFragmentManager,"EarthquakeItemOptionsBSDFragment")
        })

        latestEarthquakesVM.onFilterIndexChange.observe(viewLifecycleOwner, Observer {
            when(it) {
                0 -> mainVM.getEarthquakeList(lastQuery,0.0,12.0)
                1 -> mainVM.getEarthquakeList(lastQuery,0.0,3.0)
                2 -> mainVM.getEarthquakeList(lastQuery,3.0,4.5)
                3 -> mainVM.getEarthquakeList(lastQuery, 4.5, 12.0)
            }
        })

        binding.appBarLayout.addOnOffsetChangedListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GravitySnapHelper(Gravity.TOP).attachToRecyclerView(binding.rvEarthquakes)

        binding.rvEarthquakes.updateRecyclerViewAnimDuration()

        binding.srlRefresh.setOnRefreshListener {
            refresh()
        }

        binding.ivKandilli.setOnClickListener {
            requireActivity().openWebView(Constants.kandilliUrl)
        }

        binding.ivFilter.setOnClickListener {
            if (!isAppBarExpanded) {
                if (binding.appBarLayout.isVisible)
                    binding.appBarLayout.setExpanded(true, true)
                else {
                    GlobalScope.launch(Dispatchers.Main) {
                        binding.appBarLayout.makeItVisible()
                        delay(50)
                        binding.appBarLayout.setExpanded(true, true)
                    }
                }
            } else {
                binding.appBarLayout.setExpanded(false, true)
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500)
                    binding.appBarLayout.makeItGone()
                }
            }
        }

        binding.rvEarthquakes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    binding.fabScrollToTop.show()
                else
                    binding.fabScrollToTop.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if ((recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() != 0 ||
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() > 0) {
                        binding.fabScrollToTop.show()
                }
            }
        })

        binding.fabScrollToTop.setOnClickListener {
            binding.fabScrollToTop.makeItGone()
            binding.rvEarthquakes.smoothScrollToPosition(0)
        }

        binding.svEarthquake.viewTreeObserver.addOnGlobalLayoutListener(this)
        binding.svEarthquake.setOnQueryTextListener(this)
    }

    private fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            mainVM.getEarthquakes()
            delay(500)
            binding.srlRefresh.isRefreshing = false
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (verticalOffset == 0)
            isAppBarExpanded = true
        else if (abs(verticalOffset) >= appBarLayout!!.totalScrollRange)
            isAppBarExpanded = false
    }

    override fun onGlobalLayout() {
        binding.svEarthquake.viewTreeObserver.removeOnGlobalLayoutListener(this)
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