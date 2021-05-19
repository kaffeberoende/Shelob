package com.rokn.shelob.settingsview

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.rokn.shelob.R
import com.rokn.shelob.settingsview.SettingsFragment.Companion.TAG
import com.rokn.shelob.data.Database
import com.rokn.shelob.data.Repository
import com.rokn.shelob.graphview.GraphViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SettingsFragment: Fragment() {

    companion object {
         const val TAG = "SPIN_SETTINGS"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apiKeyInput = view.findViewById<EditText>(R.id.api_key_input)
        val prefs = requireContext().getSharedPreferences(GraphViewModel.SHARED_PREFS, Context.MODE_PRIVATE)

        apiKeyInput.setText(prefs?.getString(GraphViewModel.API_KEY, "no key"))
        view.findViewById<Button>(R.id.api_key_button).setOnClickListener {
            prefs?.edit(commit = true) {
                putString(GraphViewModel.API_KEY, apiKeyInput.text.toString())
                Log.d(TAG, "api key stored")
            }

        }

        val deviceNameInput = view.findViewById<EditText>(R.id.device_name_input)
        view.findViewById<Button>(R.id.device_name_button).setOnClickListener {
            prefs?.edit(commit = true) {
                putString(GraphViewModel.DEVICE_NAME, deviceNameInput.text.toString())
                Log.d(TAG, "device name stored")
            }
        }

        view.findViewById<Button>(R.id.clear_data_button).setOnClickListener {
            Log.d(TAG, "onViewCreated: data cleared")
            val database = Room.databaseBuilder(requireContext(), Database::class.java,
                Repository.DATABASE_NAME
            ).build()
            lifecycleScope.launch(Dispatchers.IO) {
                database.clearAllTables()
            }
        }

        view.findViewById<Button>(R.id.start_time_picker).setOnClickListener {
            DatePickerFragment().show(parentFragmentManager, "datepicker")
        }

        val calibrationInput = view.findViewById<EditText>(R.id.calibration_input)
        view.findViewById<Button>(R.id.calibration_button).setOnClickListener {
            prefs?.edit(commit = true) {
                val value = if (calibrationInput.text.toString().isEmpty()) "0" else calibrationInput.text.toString()
                putFloat(GraphViewModel.CALIBRATION, value.toFloat())
                Log.d(TAG, "calibration stored")
            }
        }
    }
}

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        TimePickerFragment(year, month, day).show(parentFragmentManager, "timepicker")
    }
}

class TimePickerFragment(private val year: Int, private val month: Int, val day: Int) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, day, hourOfDay, minute)
        Log.d(TAG, "onTimeSet: $c")
        val prefs = requireContext().getSharedPreferences(GraphViewModel.SHARED_PREFS, Context.MODE_PRIVATE)
        prefs?.edit(commit = true) {
            putLong(GraphViewModel.START_TIME, c.timeInMillis)
        }
    }
}