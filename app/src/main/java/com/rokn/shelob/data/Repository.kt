package com.rokn.shelob.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.rokn.shelob.graphview.GraphViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

object Repository {

    fun getDataOfOneType(context: Context, token: String?, vararg type: ValueType): Flow<ValuesCollection> {

        val database = Room.databaseBuilder(context, Database::class.java, DATABASE_NAME).build()
        val valuesDao = database.valueDao()

        return flow {
            val storedValues = ValuesCollection()

            type.forEach {
                storedValues.addAllForType(valuesDao.getAll(type = it), it)

                val time = storedValues.getValuesForType(it).firstOrNull()?.timestamp ?: getStartTime(context = context)
                Log.d(TAG, "getDataOfOneType: getting from after $time")
                storedValues.addAllForType(
                    Network.fetchPagesOfType(startTime = time, token = token, type = it), it)

                //TODO Fulfix för calibration
                if (it == ValueType.GRAVITY) {
                    val cali = context.getSharedPreferences(
                        GraphViewModel.SHARED_PREFS,
                        Context.MODE_PRIVATE
                    ).getFloat(
                        GraphViewModel.CALIBRATION, 0F
                    )
                    val calibratedGravity = storedValues.gravityValues.map { g ->
                        Value(
                            uid = g.uid,
                            type = ValueType.CALIBRATED_GRAVITY,
                            timestamp = g.timestamp,
                            created_at = g.created_at,
                            value = (g.value?.toFloat()?.plus(cali)).toString()
                        )
                    }
                    storedValues.addAllForType(calibratedGravity, ValueType.CALIBRATED_GRAVITY)
                    // mpandroidchart wont draw anything that isn't sorted in ascending order
                    storedValues.getValuesForType(ValueType.CALIBRATED_GRAVITY).sortBy { value ->
                        value.timestamp
                    }
                }

                // mpandroidchart wont draw anything that isn't sorted in ascending order
                storedValues.getValuesForType(it).sortBy { value ->
                    value.timestamp
                }

                emit(storedValues)
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun getStartTime(context: Context) =
        context.getSharedPreferences(GraphViewModel.SHARED_PREFS, Context.MODE_PRIVATE).getLong(
            GraphViewModel.START_TIME, 0)

    const val DATABASE_NAME = "database-name"
    private const val TAG = "SPIN_REPOSITORY"
}