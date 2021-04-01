package com.rokn.shelob.ui.main.data

import com.rokn.shelob.ui.main.database.Value

class ValuesCollection() {
    var tiltValues: List<Value>? = null
    var temperatureValues: List<Value>? = null
    var batteryValues: List<Value>? = null
    var gravityValues: List<Value>? = null
    var rssiValues: List<Value>? = null
    var intervalValues: List<Value>? = null
}