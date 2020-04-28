package com.enesky.guvenlikbildir.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.others.ioThread
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Enes Kamil YILMAZ on 25.04.2020
 */

class EarthquakeVM(
    application: Application,
    val earthquakeDao: EarthquakeDao) : AndroidViewModel(application) {


    val earthquakeList: LiveData<PagedList<Earthquake>> = earthquakeDao.getEarthquakes().toLiveData(
        Config( pageSize =  15,
                enablePlaceholders = true,
                maxSize = Constants.EARTHQUAKE_LIST_SIZE
    ))

    var filterText: MutableLiveData<String> = MutableLiveData()

    suspend fun initAllEarthquakes(earthquakes: List<Earthquake>) {
        earthquakeDao.deleteAll()
        insert(earthquakes)
    }

    fun insert(earthquakes: List<Earthquake>) = ioThread {
        GlobalScope.launch(Dispatchers.Main) {
            earthquakeDao.insertAll(earthquakes)
        }
    }

}