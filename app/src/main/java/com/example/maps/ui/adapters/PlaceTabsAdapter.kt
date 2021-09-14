package com.example.maps.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.maps.ui.fragments.place_info.PlaceInfoFragment
import com.example.maps.ui.fragments.place_info.PlacePhotosFragment

class PlaceTabsAdapter(fragmentManager: FragmentManager, lifeCycle: Lifecycle):
    FragmentStateAdapter(fragmentManager, lifeCycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        if(position == 0) return PlaceInfoFragment()
        return PlacePhotosFragment()
    }
}