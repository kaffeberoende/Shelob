package com.rokn.shelob.graphview

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.rokn.shelob.rawview.RawDataFragment
import com.rokn.shelob.R
import com.rokn.shelob.data.Repository
import java.util.concurrent.TimeUnit

class GraphFragment: Fragment() {

    private val model by viewModels<GraphViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.graph_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<Button>(R.id.loginbutton)
        loginButton.setOnClickListener {
            model.login(requireContext())
        }

        if (model.isLoggedIn.value == true) {
            loginButton.visibility = View.GONE
            showProgressBar(true)
            model.fetchData(requireContext())

        } else {
            Log.d(RawDataFragment.TAG, "Not logged in")
            loginButton.visibility = View.VISIBLE
        }

        val refreshButton = view.findViewById<Button>(R.id.refresh_button)
        refreshButton.setOnClickListener {
            model.fetchData(requireContext())//TODO change to pull to refresh
        }

        model.data.observe(viewLifecycleOwner, { values ->

            val gravityGraph = view.findViewById<LineChart>(R.id.gravity_chart)

            //TODO move to viewmodel and observe a list of Entrys?
            val gravityData = values.gravityValues.map { value ->
                Log.d(TAG, "adding Entry(${formatTime(value.timestamp)}, ${value.value?.toFloat() ?: 0F}")
                Entry(formatTime(value.timestamp), value.value?.toFloat() ?: 0F)
            }

            val gravityDataset = LineDataSet(gravityData, "Specific Gravity")
            gravityDataset.setCircleColor(Color.BLUE)
            gravityDataset.setDrawValues(true)
            gravityGraph.data = LineData(gravityDataset)
            gravityGraph.notifyDataSetChanged()
            gravityGraph.invalidate()

            val temperatureData = values.temperatureValues.map { value ->
                Log.d(TAG, "adding Entry(${formatTime(value.timestamp)}, ${value.value?.toFloat() ?: 0F}")
                Entry(formatTime(value.timestamp), value.value?.toFloat() ?: 0F)
            }

            val temperatureGraph = view.findViewById<LineChart>(R.id.temperature_chart)
            val temperatureDataset = LineDataSet(temperatureData, "Temperature")
            temperatureDataset.setCircleColor(Color.RED)
            temperatureDataset.setDrawValues(true)


            temperatureGraph.data = LineData(temperatureDataset)
            temperatureGraph.notifyDataSetChanged()
            temperatureGraph.invalidate()
            showProgressBar(false)
        })
    }

    private fun formatTime(time: Long): Float {
        return TimeUnit.MILLISECONDS.toMinutes(time - Repository.getStartTime(context = requireContext())).toFloat()
    }

    private fun showProgressBar(visible: Boolean) {
        Log.d(TAG, "showProgressBar: $visible")
        view?.findViewById<ProgressBar>(R.id.loading_indicator)?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "SPIN Graph Fragment"
    }
}