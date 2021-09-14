package com.example.maps.ui.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.maps.databinding.FragmentProfileBinding
import com.example.maps.ui.base.BaseFragment
import com.example.maps.ui.fragments.authorization.AuthVM
import com.example.maps.utils.extensions.url
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment: BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private val authVM by activityViewModels<AuthVM>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signOutButton.setOnClickListener {
            authVM.openSignIn()
        }
        binding.profilePhoto.url(Firebase.auth.currentUser?.photoUrl.toString())
        binding.profileName.text = Firebase.auth.currentUser?.displayName
    }
}