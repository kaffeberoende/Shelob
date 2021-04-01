package com.rokn.shelob.ui.main

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.rokn.shelob.ui.main.Network.BATTERY_URL
import com.rokn.shelob.ui.main.Network.GRAVITY_URL
import com.rokn.shelob.ui.main.Network.INTERVAL_URL
import com.rokn.shelob.ui.main.Network.RSSI_URL
import com.rokn.shelob.ui.main.Network.TEMPERATURE_URL
import com.rokn.shelob.ui.main.Network.TILT_URL
import com.rokn.shelob.ui.main.data.ValuesCollection
import com.rokn.shelob.ui.main.database.Database
import com.rokn.shelob.ui.main.database.Value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

object Repository {


    fun getData(context: Context, token: String?, currentTime: Long): Flow<ValuesCollection> {

        val database = Room.databaseBuilder(context, Database::class.java, DATABASE_NAME).build()
        val valuesDao = database.valueDao()

        return flow {
            val storedValues = ValuesCollection()

            //First get local data and emit it, should be fast
            storedValues.tiltValues = valuesDao.getAll(type = ValueType.TILT)
            Log.d(TAG, "getData: gotten ${storedValues.tiltValues?.size} tiltvalues from database")
            storedValues.temperatureValues = valuesDao.getAll(type = ValueType.TEMPERATURE)
            storedValues.batteryValues = valuesDao.getAll(type = ValueType.BATTERY)
            storedValues.gravityValues = valuesDao.getAll(type = ValueType.GRAVITY)
            storedValues.rssiValues = valuesDao.getAll(type = ValueType.RSSI)
            storedValues.intervalValues = valuesDao.getAll(type = ValueType.INTERVAL)
            emit(storedValues)

            //Now get any data that is newer than the stored data
            val time = storedValues.tiltValues?.firstOrNull()?.timestamp ?: 0L
            Log.d(TAG, "getData: getting from after $time")
            val latestValues = ValuesCollection()
            latestValues.tiltValues = Network.fetchOnePageOfValues(TILT_URL, time, token).first
            storedValues.tiltValues = (latestValues.tiltValues ?: emptyList()) + (storedValues.tiltValues ?: emptyList())
            emit(storedValues)

            latestValues.temperatureValues = Network.fetchOnePageOfValues(TEMPERATURE_URL, time, token).first
            storedValues.temperatureValues = (latestValues.temperatureValues ?: emptyList()) + (storedValues.temperatureValues ?: emptyList())
            emit(storedValues)

            latestValues.batteryValues = Network.fetchOnePageOfValues(BATTERY_URL, time, token).first
            storedValues.batteryValues = (latestValues.batteryValues ?: emptyList()) + (storedValues.batteryValues ?: emptyList())
            emit(storedValues)

            latestValues.gravityValues = Network.fetchOnePageOfValues(GRAVITY_URL, time, token).first
            storedValues.gravityValues = (latestValues.gravityValues ?: emptyList()) + (storedValues.gravityValues ?: emptyList())
            emit(storedValues)

            latestValues.rssiValues = Network.fetchOnePageOfValues(RSSI_URL, time, token).first
            storedValues.rssiValues = (latestValues.rssiValues ?: emptyList()) + (storedValues.rssiValues ?: emptyList())
            emit(storedValues)

            latestValues.intervalValues = Network.fetchOnePageOfValues(INTERVAL_URL, time, token).first
            storedValues.intervalValues = (latestValues.intervalValues ?: emptyList()) + (storedValues.intervalValues ?: emptyList())
            emit(storedValues)

            //Now get the rest of the data and emit it (page by page?) Använd tiden från den senaste datan här sen?
         /*   val olderValues = Network.fetchData(token = token, start = , end = )
            Log.d(TAG, "getData: olderDataSize")
            emit(newerValues)
*/
            //Store all the new data in room
            val listToStore = mutableListOf<Value>()
            Log.d(TAG, "getData: ${latestValues.tiltValues?.size} values to store")
            latestValues.tiltValues?.forEach {
                listToStore.add(Value(type = ValueType.TILT, timestamp = it.timestamp, value = it.value, created_at = it.created_at))
            }
            latestValues.batteryValues?.forEach {
                listToStore.add(Value(type = ValueType.BATTERY, timestamp = it.timestamp, value = it.value, created_at = it.created_at))
            }
            latestValues.gravityValues?.forEach {
                listToStore.add(Value(type = ValueType.GRAVITY, timestamp = it.timestamp, value = it.value, created_at = it.created_at))
            }
            latestValues.temperatureValues?.forEach {
                listToStore.add(Value(type = ValueType.TEMPERATURE, timestamp = it.timestamp, value = it.value, created_at = it.created_at))
            }
            latestValues.rssiValues?.forEach {
                listToStore.add(Value(type = ValueType.RSSI, timestamp = it.timestamp, value = it.value, created_at = it.created_at))
            }
            latestValues.intervalValues?.forEach {
                listToStore.add(Value(type = ValueType.INTERVAL, timestamp = it.timestamp, value = it.value, created_at = it.created_at))
            }
            database.valueDao().insert(listToStore)

        }.flowOn(Dispatchers.IO)
    }

    const val DATABASE_NAME = "database-name"
    private const val TAG = "SPIN_REPOSITORY"
}