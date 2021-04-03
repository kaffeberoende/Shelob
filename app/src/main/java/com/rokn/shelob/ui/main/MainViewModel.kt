package com.rokn.shelob.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.rokn.shelob.ui.main.data.ValuesCollection
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _data = MutableLiveData<ValuesCollection>()
    val data: LiveData<ValuesCollection> get() = _data

    val isLoggedIn = MutableLiveData(true)

    fun login(context: Context) {
        viewModelScope.launch {
            isLoggedIn.value = LoginHelper.login(context)
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
            Repository.getData(context = context, token = token)
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
        const val TAG = "SPINDEL MODEL"
        const val SHARED_PREFS = "prefs"
        const val TOKEN = "token"
        const val BASE_URL = "https://industrial.api.ubidots.com/api/v1.6/"
        const val TOKEN_URL = "${BASE_URL}auth/token/"
        const val API_KEY_HEADER = "x-ubidots-apikey"
        const val API_KEY = "api_key"
        const val DEVICE_NAME = "device_name"
        const val TOKEN_HEADER = "X-Auth-Token"
        const val START_TIME = "start_time"
    }
}