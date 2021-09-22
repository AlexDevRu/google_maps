package com.example.maps.ui.fragments.place_info

import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.domain.common.Result
import com.example.domain.models.place_info.PlaceInfo
import com.example.maps.databinding.LayoutPlaceReviewsBinding
import com.example.maps.ui.adapters.ReviewAdapter
import com.example.maps.ui.fragments.base.BaseFragment
import com.example.maps.ui.fragments.main.MainVM
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class PlaceReviewsFragment: BaseFragment<LayoutPlaceReviewsBinding>(LayoutPlaceReviewsBinding::inflate) {

    //private val mainVM: MainVM by activityViewModels()
    private val mainVM by sharedViewModel<MainVM>()

    private lateinit var reviewAdapter: ReviewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewAdapter = ReviewAdapter()
        binding.reviewsList.adapter = reviewAdapter

        binding.reviewsList.retryHandler = {
            mainVM.retry()
        }

        observe()
    }

    private fun observe() {
        mainVM.placeData.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.reviewsList.isLoading = true
                }
                is Result.Success -> {
                    binding.reviewsList.isLoading = false

                    val place = it.value
                    Log.w("MapsActivity", "place found: ${place}")

                    setCurrentPlaceData(place)
                }
            }
        }
    }

    private fun setCurrentPlaceData(place: PlaceInfo) {
        binding.placeRating.rating = place.rating?.toFloat() ?: 0f
        binding.ratingText.text = if(place.rating != null) "%.1f".format(place.rating) else "0"

        reviewAdapter.submitList(place.reviews)
        binding.reviewsList.isResultEmpty = place.reviews.isNullOrEmpty()
    }
}