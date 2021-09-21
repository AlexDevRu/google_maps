package com.example.maps.ui.fragments.place_info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.domain.common.Result
import com.example.domain.models.place_info.Photo
import com.example.maps.databinding.LayoutPlacePhotosBinding
import com.example.maps.ui.adapters.PlacePhotosAdapter
import com.example.maps.ui.fragments.base.BaseFragment
import com.example.maps.ui.fragments.main.MainVM
import com.example.maps.utils.extensions.hide
import com.example.maps.utils.extensions.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlacePhotosFragment: BaseFragment<LayoutPlacePhotosBinding>(LayoutPlacePhotosBinding::inflate) {

    private val mainVM: MainVM by activityViewModels()

    private lateinit var photosAdapter: PlacePhotosAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photosAdapter = PlacePhotosAdapter({ startPostponedEnterTransition() })
        binding.placePhotosList.adapter = photosAdapter

        binding.placePhotosList.retryHandler = {
            mainVM.retry()
        }
        binding.placePhotosList.prepareToSharedTransition(parentFragment ?: this)

        observe()
    }

    private fun observe() {
        mainVM.placeData.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.placePhotosList.isLoading = true
                }
                is Result.Success -> {
                    binding.placePhotosList.isLoading = false
                    val place = it.value
                    updatePhotos(place.photos)
                }
            }
        }
    }

    private fun updatePhotos(photos: List<Photo>?) {
        photosAdapter.submitList(photos)
        binding.placePhotosList.isResultEmpty = photos.isNullOrEmpty()
    }
}