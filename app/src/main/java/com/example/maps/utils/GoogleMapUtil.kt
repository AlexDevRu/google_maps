
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
import com.example.maps.R
import com.example.maps.ui.adapters.CustomInfoWindowAdapter
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil


class GoogleMapUtil(
    private val context: Context
) {

    companion object {
        private const val TAG = "GoogleMapUtil"
        private const val DEFAULT_ZOOM = 15f
    }

    private var googleMap: GoogleMap? = null

    val colors = listOf(R.color.black, R.color.green, R.color.red, R.color.teal_700, R.color.purple_500, R.color.orange)

    @SuppressLint("PotentialBehaviorOverride")
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
        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(context))
    }

    fun printInfo() {
        Log.w(TAG, "markerMode: ${markerMode}")
        Log.w(TAG, "origin: ${origin?.position}")
        Log.w(TAG, "destination: ${destination?.position}")
        Log.w(TAG, "placeMarker: ${placeMarker?.position}")
    }

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
        private set

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


    var currentLocation: LatLng? = null
        private set


    private val directionPolylines = hashMapOf<Polyline, Marker>()

    var markerMode = MAP_MODE.PLACE
        set(value) {
            field = value
            changeMarkerMode(field)
        }

    private fun changeMarkerMode(mode: MAP_MODE) {
        val isDirection = mode == MAP_MODE.DIRECTION
        directionPolylines.forEach {
            it.key.isVisible = isDirection
            it.value.isVisible = isDirection
        }
        Log.e(TAG, "markerMode direction: ${isDirection}")
        if(isDirection && placeMarker != null) {
            createDestinationMarker(placeMarker!!.position)
        }
        if(isDirection && currentLocation != null) {
            createOriginMarker(currentLocation!!)
        }
        origin?.isVisible = isDirection
        destination?.isVisible = isDirection
        directionPolylines.keys.forEach {
            Log.d(TAG, "hfdgkjhdfkgjhdkjghdfkjgh")
            it.isVisible = isDirection
            it.isClickable = isDirection
        }
        printInfo()
    }

    var currentDirectionMarker = DIRECTION_MARKER.DESTINATION

    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    var mapClickHandler: ((String) -> Unit)? = null

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

        val icon = Utils.getBitmapFromVector(context, R.drawable.ic_origin_marker)
        origin = createMarker(latLng, null, icon, null)
        origin?.isVisible = markerMode == MAP_MODE.DIRECTION
    }

    @SuppressLint("PotentialBehaviorOverride")
    fun createDestinationMarker(latLng: LatLng) {
        destination?.remove()

        val icon = Utils.getBitmapFromVector(context, R.drawable.ic_destination_marker)
        destination = createMarker(latLng, null, icon, null)
        destination?.isVisible = markerMode == MAP_MODE.DIRECTION
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun createMarker(latLng: LatLng, title: String?, markerIcon: BitmapDescriptor? = null, snippet: String? = null): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(title)
        markerOptions.snippet(snippet)
        markerOptions.icon(markerIcon ?: BitmapDescriptorFactory.defaultMarker())
        return googleMap?.addMarker(markerOptions)
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
        googleMap?.setOnPolylineClickListener {
            val marker = directionPolylines[it]
            marker?.showInfoWindow()
            if(marker != null) moveCamera(marker.position, googleMap!!.cameraPosition.zoom)
        }

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
        }
    }

    fun getAddress(latLng: LatLng): Address? {
        return try {
            val list =  Geocoder(context)
                .getFromLocation(latLng.latitude, latLng.longitude, 1)
            val currentAddress = list.firstOrNull()
            Log.w(TAG, "currentAddress: ${currentAddress}")
            currentAddress
        } catch (e: Exception) {
            Log.w(TAG, "getAddress exception: ${e}")
            null
        }
    }

    private fun addPolylineToMap(polylineList: List<LatLng>, @ColorRes color: Int = R.color.black): Polyline? {
        val polylineOptions = PolylineOptions()
        polylineOptions.color(ContextCompat.getColor(context, color))
        polylineOptions.width(8f)
        polylineOptions.startCap(ButtCap())
        polylineOptions.jointType(JointType.ROUND)
        polylineOptions.clickable(true)
        polylineOptions.addAll(polylineList)

        return googleMap?.addPolyline(polylineOptions)
    }

    fun createDirection(direction: Direction) {
        if(direction.routes.isNullOrEmpty())
            return

        directionPolylines.forEach {
            it.key.remove()
            it.value.remove()
        }
        directionPolylines.clear()

        for (route in direction.routes!!) {
            //polylineList.addAll(PolyUtil.decode(route.overview_polyline.points))
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

                        marker?.position = midPoint

                        if(marker != null) directionPolylines[polyline] = marker
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

            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
        }

        directionPolylines.keys.forEach {
            it.isVisible = markerMode == MAP_MODE.DIRECTION
            it.isClickable = markerMode == MAP_MODE.DIRECTION
        }
    }

    @SuppressLint("MissingPermission")
    fun getDeviceLocation(deviceLocationChangedHandler: (LatLng) -> Unit) {
        /*val placeFields = listOf(Place.Field.LAT_LNG)
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
        }*/

        val locationRequest = LocationRequest().setInterval(5000).setFastestInterval(3000)
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
                                if(currentLocation == null) {
                                    moveCamera(newLocation)
                                }
                                currentLocation = newLocation
                                deviceLocationChangedHandler(currentLocation!!)
                            }
                        }
                    }, Looper.getMainLooper())

                }

            } catch (e: ApiException) {
                Log.e(TAG, "getDeviceLocation: SecurityException: " + e.message)
            }
        }
    }

    fun moveCameraToCurrentLocation() {
        if(currentLocation != null) moveCamera(currentLocation!!)
    }
}