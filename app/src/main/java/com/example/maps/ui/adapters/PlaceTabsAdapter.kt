package com.example.maps.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.maps.ui.fragments.place_info.PlaceInfoFragment
import com.example.maps.ui.fragments.place_info.PlacePhotosFragment
import com.example.maps.ui.fragments.place_info.PlaceReviewsFragment

class PlaceTabsAdapter(fragmentManager: FragmentManager, lifeCycle: Lifecycle):
    FragmentStateAdapter(fragmentManager, lifeCycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> PlaceInfoFragment()
            1 -> PlacePhotosFragment()
            else -> PlaceReviewsFragment()
        }
    }
}