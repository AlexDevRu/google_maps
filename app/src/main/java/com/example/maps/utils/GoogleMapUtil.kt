
package com.example.maps.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.annotation.ColorRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.domain.models.directions.Direction
import com.example.domain.models.directions.Step
import com.example.maps.R
import com.example.maps.ui.adapters.CustomInfoWindowAdapter
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.PolyUtil
import io.reactivex.subjects.BehaviorSubject


class GoogleMapUtil(
    private val context: Context
) {
    enum class MAP_MODE {
        DIRECTION, PLACE
    }

    enum class DIRECTION_MARKER {
        ORIGIN, DESTINATION
    }

    companion object {
        private const val TAG = "GoogleMapUtil"

        private const val DEFAULT_ZOOM = 15f
        private const val DEFAULT_LOCATION_INTERVAL = 5000L
        private const val DEFAULT_FASTEST_LOCATION_INTERVAL = 3000L

        private const val DEFAULT_POLYLINE_WIDTH = 8f

        private val colors = listOf(
            R.color.black,
            R.color.green,
            R.color.red,
            R.color.teal_700,
            R.color.purple_500,
            R.color.orange
        )
    }

    private lateinit var googleMap: GoogleMap

    private val placesClient = Places.createClient(context)

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    var currentCameraPosition: LatLng? = null
        private set


    val placeMarker = BehaviorSubject.create<Marker>()

    val origin = BehaviorSubject.create<Marker>()
    val destination = BehaviorSubject.create<Marker>()

    val currentLocation = BehaviorSubject.create<LatLng>()

    var currentDirectionMarker = BehaviorSubject.createDefault(DIRECTION_MARKER.DESTINATION)


    private val _directionPolylines = mutableMapOf<Polyline, Marker>()
    val directionPolylines: Map<Polyline, Marker> = _directionPolylines

    private val _stepMap = mutableMapOf<Step, Polyline>()
    val stepMap: Map<Step, Polyline> = _stepMap

    var mapClickHandler: ((String) -> Unit)? = null



    var markerMode = MAP_MODE.PLACE
        set(value) {
            field = value
            changeMarkerMode(field)
        }


    @SuppressLint("PotentialBehaviorOverride")
    fun initMap(googleMap: GoogleMap) {
        this.googleMap = googleMap
        printInfo()
        if(markerMode == MAP_MODE.PLACE) {
            if(placeMarker.value != null)
                createPlaceMarker(placeMarker.value!!.position)
        } else {
            origin.value?.let { createOriginMarker(it.position) }
            destination.value?.let { createDestinationMarker(it.position) }
        }
        googleMap.setOnCameraMoveListener {
            currentCameraPosition = this.googleMap.cameraPosition.target
        }
        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(context))
    }

    fun printInfo() {
        Log.w(TAG, "markerMode: ${markerMode}")
        Log.w(TAG, "origin: ${origin.value?.position}")
        Log.w(TAG, "destination: ${destination.value?.position}")
        Log.w(TAG, "placeMarker: ${placeMarker.value?.position}")
    }



    private fun changeMarkerMode(mode: MAP_MODE) {
        val isDirection = mode == MAP_MODE.DIRECTION
        directionPolylines.forEach {
            it.key.isVisible = isDirection
            it.value.isVisible = isDirection
        }
        Log.e(TAG, "markerMode direction: ${isDirection}")
        if(isDirection && placeMarker.value != null) {
            createDestinationMarker(placeMarker.value!!.position)
        }
        if(isDirection && currentLocation.value != null) {
            createOriginMarker(currentLocation.value!!)
        }
        origin.value?.isVisible = isDirection
        destination.value?.isVisible = isDirection
        directionPolylines.keys.forEach {
            it.isVisible = isDirection
            it.isClickable = isDirection
        }
        printInfo()
    }



    fun moveCamera(latLng: LatLng, zoom: Float = DEFAULT_ZOOM) {
        Log.d(
            TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    fun createPlaceMarker(latLng: LatLng) {
        placeMarker.value?.remove()

        val newPlaceMarker = createMarker(latLng, null)

        newPlaceMarker?.let {
            it.isVisible = markerMode == MAP_MODE.PLACE
            placeMarker.onNext(it)
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    fun createOriginMarker(latLng: LatLng) {
        origin.value?.remove()

        val icon = Utils.getBitmapFromVector(context, R.drawable.ic_origin_marker)
        val newOrigin = createMarker(latLng, null, icon, null)
        newOrigin?.let {
            it.isVisible = markerMode == MAP_MODE.DIRECTION
            origin.onNext(it)
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    fun createDestinationMarker(latLng: LatLng) {
        destination.value?.remove()

        val icon = Utils.getBitmapFromVector(context, R.drawable.ic_destination_marker)
        val newDestination = createMarker(latLng, null, icon, null)
        newDestination?.let {
            it.isVisible = markerMode == MAP_MODE.DIRECTION
            destination.onNext(it)
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun createMarker(latLng: LatLng, title: String?, markerIcon: BitmapDescriptor? = null, snippet: String? = null): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        markerOptions.snippet(snippet)
        markerOptions.icon(markerIcon ?: BitmapDescriptorFactory.defaultMarker())
        return googleMap.addMarker(markerOptions)
    }

    @SuppressLint("MissingPermission")
    fun setDefaultSettings(): Boolean {
        if (checkCoarseAndFineLocationPermissions()) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            return true
        }
        return false
    }

    fun checkCoarseAndFineLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun initTouchEvents() {
        googleMap.setOnPolylineClickListener {
            val marker = directionPolylines[it]
            marker?.showInfoWindow()
            if(marker != null) moveCamera(marker.position, googleMap.cameraPosition.zoom)
        }

        googleMap.setOnPoiClickListener {

            if(markerMode == MAP_MODE.PLACE) {
                createPlaceMarker(it.latLng)
                mapClickHandler?.invoke(it.placeId)
            } else {
                if(currentDirectionMarker.value == DIRECTION_MARKER.ORIGIN)
                    createOriginMarker(it.latLng)
                else
                    createDestinationMarker(it.latLng)
            }
        }
    }

    fun getAddress(latLng: LatLng): Address? {
        return try {
            val list =  Geocoder(context)
                .getFromLocation(latLng.latitude, latLng.longitude, 1)
            val currentAddress = list.firstOrNull()
            Log.w(TAG, "currentAddress: ${currentAddress?.locale}")
            currentAddress
        } catch (e: Exception) {
            Log.w(TAG, "getAddress exception: ${e}")
            null
        }
    }

    private fun addPolylineToMap(polylineList: List<LatLng>, @ColorRes color: Int = R.color.black): Polyline? {
        val polylineOptions = PolylineOptions()

        polylineOptions.color(ContextCompat.getColor(context, color))
        polylineOptions.width(DEFAULT_POLYLINE_WIDTH)
        polylineOptions.startCap(ButtCap())
        polylineOptions.jointType(JointType.ROUND)
        polylineOptions.clickable(true)
        polylineOptions.addAll(polylineList)

        return googleMap.addPolyline(polylineOptions)
    }

    fun createDirection(direction: Direction): Map<Step, Polyline>? {
        if(direction.routes.isNullOrEmpty())
            return null

        _stepMap.clear()

        _directionPolylines.forEach {
            it.key.remove()
            it.value.remove()
        }
        _directionPolylines.clear()

        for (route in direction.routes!!) {
            for(leg in route.legs) {
                for((currentColorIndex, step) in leg.steps.orEmpty().withIndex()) {
                    val polylineList = mutableListOf<LatLng>()
                    polylineList.addAll(PolyUtil.decode(step.polyline.points))

                    val polyline = addPolylineToMap(polylineList, colors[currentColorIndex % colors.size])

                    if(polyline != null) {
                        val midPoint = polyline.points[polyline.points.size / 2]

                        val invisibleMarker =
                            BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

                        val distance = context.resources.getString(R.string.distance, step.distance.text)
                        val duration = context.resources.getString(R.string.duration, step.duration.text)

                        val marker = createMarker(midPoint, distance, invisibleMarker,
                            "${duration}\n\n${step.html_instructions}")

                        marker?.let {
                            it.position = midPoint
                            _directionPolylines[polyline] = it
                        }

                        _stepMap[step] = polyline
                    }
                }
            }
        }

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

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
        }

        directionPolylines.keys.forEach {
            it.isVisible = markerMode == MAP_MODE.DIRECTION
            it.isClickable = markerMode == MAP_MODE.DIRECTION
        }

        return _stepMap
    }

    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        val locationRequest = LocationRequest()
            .setInterval(DEFAULT_LOCATION_INTERVAL)
            .setFastestInterval(DEFAULT_FASTEST_LOCATION_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true)

        val locationSettingsResponseTask = LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnCompleteListener {
            try {
                if (checkCoarseAndFineLocationPermissions()) {

                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            super.onLocationResult(locationResult)
                            if(locationResult != null) {
                                val lat = locationResult.lastLocation.latitude
                                val lng = locationResult.lastLocation.longitude

                                val newLocation = LatLng(lat, lng)
                                if(currentLocation.value == null) {
                                    moveCamera(newLocation)
                                }

                                currentLocation.onNext(newLocation)
                            }
                        }
                    }, Looper.getMainLooper())

                }

            } catch (e: ApiException) {
                Log.e(TAG, "getDeviceLocation: SecurityException: " + e.message)
            }
        }
    }

    fun getAutocompletePredictions(query: String, successHandler: (List<AutocompletePrediction>) -> Unit, failureHandler: (ApiException) -> Unit) {
        val token = AutocompleteSessionToken.newInstance()
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener {
            successHandler(it.autocompletePredictions)
        }.addOnFailureListener {
            if (it is ApiException) {
                Log.e(TAG, "Place not found: " + it.statusCode)
                failureHandler(it)
            }
        }
    }

    fun moveCameraToCurrentLocation() {
        if(currentLocation.value != null) moveCamera(currentLocation.value!!)
    }
}