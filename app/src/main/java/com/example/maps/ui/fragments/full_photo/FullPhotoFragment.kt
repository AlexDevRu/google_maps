package com.example.maps.ui.fragments.full_photo

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.example.maps.databinding.FragmentFullPhotoBinding
import com.example.maps.ui.base.BaseFragment
import com.example.maps.utils.extensions.url

class FullPhotoFragment: BaseFragment<FragmentFullPhotoBinding>(FragmentFullPhotoBinding::inflate) {

    private val args: FullPhotoFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.transitionName = args.photoUrl
        binding.imageView.url(args.photoUrl)
    }
}