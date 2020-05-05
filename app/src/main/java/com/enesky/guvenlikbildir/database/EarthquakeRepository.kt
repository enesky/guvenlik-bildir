package com.enesky.guvenlikbildir.database

import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.others.ioThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Enes Kamil YILMAZ on 02.05.2020
 */

class EarthquakeRepository(private val earthquakeDao: EarthquakeDao) {

    fun getEarthquakes(earthquakeSF: EarthquakeSF, query: String, minMag: Double, maxMag: Double) {
        earthquakeSF.filter(query, minMag, maxMag)
    }

    suspend fun initAllEarthquakes(earthquakes: List<Earthquake>) {
        earthquakeDao.deleteAll()
        ioThread {
            GlobalScope.launch(Dispatchers.Main) {
                earthquakeDao.insertAll(earthquakes)
            }
        }
    }

}