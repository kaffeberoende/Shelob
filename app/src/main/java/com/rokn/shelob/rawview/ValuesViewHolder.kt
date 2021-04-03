package com.rokn.shelob.rawview

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rokn.shelob.R

class ValuesViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val tiltValue: TextView = view.findViewById(R.id.tilt_value)
    val timeValue: TextView = view.findViewById(R.id.time_value)
    val temperatureValue: TextView = view.findViewById(R.id.temp_value)
    val batteryValue: TextView = view.findViewById(R.id.battery_value)
    val rssiValue: TextView = view.findViewById(R.id.rssi_value)
    val gravityValue: TextView = view.findViewById(R.id.gravity_value)
    val intervalValue: TextView = view.findViewById(R.id.interval_value)
}