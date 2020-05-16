package com.enesky.guvenlikbildir.database.repo

import com.enesky.guvenlikbildir.database.source.EarthquakeSF
import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.extensions.lastLoadedEarthquake
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

    suspend fun refreshEartquakes(earthquakes: List<Earthquake>) {
        earthquakeDao.deleteAll()
        lastLoadedEarthquake = earthquakes.first()
        ioThread {
            GlobalScope.launch(Dispatchers.Default) {
                earthquakeDao.insertAll(earthquakes)
            }
        }
    }

}