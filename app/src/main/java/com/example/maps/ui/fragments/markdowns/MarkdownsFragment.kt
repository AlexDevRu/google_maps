package com.example.maps.ui.fragments.markdowns

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.maps.databinding.FragmentMarkdownsBinding
import com.example.maps.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import com.example.domain.common.Result
import com.example.maps.ui.adapters.MarkdownAdapter

@AndroidEntryPoint
class MarkdownsFragment: BaseFragment<FragmentMarkdownsBinding>(FragmentMarkdownsBinding::inflate) {

    private val viewModel by viewModels<MarkdownsVM>()

    private lateinit var adapter: MarkdownAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MarkdownAdapter(viewModel::deleteMarkdown)
        binding.markdownsList.adapter = adapter

        viewModel.getMarkdowns()
        observe()
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
    }
}