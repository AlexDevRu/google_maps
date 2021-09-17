package com.example.maps.ui.fragments.place_info

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.domain.common.Result
import com.example.domain.models.place_info.PlaceInfo
import com.example.maps.R
import com.example.maps.databinding.LayoutPlaceInfoBinding
import com.example.maps.ui.adapters.ReviewAdapter
import com.example.maps.ui.fragments.base.BaseFragment
import com.example.maps.ui.fragments.main.MainVM
import com.example.maps.utils.extensions.hide
import com.example.maps.utils.extensions.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaceInfoFragment: BaseFragment<LayoutPlaceInfoBinding>(LayoutPlaceInfoBinding::inflate) {

    private val mainVM: MainVM by activityViewModels()

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
                    binding.root.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.root.visibility = View.VISIBLE
                    binding.reviewsList.isLoading = false

                    val place = it.value
                    Log.w("MapsActivity", "place found: ${place}")

                    setCurrentPlaceData(place)
                }
            }
        }

        mainVM.currentPlaceFavorite.observe(viewLifecycleOwner) {
            Log.e("MapsActivity", "currentPlaceFavorite observer $it")
            when(it) {
                is Result.Loading -> {
                    binding.markdownProgressBar.show()
                    binding.markdownButton.hide()
                }
                is Result.Success -> {
                    val color = if(it.value) R.color.red else R.color.black
                    binding.markdownButton.setColorFilter(ContextCompat.getColor(requireContext(), color))
                    binding.markdownButton.show()
                    binding.markdownProgressBar.hide()
                }
                is Result.Failure -> {
                    binding.markdownButton.show()
                    binding.markdownProgressBar.hide()
                }
            }
        }

        binding.markdownButton.setOnClickListener {
            mainVM.toggleFavoriteCurrentPlace()
        }

        globalVM.isSignedIn.observe(viewLifecycleOwner) {
            if(it) binding.markdownButton.show()
            else binding.markdownButton.hide()
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
            binding.placeWebsite.text = data
        } else {
            binding.placeWebsite.visibility = View.GONE
        }

        binding.placeRating.rating = place.rating?.toFloat() ?: 0f
        binding.ratingText.text = if(place.rating != null) "%.1f".format(place.rating) else "0"

        reviewAdapter.submitList(place.reviews)
        binding.reviewsList.isResultEmpty = place.reviews.isNullOrEmpty()
    }
}