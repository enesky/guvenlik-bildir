package com.enesky.guvenlikbildir.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.enesky.guvenlikbildir.database.entity.Earthquake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Created by Enes Kamil YILMAZ on 24.04.2020
 */

@Dao
interface EarthquakeDao {

    @Query("SELECT * FROM earthquake WHERE id == :id")
    suspend fun getEarthquake(id: Int): Flow<Earthquake>

    @ExperimentalCoroutinesApi
    suspend fun getEarthquakeDistinctUntilChanged(id: Int) = getEarthquake(id).distinctUntilChanged()

    @Query("SELECT * FROM earthquake") // ORDER BY id ASC
    suspend fun getAllEarthquakes(): Flow<List<Earthquake>>

    @Query("SELECT * FROM earthquake WHERE magMD > :minMag") //ORDER BY ${Earthquake.COLUMN_ID} ASC
    suspend fun getEarthquakesBiggerThanGivenMag(minMag: Int): Flow<List<Earthquake>>

    @Query("SELECT * FROM earthquake WHERE locationInner LIKE :location OR locationOuter LIKE :location") //ORDER BY ${Earthquake.COLUMN_ID} ASC
    suspend fun getEarthquakesHappenedAtGivenLoc(location: String): Flow<List<Earthquake>>

    @Query("DELETE FROM earthquake")
    suspend fun deleteAll()

    //TODO: Check if old entries are still there or this method replacing all of them.
    @Insert
    suspend fun insertAll(vararg earthquake: Earthquake)

}