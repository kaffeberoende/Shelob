package com.rokn.shelob.ui.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.DateFormat
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
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
import com.rokn.shelob.ui.main.SettingsFragment.Companion.TAG
import com.rokn.shelob.ui.main.database.Database
import kotlinx.coroutines.CoroutineScope
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
        val prefs = requireContext().getSharedPreferences(MainViewModel.SHARED_PREFS, Context.MODE_PRIVATE)

        apiKeyInput.setText(prefs?.getString(MainViewModel.API_KEY, "no key"))
        view.findViewById<Button>(R.id.api_key_button).setOnClickListener {
            prefs?.edit(commit = true) {
                putString(MainViewModel.API_KEY, apiKeyInput.text.toString())
                Log.d(TAG, "api key stored")
            }

        }

        val deviceNameInput = view.findViewById<EditText>(R.id.device_name_input)
        view.findViewById<Button>(R.id.device_name_button).setOnClickListener {
            prefs?.edit(commit = true) {
                putString(MainViewModel.DEVICE_NAME, deviceNameInput.text.toString())
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
        val prefs = requireContext().getSharedPreferences(MainViewModel.SHARED_PREFS, Context.MODE_PRIVATE)
        prefs?.edit(commit = true) {
            putLong(MainViewModel.START_TIME, c.timeInMillis)
        }
    }
}