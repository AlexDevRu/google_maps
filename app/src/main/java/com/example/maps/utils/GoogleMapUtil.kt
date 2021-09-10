
package com.example.maps.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.domain.models.directions.Direction
import com.example.maps.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.PolyUtil
import java.io.IOException


class GoogleMapUtil(
    private val context: Context
) {

    private var googleMap: GoogleMap? = null

    fun initMap(googleMap: GoogleMap) {
        this.googleMap = googleMap
        printInfo()
        if(markerMode == MAP_MODE.PLACE) {
            if(placeMarker != null)
                createPlaceMarker(placeMarker!!.position)
        } else {
            origin?.let { createOriginMarker(it.position) }
            destination?.let { createDestinationMarker(it.position) }
        }
        googleMap.setOnCameraMoveListener {
            currentCameraPosition = this.googleMap?.cameraPosition?.target
        }
    }

    fun printInfo() {
        Log.w("MapsActivity", "markerMode: ${markerMode}")
        Log.w("MapsActivity", "origin: ${origin?.position}")
        Log.w("MapsActivity", "destination: ${destination?.position}")
        Log.w("MapsActivity", "placeMarker: ${placeMarker?.position}")
    }

    private val TAG = "MapsActivity"
    private val DEFAULT_ZOOM = 15f

    enum class MAP_MODE {
        DIRECTION, PLACE
    }

    enum class DIRECTION_MARKER {
        ORIGIN, DESTINATION
    }

    var currentCameraPosition: LatLng? = null
        private set

    var originChangeListener: (() -> Unit)? = null
        set(value) {
            field = value
            if(origin != null) field?.invoke()
        }

    var destinationChangeListener: (() -> Unit)? = null
        set(value) {
            field = value
            if(destination != null) field?.invoke()
        }

    var placeMarker: Marker? = null
        private set(value) {
            field = value
        }

    var origin: Marker? = null
        private set(value) {
            field = value
            originChangeListener?.invoke()
        }

    var destination: Marker? = null
        private set(value) {
            field = value
            destinationChangeListener?.invoke()
        }

    private var currentDirection: Polyline? = null

    var markerMode = MAP_MODE.PLACE
        set(value) {
            field = value
            val isDirection = field == MAP_MODE.DIRECTION
            origin?.isVisible = isDirection
            destination?.isVisible = isDirection
            currentDirection?.isVisible = isDirection
            Log.e(TAG, "markerMode dir: ${field == MAP_MODE.DIRECTION}")
            printInfo()
        }

    var currentDirectionMarker = DIRECTION_MARKER.DESTINATION


    val fusedLocationProviderClient: FusedLocationProviderClient
    private val placesClient: PlacesClient

    var mapClickHandler: ((String) -> Unit)? = null

    var hasMarkers: Boolean = false
        private set

    init {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        placesClient = Places.createClient(context)
    }

    fun moveCamera(latLng: LatLng, zoom: Float = DEFAULT_ZOOM) {
        Log.d(
            TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    fun createPlaceMarker(latLng: LatLng) {
        placeMarker?.remove()
        placeMarker = createMarker(latLng, null)
        placeMarker?.isVisible = markerMode == MAP_MODE.PLACE
    }

    @SuppressLint("PotentialBehaviorOverride")
    fun createOriginMarker(latLng: LatLng) {
        origin?.remove()
        origin = createMarker(
            latLng,
            null,
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
            true
        )
        origin?.isVisible = markerMode == MAP_MODE.DIRECTION
    }

    @SuppressLint("PotentialBehaviorOverride")
    fun createDestinationMarker(latLng: LatLng) {
        destination?.remove()
        destination = createMarker(
            latLng,
            null,
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
            true
        )
        destination?.isVisible = markerMode == MAP_MODE.DIRECTION
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun createMarker(latLng: LatLng, title: String?, markerIcon: BitmapDescriptor? = null, draggable: Boolean = false): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        markerOptions.draggable(draggable)
        markerOptions.icon(markerIcon ?: BitmapDescriptorFactory.defaultMarker())
        hasMarkers = true
        return googleMap?.addMarker(markerOptions)
    }

    private fun getLocationByKey(query: String) {
        val geocoder = Geocoder(context)
        try {
            val list =  geocoder.getFromLocationName(query, 1)
            if(list.isNotEmpty()) {
                val address = list.first()
                val marker = LatLng(address.latitude, address.longitude)
                googleMap?.addMarker(MarkerOptions().position(marker).title(address.getAddressLine(0)))
                moveCamera(marker)
            }
        } catch (e: IOException) {
            Log.e(TAG, "getLocationByKey IOException: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun setDefaultSettings(): Boolean {
        if (checkCoarseAndFineLocationPermissions()) {
            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled = false
            return true
        }
        return false
    }

    fun checkCoarseAndFineLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun initTouchEvents() {
        googleMap?.setOnPoiClickListener {

            if(markerMode == MAP_MODE.PLACE) {
                placeMarker?.remove()
                placeMarker = createMarker(it.latLng, it.name)
                mapClickHandler?.invoke(it.placeId)
            } else {
                if(currentDirectionMarker == DIRECTION_MARKER.ORIGIN)
                    createOriginMarker(it.latLng)
                else
                    createDestinationMarker(it.latLng)
            }
            /*val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.RATING,
                Place.Field.OPENING_HOURS,
                Place.Field.PHONE_NUMBER,
                Place.Field.PHOTO_METADATAS,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.WEBSITE_URI,
            )
            Log.w(TAG, "place id selected: ${it.placeId}")

            val request = FetchPlaceRequest.newInstance(it.placeId, placeFields)
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place
                Log.i(TAG, "Place found: " + place)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.message)
                    val statusCode = exception.statusCode
                    // TODO: Handle error with given status code.
                }
            }*/
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

    fun createDirection(direction: Direction) {
        if(direction.routes.isNullOrEmpty())
            return

        val polylineList = mutableListOf<LatLng>()
        for (route in direction.routes!!) {
            polylineList.addAll(PolyUtil.decode(route.overview_polyline.points))
        }
        val polylineOptions = PolylineOptions()
        polylineOptions.color(ContextCompat.getColor(context, R.color.black))
        polylineOptions.width(8f)
        polylineOptions.startCap(ButtCap())
        polylineOptions.jointType(JointType.ROUND)
        polylineOptions.addAll(polylineList)

        currentDirection?.remove()
        currentDirection = googleMap?.addPolyline(polylineOptions)

        if(direction.bounds != null) {
            val builder = LatLngBounds.builder()
            builder.include(
                LatLng(
                    direction.bounds!!.northeast.lat,
                    direction.bounds!!.northeast.lng
                ))
            builder.include(
                LatLng(
                    direction.bounds!!.southwest.lat,
                    direction.bounds!!.southwest.lng
                )
            )

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
        }
    }

    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FindCurrentPlaceRequest.builder(placeFields).build()
        if(checkCoarseAndFineLocationPermissions()) {
            placesClient.findCurrentPlace(request)
                .addOnSuccessListener { response: FindCurrentPlaceResponse ->
                    for (placeLikelihood in response.placeLikelihoods) {
                        Log.i(
                            TAG, String.format(
                                "current latlng: '%s'",
                                placeLikelihood.place.latLng
                            )
                        )
                        moveCamera(placeLikelihood.place.latLng!!)
                    }
                }.addOnFailureListener { exception: Exception? ->
                    if (exception is ApiException) {
                        Log.e(TAG, "Place not found: " + exception.statusCode)
                    }
                }
        }
    }
}