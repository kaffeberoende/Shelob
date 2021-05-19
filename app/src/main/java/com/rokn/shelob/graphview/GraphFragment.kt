package com.rokn.shelob.graphview

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.rokn.shelob.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class GraphFragment: Fragment() {

    private val model by viewModels<GraphViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.graph_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (model.isLoggedIn.value == true) {
            showProgressBar(true)
            model.fetchData(requireContext())
        } else {
            Log.d(TAG, "Logging in")
            lifecycleScope.launch(Dispatchers.IO) {
                model.login(requireContext())
            }
        }

        val refreshButton = view.findViewById<Button>(R.id.refresh_button)
        refreshButton.setOnClickListener {
            model.fetchData(requireContext())//TODO change to pull to refresh
        }

        model.data.observe(viewLifecycleOwner, { values ->
            val gravityData = values.calibratedGravityValues.map { value ->
                Entry(value.timestamp.toFloat(), value.value?.toFloat() ?: 0F)
            }

            val gravityGraph = view.findViewById<LineChart>(R.id.gravity_chart)
            setupGraph(gravityGraph, gravityData, "Specific Gravity", Color.BLUE)


            val temperatureData = values.temperatureValues.map { value ->
                Entry(value.timestamp.toFloat(), value.value?.toFloat() ?: 0F)
            }

            val temperatureGraph = view.findViewById<LineChart>(R.id.temperature_chart)
            setupGraph(temperatureGraph, temperatureData, "Temperature", Color.RED, unit = "°C")
            showProgressBar(false)
        })

        model.isLoggedIn.observe(viewLifecycleOwner, {
            Log.d(TAG, "observed login: $it")
            if (it) {
                showProgressBar(true)
                model.fetchData(requireContext())
            } else {
                Log.d(TAG, "Logging in")
                lifecycleScope.launch(Dispatchers.IO) {
                    model.login(requireContext())
                }
            }
        })
    }

    private fun setupGraph(graph: LineChart, data: List<Entry>, label: String, color: Int, unit: String? = null) {
        val dataset = LineDataSet(data, label)
        dataset.color = color
        dataset.setCircleColor(color)
        dataset.setDrawValues(false)
        dataset.setDrawCircles(false)
        dataset.setDrawHighlightIndicators(true)
        graph.description = null
        graph.xAxis.position = XAxis.XAxisPosition.BOTTOM
        graph.xAxis.granularity = 4F
        graph.xAxis.valueFormatter = object: ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return getXAxisLabel(value)
            }
        }
        graph.marker = object: MarkerView(requireContext(), R.layout.graph_marker) {
            override fun refreshContent(e: Entry, highlight: Highlight?) {
                super.refreshContent(e, highlight)
                val markerTime = findViewById<TextView>(R.id.marker_time)
                markerTime.text = getXAxisLabel(e.x)
                markerTime.setTextColor(color)
                val markerValue = findViewById<TextView>(R.id.marker_value)
                markerValue.text = e.y.toString() + unit.orEmpty()
                markerValue.setTextColor(color)
            }

            //TODO override getOffsetForDrawingAtPoint so that it doesn't draw outside the screen!
        }
        graph.data = LineData(dataset)
        val highlight = Highlight(
            dataset.values.lastOrNull()?.x ?: 0F,
            dataset.values.lastOrNull()?.y ?: 0F,
            0
        )
        highlight.dataIndex = 0
        graph.highlightValue(highlight, false)
        graph.setDragOffsetX(50F)

        graph.post {
            //The marker woudln't show if invalidating immediatly :)
            graph.invalidate()
        }

    }

    private fun getXAxisLabel(timestamp: Float): String {
        val localDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp.toLong()),
            ZoneId.systemDefault()
        )
        return localDate.format(DateTimeFormatter.ofPattern("E HH:mm"))
    }


    private fun showProgressBar(visible: Boolean) {
        Log.d(TAG, "showProgressBar: $visible")
        view?.findViewById<ProgressBar>(R.id.loading_indicator)?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "SPIN Graph Fragment"
    }
}