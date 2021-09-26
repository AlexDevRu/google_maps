package com.example.maps.ui.fragments.markdowns

import android.os.Bundle
import android.view.View
import com.example.maps.databinding.FragmentMarkdownsBinding
import com.example.maps.ui.adapters.MarkdownAdapter
import com.example.maps.ui.fragments.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.github.core.common.Result

class MarkdownsFragment: BaseFragment<FragmentMarkdownsBinding>(FragmentMarkdownsBinding::inflate) {

    private val viewModel by viewModel<MarkdownsVM>()

    private lateinit var adapter: MarkdownAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe()

        adapter = MarkdownAdapter(viewModel::deleteMarkdown)
        binding.markdownsList.adapter = adapter
        binding.markdownsList.retryHandler = {
            viewModel.getMarkdowns()
        }

        viewModel.getMarkdowns()

        initSwipeToDelete()
    }

    private fun observe() {
        viewModel.markdowns.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.markdownsList.isLoading = true
                }
                is Result.Success -> {
                    adapter.submitList(it.value)
                    binding.markdownsList.isResultEmpty = it.value.isNullOrEmpty()
                    binding.markdownsList.isLoading = false
                }
                is Result.Failure -> {
                    binding.markdownsList.errorMessage = it.throwable.message
                    binding.markdownsList.isLoading = false
                }
            }
        }
    }

    private fun initSwipeToDelete() {
        binding.markdownsList.addSwipeToDelete {
            (it as MarkdownAdapter.MarkdownViewHolder).delete()
        }
    }
}