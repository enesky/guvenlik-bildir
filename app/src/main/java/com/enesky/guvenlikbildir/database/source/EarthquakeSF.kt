package com.enesky.guvenlikbildir.database.source

import androidx.paging.DataSource
import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.database.entity.Earthquake

/**
 * Created by Enes Kamil YILMAZ on 01.05.2020
 */

class EarthquakeSF(private val earthquakeDao: EarthquakeDao) :
    DataSource.Factory<Int, Earthquake>() {

    private var query = ""
    private var minMag = 0.0
    private var maxMag = 12.0

    override fun create(): DataSource<Int, Earthquake> {
        return if (query.isEmpty()) {
            if (minMag == 0.0 && maxMag == 12.0) {
                earthquakeDao.getAllEarthquakesDsF().map { it }.create()
            } else {
                when {
                    maxMag == 12.0 -> earthquakeDao.getEarthquakesHigherThanGivenMagDsF(minMag).map { it }.create()
                    minMag == 0.0 -> earthquakeDao.getEarthquakesLowerThanGivenMagDsF(maxMag).map { it }.create()
                    else -> earthquakeDao.getEarthquakesBetweenGivenMagsDsF(minMag, maxMag).map { it }.create()
                }
            }
        } else {
            if (minMag == 0.0 && maxMag == 12.0) {
                earthquakeDao.getEarthquakesWithContainsQuery(query).map { it }.create()
            } else {
                when {
                    maxMag == 12.0 -> earthquakeDao.getEarthquakesHappenedAtGivenLocAndHigherThanMagDsF(query, minMag).map { it }.create()
                    minMag == 0.0 -> earthquakeDao.getEarthquakesHappenedAtGivenLocAndLowerThanGivenMagDsF(query, maxMag).map { it }.create()
                    else -> earthquakeDao.getEarthquakesHappenedAtGivenLocAndBetweenGivenMagsDsF(query, minMag, maxMag).map { it }.create()
                }
            }
        }
    }

    fun filter(query: String, minMag: Double, maxMag: Double) {
        this.query = query
        this.minMag = minMag
        this.maxMag = maxMag
    }

}