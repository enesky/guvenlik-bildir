package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.EarthquakePagingAdapter
import com.enesky.guvenlikbildir.database.EarthquakeDB
import com.enesky.guvenlikbildir.database.EarthquakeVM
import com.enesky.guvenlikbildir.databinding.FragmentLastestEarthquakesBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.model.EarthquakeOA
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_lastest_earthquakes.*
import kotlinx.coroutines.*
import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
class LatestEarthquakesFragment : BaseFragment(), AppBarLayout.OnOffsetChangedListener,
    ViewTreeObserver.OnGlobalLayoutListener, SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentLastestEarthquakesBinding
    private lateinit var latestEarthquakesVM: LatestEarthquakesVM
    private var isAppBarExpanded: Boolean = false

    private val loadingDuration: Long = (600L / 0.8).toLong()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_lastest_earthquakes,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        latestEarthquakesVM = getViewModel()
        binding.apply {
            viewModel = latestEarthquakesVM
            lifecycleOwner = this@LatestEarthquakesFragment
        }

        latestEarthquakesVM.init(requireContext(), binding)

        val earthquakeDao = EarthquakeDB.getDatabaseManager(requireActivity().applicationContext).earthquakeDao()

        val rvViewModel by lazy {
            getViewModel { EarthquakeVM(App.mInstance, earthquakeDao) }
        }

        val earthquakePagingAdapter = EarthquakePagingAdapter(
            context = requireContext(),
            earthquakeItemListener = latestEarthquakesVM
        )

        rv_earthquakes.layoutManager = LinearLayoutManager(requireContext())
        rv_earthquakes.adapter = earthquakePagingAdapter

        rvViewModel.earthquakeList.observe(
            viewLifecycleOwner,
            Observer(earthquakePagingAdapter::submitList)
        )

        app_bar_layout.addOnOffsetChangedListener(this)

        /* TODO: Internet bağlantısı olmadığında refresh edemesin veya toast göster.
        ConnectionLiveData(requireContext()).observe(viewLifecycleOwner, Observer { isOnline ->
            if (isOnline)
                fab_synchronize.setImageResource(R.drawable.ic_sync)
            else
                fab_synchronize.setImageResource(R.drawable.ic_sync_problem)
        })
        */

        latestEarthquakesVM.responseHandler.addObserver { _, response ->
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    if (response != null && response is Result<*>) {
                        when (response.status) {
                            Status.SUCCESS -> ""
                            Status.FAILURE, Status.EXCEPTION -> requireContext().showToast(response.data.toString())
                        }
                    }
                }
            }
        }

        latestEarthquakesVM.whereTo.observe(viewLifecycleOwner, Observer {
            if (it is String)
                openInfoCountDownDialog(Constants.map + it)
        })

        latestEarthquakesVM.onClick.observe(viewLifecycleOwner, Observer {
            if (it is EarthquakeOA)
                Toast.makeText(requireContext(), "Item clicked: ${it.date}", Toast.LENGTH_SHORT)
                    .show()
            //requireContext().showToast("Item clicked: ${it.date}")
        })

        latestEarthquakesVM.onLongPressed.observe(viewLifecycleOwner, Observer {
            if (it is EarthquakeOA)
                requireContext().showToast("Item long pressed: ${it.date}")
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pb_loading.makeItVisible()
        srl_refresh.isRefreshing = true

        GlobalScope.launch(Dispatchers.Main) {

            //TODO: set data

            delay(100)
            requireActivity().runOnUiThread {
                pb_loading.makeItGone()
                srl_refresh.isRefreshing = false
            }
        }

        val snapHelper = GravitySnapHelper(Gravity.CENTER)
        snapHelper.attachToRecyclerView(rv_earthquakes)

        //updateRecyclerViewAnimDuration()

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
        pb_loading.makeItVisible()
        GlobalScope.launch(Dispatchers.Main) {

            //TODO: Refresh data

            delay(1000)
            pb_loading.makeItGone()
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
        latestEarthquakesVM.earthquakeAdapter.value!!.filter.filter(newText)
        return true
    }
}