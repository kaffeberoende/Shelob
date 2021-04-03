package com.rokn.shelob.graphview

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokn.shelob.data.LoginHelper
import com.rokn.shelob.rawview.RawDataViewModel
import com.rokn.shelob.data.Repository
import com.rokn.shelob.data.ValueType
import com.rokn.shelob.data.Value
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class GraphViewModel: ViewModel() {

    private val _data = MutableLiveData<List<Value>>()
    val data: LiveData<List<Value>> get() = _data

    val isLoggedIn = MutableLiveData(true)

    fun login(context: Context) {
        isLoggedIn.value = LoginHelper.login(context)
    }

    fun fetchData(context: Context) {
        var token = LoginHelper.token
        if (token == null) {
            Log.d(RawDataViewModel.TAG, "fetchData: fetching stored token")
            token = context.getSharedPreferences(RawDataViewModel.SHARED_PREFS, Context.MODE_PRIVATE)
                .getString(RawDataViewModel.TOKEN, null)
        }

        if (token == null) {
            Log.d(RawDataViewModel.TAG, "fetchData: no stored token, returning")
            isLoggedIn.value = false
            return
        }

        viewModelScope.launch {
            Repository.getDataOfOneType(context = context, token = token, type = ValueType.GRAVITY)
                .onStart { /* _foo.value = loading state */ }
                .catch { exception ->
                    Log.d(RawDataViewModel.TAG, "fetchData: exception: $exception")
                    isLoggedIn.value = false
                }
                .collect { values ->
                    _data.value = values
                }
        }
    }
}