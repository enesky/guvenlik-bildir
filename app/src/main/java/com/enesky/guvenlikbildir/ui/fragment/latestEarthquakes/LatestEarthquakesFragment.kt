package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentLastestEarthquakesBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.model.EarthquakeOA
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_lastest_earthquakes.*
import kotlinx.coroutines.*
import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
class LatestEarthquakesFragment: BaseFragment(), AppBarLayout.OnOffsetChangedListener,
    ViewTreeObserver.OnGlobalLayoutListener, SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentLastestEarthquakesBinding
    private lateinit var latestEarthquakesVM: LatestEarthquakesVM
    private var isAppBarExpanded: Boolean = false
    private var listExpand: Int = 10

    private val loadingDuration: Long = (600L / 0.8).toLong()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lastest_earthquakes, container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        latestEarthquakesVM = getViewModel()
        binding.apply {
            viewModel = latestEarthquakesVM
            lifecycleOwner = this@LatestEarthquakesFragment
        }

        latestEarthquakesVM.init(requireActivity(), binding)

        app_bar_layout.addOnOffsetChangedListener(this)

        ConnectionLiveData(requireContext()).observe(viewLifecycleOwner, Observer { isOnline ->
            if (isOnline)
                fab_synchronize.setImageResource(R.drawable.ic_sync)
            else
                fab_synchronize.setImageResource(R.drawable.ic_sync_problem)
        })

        latestEarthquakesVM.responseHandler.addObserver{ _, response ->
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

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pb_loading.makeItVisible()

        GlobalScope.launch(Dispatchers.Main) {
            latestEarthquakesVM.earthquakeAdapter.value!!.update(App.mInstance.mockEarthquakeList.result.subList(0,10).toMutableList())
            delay(100)
            requireActivity().runOnUiThread { pb_loading.makeItGone() }
        }

        val snapHelper = GravitySnapHelper(Gravity.CENTER)
        snapHelper.attachToRecyclerView(rv_earthquakes)

        updateRecyclerViewAnimDuration()

        fab_synchronize.setOnClickListener {
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
        val objectAnimator = ObjectAnimator
            .ofFloat(fab_synchronize, "rotation", 360f, 0f)

        objectAnimator.start()
        pb_loading.makeItVisible()
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            latestEarthquakesVM.earthquakeAdapter.value!!.update(App.mInstance.mockEarthquakeList.result.subList(0,listExpand) as MutableList<EarthquakeOA>)
            listExpand += 10

            //latestEarthquakesVM.getLastEarthquakes("20")

            delay(1000)
            objectAnimator.cancel()
            pb_loading.makeItGone()
        }
    }

    /**
     * Update RecyclerView Item Animation Durations
     */
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