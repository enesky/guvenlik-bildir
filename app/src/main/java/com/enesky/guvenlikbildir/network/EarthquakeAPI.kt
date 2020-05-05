package com.enesky.guvenlikbildir.network

import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.network.EarthquakeOaAPI.Companion.createHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Enes Kamil YILMAZ on 23.04.2020
 */

class EarthquakeAPI {

    suspend fun getKandilliPost() = kandilliWebService.getKandilliPost("application/json")

    companion object {

        /**
         * Retrofit Builder for http://www.koeri.boun.edu.tr/scripts/
         * Suitable for HTML formatted responses
         */
        val kandilliWebService: WebService by lazy {
            Retrofit.Builder()
                .baseUrl(Constants.kandilliBaseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(createHttpClient())
                .build()
                .create(WebService::class.java)
        }

        private const val dataIndex = 591
        private const val dateIndex = 10
        private const val timeIndex = dateIndex + 10
        private const val latIndex = timeIndex + 10
        private const val lngIndex = latIndex + 10
        private const val depthIndex = lngIndex + 14
        private const val magMDIndex = depthIndex + 5
        private const val magMLIndex = magMDIndex + 5
        private const val magMWIndex = magMLIndex + 5
        private const val locationIndex = magMWIndex + 50
        
        fun parseResponse(response: String): List<Earthquake> {
            val earthquakeList = ArrayList<Earthquake>()

            val regex = """<pre>.*</pre>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            regex.find(response)?.value?.let {
                it.slice(dataIndex until it.length).split("\n").forEachIndexed { index, line ->
                    if (index < Constants.EARTHQUAKE_LIST_SIZE && line.trim().isNotEmpty() && !line.contains("</pre>"))
                        earthquakeList.add(parseLine(line))
                    else
                        return@forEachIndexed
                }
            }

            //Log.i("EarthquakeAPI - earthquakeList", earthquakeList.toString())

            return earthquakeList.toList()
        }

        private fun parseLine(line: String): Earthquake {
            var locationOuter = ""
            var locationInner = ""

            line.slice(magMWIndex..locationIndex).trim().split(" ").let {
                locationOuter = it.last().replace("(", "").replace(")", "").run {
                    this[0] + this.slice(1 until length).toLowerCase(Locale("TR"))
                }
                if (it.size > 1)
                    locationInner = it.first()
            }

            return Earthquake(
                0,
                date = line.slice(0..dateIndex).trim(),
                time = line.slice(dateIndex..timeIndex).trim(),
                dateTime = line.slice(0..dateIndex).trim() + " " + line.slice(dateIndex..timeIndex).trim(),
                lat = line.slice(timeIndex..latIndex).trim(),
                lng = line.slice(latIndex..lngIndex).trim(),
                depth = line.slice(lngIndex..depthIndex).trim(),
                magMD = line.slice(depthIndex..magMDIndex).trim(),
                magML = line.slice(magMDIndex..magMLIndex).trim().toDouble(),
                magMW = line.slice(magMLIndex..magMWIndex).trim(),
                locationOuter = locationOuter,
                locationInner = locationInner,
                location = "$locationInner $locationOuter",
                quality = line.slice(locationIndex until line.length).trim()
            )
        }

    }

}

