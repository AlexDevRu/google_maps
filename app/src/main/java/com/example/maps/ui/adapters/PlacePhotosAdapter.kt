package com.example.maps.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.models.place_info.Photo
import com.example.maps.R
import com.example.maps.databinding.ViewholderPhotoBinding

class PlacePhotosAdapter: ListAdapter<Photo, PlacePhotosAdapter.PlacePhotoViewHolder>(PlacePhotosDiffUtil()) {

    inner class PlacePhotoViewHolder(private val binding: ViewholderPhotoBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: Photo) {
            val apiKey = itemView.resources.getString(R.string.google_maps_key)
            Glide.with(itemView.context)
                .load("https://maps.googleapis.com/maps/api/place/photo?maxheight=${photo.height}&maxwidth=${photo.width}&photoreference=${photo.photoReference}&sensor=false&key=" + apiKey)
                .into(binding.photo)
        }
    }

    companion object {
        class PlacePhotosDiffUtil(): DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return false
            }

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacePhotoViewHolder {
        val binding = ViewholderPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PlacePhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlacePhotoViewHolder, position: Int) {
        getItem(position).let { holder.bind(it) }
    }
}