package com.example.maps.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.models.Markdown
import com.example.maps.databinding.ViewholderMarkdownBinding

class MarkdownAdapter(private val deleteHandler: ((Markdown) -> Unit)): ListAdapter<Markdown, MarkdownAdapter.MarkdownViewHolder>(MarkdownDiffUtil()) {

    inner class MarkdownViewHolder(private val binding: ViewholderMarkdownBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(markdown: Markdown) {
            binding.placeName.text = markdown.name
            binding.placeAddress.text = markdown.address
            binding.deleteButton.setOnClickListener {
                deleteHandler(markdown)
                notifyItemRemoved(adapterPosition)
                notifyItemRangeChanged(adapterPosition, itemCount)
            }
        }
    }

    companion object {
        class MarkdownDiffUtil : DiffUtil.ItemCallback<Markdown>() {
            override fun areItemsTheSame(oldItem: Markdown, newItem: Markdown): Boolean {
                return oldItem.id == newItem.id
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