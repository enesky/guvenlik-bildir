package com.enesky.guvenlikbildir.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.database.repo.EarthquakeRepository
import com.enesky.guvenlikbildir.others.lastLoadedEarthquake
import com.enesky.guvenlikbildir.network.EarthquakeAPI
import com.enesky.guvenlikbildir.service.FcmService
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 10.05.2020
 */

class NotifierWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        return@coroutineScope try {

            val earthquakeDao = AppDatabase.getDatabaseManager(context.applicationContext).earthquakeDao()
            val earthquakeRepository = EarthquakeRepository(earthquakeDao)
            val earthquakeList: List<Earthquake>

            val response = EarthquakeAPI().getKandilliPost()
            if (response.isSuccessful) {
                Timber.tag("NotifierWorker").d("doWork Success")
                earthquakeList = EarthquakeAPI.parseResponse(response.body()!!.replace("�", "I"))

                if (lastLoadedEarthquake != null) {
                    loop@ for (earthquake: Earthquake in earthquakeList)
                        when {

                            earthquake == lastLoadedEarthquake -> {
                                Timber.tag("NotifierWorker")
                                    .d("lastLoadedEarthquake= ${earthquake.location} - ${earthquake.dateTime}")
                                break@loop
                            }

                        earthquake.magML >= notificationMagLimit -> {
                            Timber.tag("NotifierWorker")
                                .d("earthquake.magML >= 1.5 => ${earthquake.location} - ${earthquake.dateTime}")
                            FcmService.showLocalNotification(
                                context = context,
                                earthquake = earthquake
                            )
                        }

                            else -> {
                                Timber.tag("NotifierWorker")
                                    .d("${earthquake.location} - ${earthquake.dateTime}")
                            }

                        }
                }

                earthquakeRepository.refreshEartquakes(earthquakeList)
                Result.success()
            } else {
                Timber.tag("NotifierWorker").d("doWork Failed")
                Result.failure()
            }
        } catch (e: Exception) {
            Timber.tag("NotifierWorker").d("doWork Exception")
            Result.failure()
        }

    }

}