package com.example.maps.ui.fragments.authorization

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.maps.R
import com.example.maps.databinding.FragmentAuthBinding
import com.example.maps.ui.base.BaseFragment
import com.example.maps.ui.fragments.profile.ProfileFragmentDirections
import com.example.maps.ui.fragments.sign_in.SignInFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment: BaseFragment<FragmentAuthBinding>(FragmentAuthBinding::inflate) {

    private val viewModel by activityViewModels<AuthVM>()

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_fragment1) as NavHostFragment

        navController = navHostFragment.navController

        observe()
    }

    private fun observe() {
        viewModel.openProfile.observe(viewLifecycleOwner) {
            if(it) {
                val action = SignInFragmentDirections.actionSignInFragmentToProfileFragment()
                navController.navigate(action)
            }
        }

        viewModel.openSignIn.observe(viewLifecycleOwner) {
            if(it) {
                val action = ProfileFragmentDirections.actionProfileFragmentToSignInFragment()
                navController.navigate(action)
            }
        }
    }

}