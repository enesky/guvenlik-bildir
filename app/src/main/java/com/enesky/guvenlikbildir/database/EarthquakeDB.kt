package com.enesky.guvenlikbildir.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.database.entity.Earthquake

/**
 * Created by Enes Kamil YILMAZ on 24.04.2020
 */

@Database(entities = [Earthquake::class], version = 1)
abstract class EarthquakeDB : RoomDatabase() {

    abstract fun earthquakeDao(): EarthquakeDao

    companion object {
        var INSTANCE: EarthquakeDB? = null

        fun getDatabaseManager(context: Context): EarthquakeDB? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    EarthquakeDB::class.java,
                    "earthquake-db"
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE
        }
    }
}