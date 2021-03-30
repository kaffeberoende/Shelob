package com.rokn.shelob.ui.main

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.*
import com.google.gson.Gson
import com.rokn.shelob.R
import com.rokn.shelob.ui.main.data.TokenResponse
import com.rokn.shelob.ui.main.data.ValuesCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.toRequestBody

class MainViewModel : ViewModel() {
    private val _data = MutableLiveData<ValuesCollection>()
    val data: LiveData<ValuesCollection> get() = _data

    val isLoggedIn = MutableLiveData(true)

    private var token: String? = null

    fun login(context: Context) {
        Log.d(TAG, "login: ")
        val apiKey = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).getString(
            API_KEY, null
        )
        if (apiKey != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val client = OkHttpClient().newBuilder()
                    .build()
                val mediaType: MediaType = "text/plain".toMediaType()
                val body: RequestBody = "".toRequestBody(mediaType)
                val request = Builder()
                    .url(TOKEN_URL)
                    .method("POST", body)
                    .addHeader(API_KEY_HEADER, apiKey)
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let {
                        val gson = Gson()
                        viewModelScope.launch(Dispatchers.Main) {
                            token =
                                gson.fromJson<TokenResponse>(
                                    it, TokenResponse::class.java
                                ).token ?: ""
                            context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                                .edit(commit = true) {
                                    putString(TOKEN, token)
                                }
                            isLoggedIn.value = true
                        }
                    }

                }
            }
        } else {
            Toast.makeText(context, R.string.no_api_key_set, Toast.LENGTH_SHORT).show()
        }
    }

    fun fetchData(context: Context) {
        if (token == null) {
            Log.d(MainViewModel.TAG, "fetchData: fetching stored token")
            token = context.getSharedPreferences(MainViewModel.SHARED_PREFS, Context.MODE_PRIVATE)
                .getString(MainViewModel.TOKEN, null)
        }

        if (token == null) {
            Log.d(MainViewModel.TAG, "fetchData: no stored token, returning")
            isLoggedIn.value = false
            return
        }

        viewModelScope.launch {
            Repository.getData(token = token, currentTime = System.currentTimeMillis())
                .onStart { /* _foo.value = loading state */ }
                .catch { exception -> /* _foo.value = error state */ }
                .collect { values ->
                    _data.value = values
                }
        }
    }


    companion object {
        const val TAG = "SPINDEL MODEL"
        const val SHARED_PREFS = "prefs"
        const val TOKEN = "token"
        private const val BASE_URL = "https://industrial.api.ubidots.com/api/v1.6/"
        const val TOKEN_URL = "${BASE_URL}auth/token/"
        const val API_KEY_HEADER = "x-ubidots-apikey"
        const val API_KEY = "api_key"
        const val DEVICE_NAME = "device_name"
        private const val VALUES_BASE_URL = "${BASE_URL}devices/ispindel000/"
        const val TILT_URL = "${VALUES_BASE_URL}tilt/values/"
        const val TEMPERATURE_URL = "${VALUES_BASE_URL}temperature/values/"
        const val BATTERY_URL = "${VALUES_BASE_URL}battery/values/"
        const val GRAVITY_URL = "${VALUES_BASE_URL}gravity/values/"
        const val RSSI_URL = "${VALUES_BASE_URL}rssi/values/"
        const val INTERVAL_URL = "${VALUES_BASE_URL}interval/values/"
        const val TOKEN_HEADER = "X-Auth-Token"
        const val MAX_PAGES = 10
    }
}