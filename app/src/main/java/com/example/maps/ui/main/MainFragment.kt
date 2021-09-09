package com.example.maps.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.common.Result
import com.example.domain.models.Location
import com.example.domain.models.place_info.PlaceInfo
import com.example.maps.R
import com.example.maps.databinding.FragmentMainBinding
import com.example.maps.ui.adapters.ReviewAdapter
import com.example.maps.ui.base.BaseFragment
import com.example.maps.utils.Constants
import com.example.maps.utils.GoogleMapUtil
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainFragment: BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate),
    OnMapReadyCallback {

    private val viewModel by viewModels<MainVM>()

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private val TAG = "MapsActivity"

    private var job: Job? = null

    private lateinit var googleMapUtil: GoogleMapUtil

    private lateinit var reviewAdapter: ReviewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        reviewAdapter = ReviewAdapter()

        checkLocationPermission()
    }

    private fun initMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun checkLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                initMap()
            } else {
                requestPermissions(permissions, Constants.LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            requestPermissions(permissions, Constants.LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(TAG, "onRequestPermissionsResult: called.")
        when (requestCode) {
            Constants.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    for(result in grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    initMap()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMapUtil = GoogleMapUtil(googleMap, requireContext())

        if (googleMapUtil.checkCoarseAndFineLocationPermissions()) {
            getDeviceLocation()

            googleMapUtil.setDefaultSettings()

            initViews()
        }
    }

    private fun initViews() {
        binding.myLocationButton.setOnClickListener {
            getDeviceLocation()
        }

        googleMapUtil.initTouchEvents()

        googleMapUtil.setOnClickMapListener() {
            viewModel.getInfoByLocation(it)
        }

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: " + place.name + ", " + place.id + ", " + place.latLng)
                if(place.latLng != null) {
                    googleMapUtil.createSingleMarker(place)
                    viewModel.getInfoByLocation(place.id!!)
                }
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })

        binding.voiceSearchButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            startActivityForResult(intent, Constants.SPEECH_REQUEST_CODE)
        }

        binding.directionsButton.setOnClickListener {
            /*binding.motionLayout.setTransition(R.id.hiddenDirections, R.id.visibleDirections)
            binding.motionLayout.transitionToEnd()
            binding.motionLayout.setTransition(R.id.directionsTransition)*/
            if(googleMapUtil.markerMode == GoogleMapUtil.MAP_MARKER_MODE.PLACE) {
                binding.motionLayout.transitionToState(R.id.visibleDirections)
                binding.motionLayout.setTransition(R.id.directionsTransition)
                googleMapUtil.markerMode = GoogleMapUtil.MAP_MARKER_MODE.DIRECTION
                binding.directionsButton.setImageResource(R.drawable.ic_baseline_close_24)
            } else {
                binding.motionLayout.transitionToState(R.id.hiddenDirections)
                binding.motionLayout.setTransition(R.id.directionsTransition)
                googleMapUtil.markerMode = GoogleMapUtil.MAP_MARKER_MODE.PLACE
                binding.directionsButton.setImageResource(R.drawable.ic_baseline_directions_car_24)
            }
        }

        binding.motionLayout.transitionToState(R.id.hiddenPlaceInfo)
        binding.placeInfo.reviewsList.adapter = reviewAdapter

        binding.directionsChoosing.originButton.setOnClickListener {
            googleMapUtil.currentDirectionMarker = GoogleMapUtil.DIRECTION_MARKER.ORIGIN
        }

        binding.directionsChoosing.destinationButton.setOnClickListener {
            googleMapUtil.currentDirectionMarker = GoogleMapUtil.DIRECTION_MARKER.DESTINATION
        }

        binding.directionsChoosing.buildDirectionButton.setOnClickListener {
            if(googleMapUtil.origin != null && googleMapUtil.destination != null) {
                val originLocation = Location(
                    googleMapUtil.origin!!.position.latitude,
                    googleMapUtil.origin!!.position.longitude,
                )
                val destinationLocation = Location(
                    googleMapUtil.destination!!.position.latitude,
                    googleMapUtil.destination!!.position.longitude,
                )
                viewModel.getDirection(originLocation, destinationLocation)
            }
        }

        observe()
    }

    private fun observe() {
        viewModel.placeData.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.placeInfo.progressBar.visibility = View.VISIBLE
                    binding.placeInfo.placeName.visibility = View.GONE
                    binding.placeInfo.placeAddress.visibility = View.GONE
                    /*binding.motionLayout.setTransition(R.id.hiddenPlaceInfo, R.id.particallyVisiblePlaceInfo)
                    binding.motionLayout.transitionToEnd()
                    binding.motionLayout.setTransition(R.id.expandPlaceInfoTransition)*/
                    binding.motionLayout.transitionToState(R.id.particallyVisiblePlaceInfo)
                    binding.motionLayout.setTransition(R.id.expandPlaceInfoTransition)
                }
                is Result.Success -> {
                    binding.placeInfo.progressBar.visibility = View.GONE
                    binding.placeInfo.placeName.visibility = View.VISIBLE
                    binding.placeInfo.placeAddress.visibility = View.VISIBLE

                    val place = it.value
                    Log.w("MapsActivity", "place found: ${place}")

                    setCurrentPlaceData(place)
                }
                is Result.Failure -> {
                    binding.placeInfo.progressBar.visibility = View.GONE
                    Log.e("MapsActivity", "place found exception: ${it.throwable.message}")
                }
            }
        }

        viewModel.direction.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {

                }
                is Result.Success -> {
                    val direction = it.value
                    Log.w("MapsActivity", "direction found: ${direction}")

                    googleMapUtil.createDirection(direction)
                }
                is Result.Failure -> {
                    Log.e("MapsActivity", "direction exception: ${it.throwable.message}")
                }
            }
        }
    }

    private fun setCurrentPlaceData(place: PlaceInfo) {
        binding.placeInfo.placeName.text = resources.getString(R.string.place_name, place.name)
        binding.placeInfo.placeAddress.text = resources.getString(R.string.place_address, place.address)

        if(place.phoneNumber != null)
            binding.placeInfo.phoneNumber.text = resources.getString(R.string.place_phone, place.phoneNumber)
        else
            binding.placeInfo.phoneNumber.visibility = View.GONE

        if(place.website != null) {
            val data = resources.getString(R.string.place_website, place.website)
            val content = SpannableString(data)
            content.setSpan(UnderlineSpan(), 10, data.length, 0)
            binding.placeInfo.placeWebsite.text = resources.getString(R.string.place_website, place.website)
        } else {
            binding.placeInfo.placeWebsite.visibility = View.GONE
        }

        binding.placeInfo.placeTypes.text = resources.getString(R.string.place_types, place.types?.joinToString(", "))

        binding.placeInfo.placeRating.rating = place.rating?.toFloat() ?: 0f
        binding.placeInfo.ratingText.text = "%.1f".format(place.rating)

        reviewAdapter.submitList(place.reviews)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            Constants.SPEECH_REQUEST_CODE -> {
                if(resultCode == 200 && resultCode == AppCompatActivity.RESULT_OK) {
                    val list = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val text = list?.firstOrNull()
                    text?.let {
                        autocompleteFragment.setText(it)
                    }
                }
            }
        }
    }

    private fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location")

        try {
            if (googleMapUtil.checkCoarseAndFineLocationPermissions()) {
                val location = googleMapUtil.fusedLocationProviderClient.lastLocation
                location.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "onComplete: found location!")
                        val currentLocation = task.result

                        job?.cancel()
                        job = lifecycleScope.launch(Dispatchers.IO) {
                            val currentAddress = googleMapUtil.getAddress(currentLocation)
                            autocompleteFragment.setCountry(currentAddress?.countryCode)
                        }

                        googleMapUtil.moveCamera(
                            LatLng(currentLocation.latitude, currentLocation.longitude)
                        )
                    } else {
                        Log.d(TAG, "onComplete: current location is null")
                        Toast.makeText(
                            requireContext(),
                            "unable to get current location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.message)
        }
    }
}