package com.rokn.shelob.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rokn.shelob.R
import com.rokn.shelob.settingsview.SettingsFragment
import com.rokn.shelob.graphview.GraphFragment
import com.rokn.shelob.rawview.RawDataFragment

class TabAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> GraphFragment()
            1 -> RawDataFragment()
            else -> SettingsFragment()
        }
    }

    fun getTitle(position: Int): String {
        return when(position) {
            0 -> fragmentActivity.getString(R.string.graph_tab_name)
            1 -> fragmentActivity.getString(R.string.raw_data_tab_name)
            else -> fragmentActivity.getString(R.string.settings_tab_name)
        }
    }
}