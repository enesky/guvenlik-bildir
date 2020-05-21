package com.enesky.guvenlikbildir.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.enesky.guvenlikbildir.database.dao.ContactDao
import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.database.dao.SmsReportDao
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.database.entity.StatusConverter

/**
 * Created by Enes Kamil YILMAZ on 24.04.2020
 */

@Database(
    entities = [Earthquake::class, Contact::class, SmsReport::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StatusConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun earthquakeDao(): EarthquakeDao
    abstract fun contactDao(): ContactDao
    abstract fun smsReportDao(): SmsReportDao

    companion object {
        @Volatile
        var dbInstance: AppDatabase? = null

        fun getDatabaseManager(context: Context): AppDatabase {
            return dbInstance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "guvenlikbildir-db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                dbInstance = instance
                instance
            }
        }

    }

}