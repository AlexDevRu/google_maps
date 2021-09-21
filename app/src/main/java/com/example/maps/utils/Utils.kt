package com.example.maps.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.example.maps.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object Utils {
    private const val TAG = "Utils"

    fun getBitmapFromVector(
        context: Context,
        @DrawableRes vectorResourceId: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ResourcesCompat.getDrawable(
            context.resources, vectorResourceId, null
        )
        if (vectorDrawable == null) {
            Log.e(TAG, "Requested vector resource was not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun getWeekDayByNumber(n: Int): Int {
        return when(n) {
            0 -> R.string.monday
            1 -> R.string.tuesday
            2 -> R.string.wednesday
            3 -> R.string.thursday
            4 -> R.string.friday
            5 -> R.string.saturday
            else -> R.string.sunday
        }
    }
}