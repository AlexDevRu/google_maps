package com.example.maps.ui.place_info

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.domain.common.Result
import com.example.domain.models.place_info.PlaceInfo
import com.example.maps.R
import com.example.maps.databinding.LayoutPlaceInfoBinding
import com.example.maps.ui.adapters.ReviewAdapter
import com.example.maps.ui.base.BaseFragment
import com.example.maps.ui.main.MainVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaceInfoFragment: BaseFragment<LayoutPlaceInfoBinding>(LayoutPlaceInfoBinding::inflate) {

    private val mainVM: MainVM by activityViewModels()

    private lateinit var reviewAdapter: ReviewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewAdapter = ReviewAdapter()
        binding.reviewsList.adapter = reviewAdapter
        observe()
        Log.e("MapsActivity", "${mainVM.placeData}")
    }

    private fun observe() {
        mainVM.placeData.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.placeInfoContainer.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.placeInfoContainer.visibility = View.VISIBLE

                    val place = it.value
                    Log.w("MapsActivity", "place found: ${place}")

                    setCurrentPlaceData(place)
                }
            }
        }
    }

    private fun setCurrentPlaceData(place: PlaceInfo) {
        binding.placeName.text = resources.getString(R.string.place_name, place.name)
        binding.placeAddress.text = resources.getString(R.string.place_address, place.address)

        if(place.phoneNumber != null)
            binding.phoneNumber.text = resources.getString(R.string.place_phone, place.phoneNumber)
        else
            binding.phoneNumber.visibility = View.GONE

        if(place.website != null) {
            val data = resources.getString(R.string.place_website, place.website)
            val content = SpannableString(data)
            content.setSpan(UnderlineSpan(), 10, data.length, 0)
            binding.placeWebsite.text = content
        } else {
            binding.placeWebsite.visibility = View.GONE
        }

        binding.placeTypes.text = resources.getString(R.string.place_types, place.types?.joinToString(", "))

        binding.placeRating.rating = place.rating?.toFloat() ?: 0f
        binding.ratingText.text = if(place.rating != null) "%.1f".format(place.rating) else "0"

        reviewAdapter.submitList(place.reviews)
    }
}