package com.example.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import java.io.IOException

class GoogleMapUtil(
    private val googleMap: GoogleMap,
    private val context: Context
) {

    private val TAG = "MapsActivity"
    private val DEFAULT_ZOOM = 15f

    val fusedLocationProviderClient: FusedLocationProviderClient

    init {
        Places.initialize(context, context.getString(R.string.google_maps_key))
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun moveCamera(latLng: LatLng, zoom: Float = DEFAULT_ZOOM) {
        Log.d(
            TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    fun createSingleMarker(latLng: LatLng, title: String? = null) {
        moveCamera(latLng)

        googleMap.clear()

        createMarker(latLng, title ?: "${latLng.latitude} : ${latLng.longitude}")
    }

    fun createSingleMarker(place: Place) {
        moveCamera(place.latLng!!)

        googleMap.clear()

        createMarker(place.latLng!!, place.name)
    }

    private fun createMarker(latLng: LatLng, title: String?) {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        googleMap.addMarker(markerOptions)
    }

    private fun getLocationByKey(query: String) {
        val geocoder = Geocoder(context)
        try {
            val list =  geocoder.getFromLocationName(query, 1)
            if(list.isNotEmpty()) {
                val address = list.first()
                val marker = LatLng(address.latitude, address.longitude)
                googleMap.addMarker(MarkerOptions().position(marker).title(address.getAddressLine(0)))
                moveCamera(marker)
            }
        } catch (e: IOException) {
            Log.e(TAG, "getLocationByKey IOException: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun setDefaultSettings(): Boolean {
        if (checkCoarseAndFineLocationPermissions()) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            return true
        }
        return false
    }

    fun checkCoarseAndFineLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun initTouchEvents() {
        googleMap.setOnPoiClickListener {
            createSingleMarker(it.latLng, it.name)
            val address = getAddress(it.latLng)
            address?.let {

            }
        }
    }

    fun getAddress(location: Location): Address? {
        return getAddress(
            LatLng(location.latitude, location.longitude)
        )
    }

    fun getAddress(latLng: LatLng): Address? {
        val list =  Geocoder(context)
            .getFromLocation(latLng.latitude, latLng.longitude, 1)
        val currentAddress = list.firstOrNull()
        Log.w(TAG, "currentAddress: ${currentAddress}")
        return currentAddress
    }
}