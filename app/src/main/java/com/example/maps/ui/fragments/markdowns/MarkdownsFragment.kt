package com.example.maps.ui.fragments.markdowns

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.maps.databinding.FragmentMarkdownsBinding
import com.example.maps.ui.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import com.example.domain.common.Result
import com.example.maps.R
import com.example.maps.ui.adapters.MarkdownAdapter
import com.example.maps.ui.fragments.authorization.AuthFragmentDirections
import com.example.maps.ui.fragments.authorization.AuthVM
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@AndroidEntryPoint
class MarkdownsFragment: BaseFragment<FragmentMarkdownsBinding>(FragmentMarkdownsBinding::inflate) {

    private val viewModel by viewModels<MarkdownsVM>()
    private val authVM by activityViewModels<AuthVM>()

    private lateinit var adapter: MarkdownAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe()

        adapter = MarkdownAdapter(viewModel::deleteMarkdown)
        binding.markdownsList.adapter = adapter

        viewModel.getMarkdowns()
    }

    private fun observe() {
        viewModel.markdowns.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.markdownsList.isLoading = true
                }
                is Result.Success -> {
                    adapter.submitList(it.value)
                    binding.markdownsList.isLoading = false
                }
                is Result.Failure -> {
                    binding.markdownsList.errorMessage = it.throwable.message
                }
            }
        }

        val navController = findNavController()

        authVM.isSignedIn.observe(viewLifecycleOwner) {
            Log.w("asd", "current user ${Firebase.auth.currentUser}")
            if(!it && navController.currentDestination?.id != R.id.authFragment) {
                val action = MarkdownsFragmentDirections.actionMarkdownFragmentToAuthFragment()
                findNavController().navigate(action)
            } else if(it && navController.currentDestination?.id != R.id.markdownFragment) {
                val action = AuthFragmentDirections.actionAuthFragmentToMarkdownFragment()
                findNavController().navigate(action)
            }
        }
    }
}