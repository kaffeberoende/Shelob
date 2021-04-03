package com.rokn.shelob.data

import android.util.Log
import com.google.gson.Gson
import com.rokn.shelob.rawview.RawDataViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object Network {

    fun fetchData(token: String?, start: Long, end: Long): ValuesCollection {
        val values = ValuesCollection()
        values.tiltValues = fetchPages(TILT_URL, start, token)
        values.temperatureValues = fetchPages(TEMPERATURE_URL, start, token)
        values.batteryValues = fetchPages(BATTERY_URL, start, token)
        values.gravityValues = fetchPages(GRAVITY_URL, start, token)
        values.rssiValues = fetchPages(RSSI_URL, start, token)
        values.intervalValues = fetchPages(INTERVAL_URL, start, token)
        return values
    }

    private fun fetchPages(url: String, startTime: Long?, token: String?): List<Value> {
        var localUrl = url
        startTime?.let {
            localUrl += "?start=${startTime + 1}"
        }
        var internalUrl: String? = localUrl
        var pages = 0
        val values = mutableListOf<Value>()
        while (internalUrl != null && pages++ <= MAX_PAGES) {
            val onePage = fetchOnePageOfValues(internalUrl, null, token)
            values.addAll(onePage.first as? List<Value> ?: emptyList())
            internalUrl = onePage.second
        }
        return values
    }

    fun fetchPagesOfType(startTime: Long?, token: String?, type: ValueType): List<Value> {
        return fetchPages(url = getUrlForType(type), startTime = startTime, token = token)
    }

    fun fetchOnePageOfValues(url: String, startTime: Long?, token: String?): Pair<List<Value>, String?> {
        var localUrl = url
        startTime?.let {
            localUrl += "?start=${startTime + 1}"
        }
        Log.d(RawDataViewModel.TAG, "fetchOnePageOfValues: $localUrl")
        val client: OkHttpClient = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .url(localUrl)
            .method("GET", null)
            .addHeader(
                RawDataViewModel.TOKEN_HEADER,
                token.orEmpty()
            )
            .build()
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            Log.d(RawDataViewModel.TAG, "fetchOnePageOfValues: success")
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

    private fun getUrlForType(type: ValueType): String {
        return when(type) {
            ValueType.TILT -> TILT_URL
            ValueType.TEMPERATURE -> TEMPERATURE_URL
            ValueType.BATTERY -> BATTERY_URL
            ValueType.GRAVITY -> GRAVITY_URL
            ValueType.RSSI -> RSSI_URL
            ValueType.INTERVAL -> INTERVAL_URL
        }
    }

    private const val VALUES_BASE_URL = "${RawDataViewModel.BASE_URL}devices/ispindel000/"
    const val TILT_URL = "${VALUES_BASE_URL}tilt/values/"
    const val TEMPERATURE_URL = "${VALUES_BASE_URL}temperature/values/"
    const val BATTERY_URL = "${VALUES_BASE_URL}battery/values/"
    const val GRAVITY_URL = "${VALUES_BASE_URL}gravity/values/"
    const val RSSI_URL = "${VALUES_BASE_URL}rssi/values/"
    const val INTERVAL_URL = "${VALUES_BASE_URL}interval/values/"
    private const val MAX_PAGES = 2
}