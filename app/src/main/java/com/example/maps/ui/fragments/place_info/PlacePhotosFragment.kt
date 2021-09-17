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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlacePhotosFragment: BaseFragment<LayoutPlacePhotosBinding>(LayoutPlacePhotosBinding::inflate) {

    private val mainVM: MainVM by activityViewModels()

    private lateinit var photosAdapter: PlacePhotosAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photosAdapter = PlacePhotosAdapter()
        binding.placePhotosList.adapter = photosAdapter

        binding.placePhotosList.retryHandler = {
            mainVM.retry()
        }
        binding.placePhotosList.prepareToSharedTransition(this)

        observe()
    }

    private fun observe() {
        mainVM.placeData.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.placePhotosList.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.placePhotosList.visibility = View.VISIBLE
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