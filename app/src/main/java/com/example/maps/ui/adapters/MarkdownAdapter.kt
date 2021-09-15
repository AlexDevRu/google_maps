package com.example.maps.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.models.Markdown
import com.example.maps.databinding.ViewholderMarkdownBinding
import com.example.maps.mappers.toArg
import com.example.maps.ui.fragments.markdowns.MarkdownsFragmentDirections

class MarkdownAdapter(private val deleteHandler: ((Markdown) -> Unit))
    : ListAdapter<Markdown, MarkdownAdapter.MarkdownViewHolder>(MarkdownDiffUtil()) {

    inner class MarkdownViewHolder(private val binding: ViewholderMarkdownBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(markdown: Markdown) {
            binding.placeName.text = markdown.name
            binding.placeAddress.text = markdown.address
            binding.deleteButton.setOnClickListener {
                deleteHandler(markdown)
                notifyItemRemoved(absoluteAdapterPosition)
                notifyItemRangeChanged(absoluteAdapterPosition, itemCount)
            }
            binding.root.setOnClickListener {
                val action = MarkdownsFragmentDirections.actionMarkdownFragmentToMainFragment(markdown.toArg())
                itemView.findNavController().navigate(action)
            }
        }
    }

    companion object {
        class MarkdownDiffUtil : DiffUtil.ItemCallback<Markdown>() {
            override fun areItemsTheSame(oldItem: Markdown, newItem: Markdown): Boolean {
                return oldItem.placeId == newItem.placeId
            }

            override fun areContentsTheSame(oldItem: Markdown, newItem: Markdown): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkdownViewHolder {
        val binding = ViewholderMarkdownBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MarkdownViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarkdownViewHolder, position: Int) {
        getItem(position).let { holder.bind(it) }
    }
}