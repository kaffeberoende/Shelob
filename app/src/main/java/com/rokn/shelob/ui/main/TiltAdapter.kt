package com.rokn.shelob.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rokn.shelob.R
import com.rokn.shelob.ui.main.data.ValuesCollection
import java.text.SimpleDateFormat
import java.util.*

class TiltAdapter(): RecyclerView.Adapter<ValuesViewHolder>() {

    var values = ValuesCollection()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ValuesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tilt_value_view, parent, false)

        return ValuesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ValuesViewHolder, position: Int) {
        holder.timeValue.text = parseTimeAndDate(values.tiltValues?.getOrNull(position)?.timestamp)
        holder.tiltValue.text = values.tiltValues?.getOrNull(position)?.value ?: "no value"
        holder.temperatureValue.text = values.temperatureValues?.getOrNull(position)?.value ?: "no value"
        holder.batteryValue.text = values.batteryValues?.getOrNull(position)?.value ?: "no value"
        holder.gravityValue.text = values.gravityValues?.getOrNull(position)?.value ?: "no value"
        holder.rssiValue.text = values.rssiValues?.getOrNull(position)?.value ?: "no value"
        holder.intervalValue.text = values.intervalValues?.getOrNull(position)?.value ?: "no value"
    }

    override fun getItemCount(): Int {
        return values.tiltValues?.size ?: 0
    }

    private fun parseTimeAndDate(long: Long?): String {
        return if (long == null) {
            "no value"
        } else {
            val date = Date(long)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            format.format(date)
        }
    }
}