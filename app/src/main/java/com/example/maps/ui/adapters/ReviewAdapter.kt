package com.example.maps.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.models.place_info.Review
import com.example.maps.databinding.ViewHolderReviewBinding

class ReviewAdapter: ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(ReviewDiffUtil()) {

    inner class ReviewViewHolder(private val binding: ViewHolderReviewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            binding.username.text = review.authorName
            binding.rating.rating = review.rating.toFloat()
            binding.ratingText.text = review.rating.toString()
            binding.reviewText.text = review.text
            binding.relativeTime.text = review.relativeTimeDescription
            Glide.with(itemView.context)
                .load(review.profilePhotoUrl)
                .into(binding.avatar)
        }
    }

    companion object {
        class ReviewDiffUtil(): DiffUtil.ItemCallback<Review>() {
            override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ViewHolderReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        getItem(position).let { holder.bind(it) }
    }

}