package com.rokn.shelob.ui.main

import android.util.Log
import com.google.gson.Gson
import com.rokn.shelob.ui.main.data.ValuesCollection
import com.rokn.shelob.ui.main.data.ValuesResponse
import com.rokn.shelob.ui.main.database.Value
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object Network {

    fun fetchData(token: String?, start: Long, end: Long): ValuesCollection {
        //TODO handle failures! 401! Throw exception?
        val values = ValuesCollection()
        values.tiltValues = fetchPages(TILT_URL, token)
        values.temperatureValues = fetchPages(TEMPERATURE_URL, token)
        values.batteryValues = fetchPages(BATTERY_URL, token)
        values.gravityValues = fetchPages(GRAVITY_URL, token)
        values.rssiValues = fetchPages(RSSI_URL, token)
        values.intervalValues = fetchPages(INTERVAL_URL, token)
        return values
    }


    private fun fetchPages(url: String, token: String?): List<Value> {
        val values = mutableListOf<Value>()
        var internalUrl: String? = url
        var pages = 0
        while (internalUrl != null && pages++ <= MAX_PAGES) {
            val onePage = fetchOnePageOfValues(internalUrl, null, token)
            values.addAll(onePage.first as? List<Value> ?: emptyList())
            internalUrl = onePage.second
        }
        return values
    }

    fun fetchOnePageOfValues(url: String, startTime: Long?, token: String?): Pair<List<Value>, String?> {
        var localUrl = url
        startTime?.let {
            localUrl += "?start=${startTime + 1}"
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
            throw Exception("Network exception: ${response.code}")
        }

        return Pair(emptyList(), null)
    }

    private const val VALUES_BASE_URL = "${MainViewModel.BASE_URL}devices/ispindel000/"
    const val TILT_URL = "${VALUES_BASE_URL}tilt/values/"
    const val TEMPERATURE_URL = "${VALUES_BASE_URL}temperature/values/"
    const val BATTERY_URL = "${VALUES_BASE_URL}battery/values/"
    const val GRAVITY_URL = "${VALUES_BASE_URL}gravity/values/"
    const val RSSI_URL = "${VALUES_BASE_URL}rssi/values/"
    const val INTERVAL_URL = "${VALUES_BASE_URL}interval/values/"
    private const val MAX_PAGES = 2
}