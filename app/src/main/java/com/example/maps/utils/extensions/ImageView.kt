package com.example.maps.utils.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.maps.R

fun ImageView.url(url: String?) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.no_photo)
        .into(this)
}