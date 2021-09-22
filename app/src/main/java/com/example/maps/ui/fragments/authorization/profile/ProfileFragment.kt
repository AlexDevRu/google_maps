package com.example.maps.ui.fragments.authorization.profile

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.maps.databinding.FragmentProfileBinding
import com.example.maps.ui.fragments.base.BaseFragment
import com.example.maps.utils.extensions.url
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ProfileFragment: BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signOutButton.setOnClickListener {
            globalVM.signOut()
            val action = ProfileFragmentDirections.actionProfileFragmentToSignInFragment()
            findNavController().navigate(action)
        }

        globalVM.isSignedIn.observe(viewLifecycleOwner) {
            if(it) {
                binding.profilePhoto.url(Firebase.auth.currentUser?.photoUrl.toString())
                binding.profileName.text = Firebase.auth.currentUser?.displayName
            }
        }
    }
}