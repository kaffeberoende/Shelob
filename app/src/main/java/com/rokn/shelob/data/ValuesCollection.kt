package com.rokn.shelob.data

class ValuesCollection {
    var tiltValues: MutableList<Value> = mutableListOf()
    var temperatureValues: MutableList<Value> = mutableListOf()
    var batteryValues: MutableList<Value> = mutableListOf()
    var gravityValues: MutableList<Value> = mutableListOf()
    var rssiValues: MutableList<Value> = mutableListOf()
    var intervalValues: MutableList<Value> = mutableListOf()
    var calibratedGravityValues: MutableList<Value> = mutableListOf()

    fun addAllForType(values: List<Value>, type: ValueType) {
        when(type) {
            ValueType.TILT -> tiltValues.addAll(values)
            ValueType.TEMPERATURE -> temperatureValues.addAll(values)
            ValueType.BATTERY -> batteryValues.addAll(values)
            ValueType.GRAVITY -> gravityValues.addAll(values)
            ValueType.RSSI -> rssiValues.addAll(values)
            ValueType.INTERVAL -> intervalValues.addAll(values)
            ValueType.CALIBRATED_GRAVITY -> calibratedGravityValues.addAll(values)
        }
    }

    fun getValuesForType(type: ValueType): MutableList<Value> {
        return when(type) {
            ValueType.TILT -> tiltValues
            ValueType.TEMPERATURE -> temperatureValues
            ValueType.BATTERY -> batteryValues
            ValueType.GRAVITY -> gravityValues
            ValueType.RSSI -> rssiValues
            ValueType.INTERVAL -> intervalValues
            ValueType.CALIBRATED_GRAVITY -> calibratedGravityValues
        }
    }
}