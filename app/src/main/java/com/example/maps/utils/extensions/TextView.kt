package com.example.maps.utils.extensions

import android.widget.TextView
import androidx.core.text.HtmlCompat

fun TextView.setHtml(html: String) {
    text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
}