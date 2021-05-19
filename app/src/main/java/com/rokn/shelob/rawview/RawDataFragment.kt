package com.rokn.shelob.rawview

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rokn.shelob.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RawDataFragment : Fragment() {

    private val model by viewModels<RawDataViewModel>()
    private val adapter = TiltAdapter()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.tilt_values)
        view.findViewById<ImageView>(R.id.settings_button).setOnClickListener {
            gotoSettings()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        model.isLoggedIn.observe(viewLifecycleOwner, {
            Log.d(TAG, "observed login: $it")
            if (it) {
                showProgressBar(true)
                model.fetchData(requireContext())
                recyclerView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.GONE
                Log.d(TAG, "Logging in")
                lifecycleScope.launch(Dispatchers.IO) {
                    model.login(requireContext())
                }
            }
        })

        model.data.observe(viewLifecycleOwner, { values ->
            if (values.tiltValues.isNullOrEmpty())
                return@observe
            adapter.values = values
            adapter.notifyDataSetChanged()
            showProgressBar(false)
        })

        if (model.isLoggedIn.value == true) {
            showProgressBar(true)
            model.fetchData(requireContext())
        } else {
            Log.d(TAG, "Logging in")
            lifecycleScope.launch(Dispatchers.IO) {
                model.login(requireContext())
            }
        }
    }

    private fun showProgressBar(visible: Boolean) {
        view?.findViewById<ProgressBar>(R.id.loading_indicator)?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun gotoSettings() {
        findNavController().navigate(R.id.settingsFragment)
    }

    companion object {
        const val TAG =  "SPINDEL"
    }
}