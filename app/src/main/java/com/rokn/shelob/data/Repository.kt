package com.rokn.shelob.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.rokn.shelob.data.Network.getUrlForType
import com.rokn.shelob.graphview.GraphViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

object Repository {


    fun getData(context: Context, token: String?): Flow<ValuesCollection> {

        val database = Room.databaseBuilder(context, Database::class.java, DATABASE_NAME).build()
        val valuesDao = database.valueDao()

        return flow {
            val storedValues = ValuesCollection()

            //Local data
            ValueType.values().forEach {
                storedValues.addAllForType(valuesDao.getAll(it), it)
                Log.d(TAG,
                    "getData: gotten ${storedValues.getValuesForType(it).size} ${it.name} from database")
            }
            emit(storedValues)


            val time = storedValues.tiltValues.firstOrNull()?.timestamp ?: getStartTime(context = context)
            Log.d(TAG, "getData: getting from after $time")

            //Data from network
            val newValuesToStore = ValuesCollection()
            ValueType.values().forEach {
                val newValues = Network.fetchOnePageOfValues(getUrlForType(it), time, token).first
                newValuesToStore.addAllForType(newValues, it)
                storedValues.addAllForType(newValues, it)
                storedValues.getValuesForType(it).sortByDescending { value ->
                    value.timestamp
                }

                emit(storedValues)
            }

            //Now get the rest of the data and emit it (page by page?) Använd tiden från den senaste datan här sen?
            /*   val olderValues = Network.fetchData(token = token, start = , end = )
               Log.d(TAG, "getData: olderDataSize")
               emit(newerValues)
            */
            //Store all the new data in room

            ValueType.values().forEach {
                val valuesOfAType = newValuesToStore.getValuesForType(it)//TODO för många värden med här i av någon anledning
                Log.d(TAG, "Storing ${valuesOfAType.size} $it values to store")
                valuesOfAType.forEach {value ->
                    value.type = it
                }
                database.valueDao().insert(valuesOfAType)
            }

        }.flowOn(Dispatchers.IO)
    }

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