package com.rokn.shelob.ui.main

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.rokn.shelob.R

class SettingsFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apiKeyInput = view.findViewById<EditText>(R.id.api_key_input)
        val prefs = context?.getSharedPreferences(MainViewModel.SHARED_PREFS, Context.MODE_PRIVATE)

        apiKeyInput.setText(prefs?.getString(MainViewModel.API_KEY, "no key"))
        view.findViewById<Button>(R.id.api_key_button).setOnClickListener {
            prefs?.edit(commit = true) {
                putString(MainViewModel.API_KEY, apiKeyInput.text.toString())
                Log.d("SPINDEL", "api key stored")
            }

        }

        val deviceNameInput = view.findViewById<EditText>(R.id.device_name_input)
        view.findViewById<Button>(R.id.device_name_button).setOnClickListener {
            prefs?.edit(commit = true) {
                putString(MainViewModel.DEVICE_NAME, deviceNameInput.text.toString())
                Log.d("SPINDEL", "device name stored")
            }
        }

    }
}