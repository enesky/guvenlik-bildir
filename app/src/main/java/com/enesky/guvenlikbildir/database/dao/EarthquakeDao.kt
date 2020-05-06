package com.enesky.guvenlikbildir.database.dao

import androidx.paging.DataSource
import androidx.room.*
import com.enesky.guvenlikbildir.database.entity.Earthquake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Created by Enes Kamil YILMAZ on 24.04.2020
 */

@Dao
interface EarthquakeDao {

    @Query("SELECT * FROM earthquake")
    fun getAllEarthquakesDsF(): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE magML >= :minMag AND magML < :maxMag")
    fun getEarthquakesBetweenGivenMagsDsF(minMag: Double, maxMag: Double): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE magML >= :minMag")
    fun getEarthquakesHigherThanGivenMagDsF(minMag: Double): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE magML < :maxMag")
    fun getEarthquakesLowerThanGivenMagDsF(maxMag: Double): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE locationInner LIKE :location OR locationOuter LIKE :location")
    fun getEarthquakesHappenedAtGivenLocDsF(location: String): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE (magML >= :minMag AND magML < :maxMag) AND location LIKE '%' || :query || '%'")
    fun getEarthquakesHappenedAtGivenLocAndBetweenGivenMagsDsF(
        query: String,
        minMag: Double,
        maxMag: Double): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE magML >= :minMag AND location LIKE '%' || :query || '%'")
    fun getEarthquakesHappenedAtGivenLocAndHigherThanMagDsF(
        query: String,
        minMag: Double): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE magML < :maxMag AND location LIKE '%' || :query || '%'")
    fun getEarthquakesHappenedAtGivenLocAndLowerThanGivenMagDsF(
        query: String,
        maxMag: Double): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE locationOuter LIKE '%' || :query || '%' OR locationInner LIKE '%' || :query || '%'")
    fun getEarthquakesWithContainsQuery(query: String?): DataSource.Factory<Int, Earthquake>

    @Query("SELECT * FROM earthquake WHERE id == :id")
    fun getEarthquake(id: Int): Flow<Earthquake>

    @ExperimentalCoroutinesApi
    fun getEarthquakeDistinctUntilChanged(id: Int) = getEarthquake(id).distinctUntilChanged()

    @Query("SELECT * FROM earthquake") // ORDER BY id ASC
    fun getAllEarthquakes(): Flow<List<Earthquake>>

    @Query("SELECT * FROM earthquake WHERE magML > :minMag") //ORDER BY ${Earthquake.COLUMN_ID} ASC
    fun getEarthquakesBiggerThanGivenMag(minMag: Int): Flow<List<Earthquake>>

    @Query("SELECT * FROM earthquake WHERE locationInner LIKE :location OR locationOuter LIKE :location") //ORDER BY ${Earthquake.COLUMN_ID} ASC
    fun getEarthquakesHappenedAtGivenLoc(location: String): Flow<List<Earthquake>>

    @Query("DELETE FROM earthquake")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg earthquake: Earthquake)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(earthquakes: List<Earthquake>)

    @Delete
    fun delete(earthquake: Earthquake)

}