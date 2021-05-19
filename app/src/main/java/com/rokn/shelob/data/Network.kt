package com.rokn.shelob.data

import android.util.Log
import com.google.gson.Gson
import com.rokn.shelob.graphview.GraphViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object Network {

    fun fetchPagesOfType(startTime: Long?, token: String?, type: ValueType): List<Value> {
        return fetchPages(url = getUrlForType(type), startTime = startTime, token = token)
    }

    fun fetchOnePageOfValues(url: String, startTime: Long?, token: String?): Pair<List<Value>, String?> {
        var localUrl = url
        startTime?.let {
            localUrl += "?start=${startTime + 1}"
        }
        Log.d(TAG, "fetchOnePageOfValues: $localUrl")
        val client: OkHttpClient = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .url(localUrl)
            .method("GET", null)
            .addHeader(
                GraphViewModel.TOKEN_HEADER,
                token.orEmpty()
            )
            .build()
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            Log.d(TAG, "fetchOnePageOfValues: success")
            response.body?.string()?.let {
                val gson = Gson()
                val resp = gson.fromJson(it, ValuesResponse::class.java)
                return Pair(resp.results.orEmpty(), resp.next)
            }
        } else {
            throw Exception("Network exception: ${response.code}")
        }

        return Pair(emptyList(), null)
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

    //TODO move to ValueType class?
    fun getUrlForType(type: ValueType): String {
        return when(type) {
            ValueType.TILT -> TILT_URL
            ValueType.TEMPERATURE -> TEMPERATURE_URL
            ValueType.BATTERY -> BATTERY_URL
            ValueType.GRAVITY -> GRAVITY_URL
            ValueType.RSSI -> RSSI_URL
            ValueType.INTERVAL -> INTERVAL_URL
            ValueType.CALIBRATED_GRAVITY -> ""
        }
    }

    private const val VALUES_BASE_URL = "${GraphViewModel.BASE_URL}devices/ispindel000/"
    private const val TILT_URL = "${VALUES_BASE_URL}tilt/values/"
    private const val TEMPERATURE_URL = "${VALUES_BASE_URL}temperature/values/"
    private const val BATTERY_URL = "${VALUES_BASE_URL}battery/values/"
    private const val GRAVITY_URL = "${VALUES_BASE_URL}gravity/values/"
    private const val RSSI_URL = "${VALUES_BASE_URL}rssi/values/"
    private const val INTERVAL_URL = "${VALUES_BASE_URL}interval/values/"
    private const val MAX_PAGES = 2
    private const val TAG = "SPINDEL network"
}