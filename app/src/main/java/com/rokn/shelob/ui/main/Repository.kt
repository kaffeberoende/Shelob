package com.rokn.shelob.ui.main

import android.util.Log
import com.google.gson.Gson
import com.rokn.shelob.ui.main.data.Value
import com.rokn.shelob.ui.main.data.ValuesCollection
import com.rokn.shelob.ui.main.data.ValuesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

object Repository {

    fun getData(token: String?, currentTime: Long): Flow<ValuesCollection> {
        return flow {
            val values = ValuesCollection()
            //First get local data and emit it
            //TODO Local data
            emit(values)

            //Now get data for a few hours and emit it (använd tiden från den senaste lokala datan här sen)
            val time = currentTime - TimeUnit.HOURS.toMillis(2)
            values.tiltValues = fetchOnePageOfValues(MainViewModel.TILT_URL, time, token).first
            emit(values)
            values.temperatureValues = fetchOnePageOfValues(MainViewModel.TEMPERATURE_URL, time, token).first
            emit(values)
            values.batteryValues = fetchOnePageOfValues(MainViewModel.BATTERY_URL, time, token).first
            emit(values)
            values.gravityValues = fetchOnePageOfValues(MainViewModel.GRAVITY_URL, time, token).first
            emit(values)
            values.rssiValues = fetchOnePageOfValues(MainViewModel.RSSI_URL, time, token).first
            emit(values)
            values.intervalValues = fetchOnePageOfValues(MainViewModel.INTERVAL_URL, time, token).first
            emit(values)

            //Now get the rest of the data and emit it (page by page?)
            emit(fetchData(token))
        }.flowOn(Dispatchers.IO)
    }

    private fun fetchData(token: String?): ValuesCollection {
            val values = ValuesCollection()
            values.tiltValues = fetchPages(MainViewModel.TILT_URL, MainViewModel.MAX_PAGES, token)
            values.temperatureValues = fetchPages(MainViewModel.TEMPERATURE_URL, MainViewModel.MAX_PAGES, token)
            values.batteryValues = fetchPages(MainViewModel.BATTERY_URL, MainViewModel.MAX_PAGES, token)
            values.gravityValues = fetchPages(MainViewModel.GRAVITY_URL, MainViewModel.MAX_PAGES, token)
            values.rssiValues = fetchPages(MainViewModel.RSSI_URL, 1, token)
            values.intervalValues = fetchPages(MainViewModel.INTERVAL_URL, 1, token)
            return values
    }


    private fun fetchPages(url: String, maxPages: Int, token: String?): List<Value> {
        val values = mutableListOf<Value>()
        var internalUrl: String? = url
        var pages = 0
        while (internalUrl != null && pages++ <= maxPages) {
            val onePage = fetchOnePageOfValues(internalUrl, null, token)
            values.addAll(onePage.first as? List<Value> ?: emptyList())
            internalUrl = onePage.second
        }
        return values
    }

    private fun fetchOnePageOfValues(url: String, startTime: Long?, token: String?): Pair<List<Value>, String?> {
        var localUrl = url
        startTime?.let {
            localUrl += "?start=$startTime"
        }
        Log.d(MainViewModel.TAG, "fetchOnePageOfValues: $localUrl")
        val client: OkHttpClient = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .url(localUrl)
            .method("GET", null)
            .addHeader(
                MainViewModel.TOKEN_HEADER,
                token.orEmpty()
            )
            .build()
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            Log.d(MainViewModel.TAG, "fetchOnePageOfValues: success")
            response.body?.string()?.let {
                val gson = Gson()
                val resp = gson.fromJson<ValuesResponse>(it, ValuesResponse::class.java)
                return Pair(resp.results.orEmpty(), resp.next)
            }
        } else {
            Log.d(MainViewModel.TAG, "fetchOnePageOfValues: ${response.code}")
        }

        return Pair(emptyList(), null)
    }
}