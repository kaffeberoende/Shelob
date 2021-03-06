package com.rokn.shelob.graphview

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokn.shelob.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class GraphViewModel: ViewModel() {

    private val _data = MutableLiveData<ValuesCollection>()
    val data: LiveData<ValuesCollection> get() = _data

    val isLoggedIn = MutableLiveData(true)

    fun login(context: Context) {
        val loggedIn = LoginHelper.login(context)
        viewModelScope.launch(Dispatchers.Main) {
            isLoggedIn.value = loggedIn
        }
    }

    fun fetchData(context: Context) {
        var token = LoginHelper.token
        if (token == null) {
            Log.d(TAG, "fetchData: fetching stored token")
            token = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                .getString(TOKEN, null)
        }

        if (token == null) {
            Log.d(TAG, "fetchData: no stored token, returning")
            isLoggedIn.value = false
            return
        }

        viewModelScope.launch {
            Repository.getDataOfOneType(context = context, token = token, ValueType.GRAVITY, ValueType.TEMPERATURE, ValueType.BATTERY)
                .onStart { /* _foo.value = loading state */ }
                .catch { exception ->
                    Log.d(TAG, "fetchData: exception: $exception")
                    isLoggedIn.value = false
                }
                .collect { values ->
                    _data.value = values
                }
        }
    }

    companion object {
        private const val TAG = "SPINDEL GRAPH MODEL"
        const val SHARED_PREFS = "prefs"
        const val TOKEN = "token"
        const val BASE_URL = "https://industrial.api.ubidots.com/api/v1.6/"
        const val TOKEN_URL = "${BASE_URL}auth/token/"
        const val API_KEY_HEADER = "x-ubidots-apikey"
        const val API_KEY = "api_key"
        const val DEVICE_NAME = "device_name"
        const val CALIBRATION = "calibration"
        const val TOKEN_HEADER = "X-Auth-Token"
        const val START_TIME = "start_time"
    }
}