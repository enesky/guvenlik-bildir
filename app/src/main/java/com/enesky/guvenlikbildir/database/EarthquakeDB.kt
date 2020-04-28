package com.enesky.guvenlikbildir.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.others.ioThread

/**
 * Created by Enes Kamil YILMAZ on 24.04.2020
 */

@Database(
    entities = [Earthquake::class],
    version = 1,
    exportSchema = false
)
abstract class EarthquakeDB : RoomDatabase() {

    abstract fun earthquakeDao(): EarthquakeDao

    companion object {
        @Volatile
        var DB_INSTANCE: EarthquakeDB? = null

        fun getDatabaseManager(context: Context): EarthquakeDB {
            if (DB_INSTANCE == null) {
                DB_INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    EarthquakeDB::class.java,
                    "earthquake-db"
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return DB_INSTANCE!!
        }

    }

}