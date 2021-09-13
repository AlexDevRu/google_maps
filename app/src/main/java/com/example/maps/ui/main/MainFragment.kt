package com.example.maps.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import com.example.domain.models.directions.Direction
import com.example.domain.models.place_info.PlaceInfo
import com.example.maps.R
import com.example.maps.databinding.FragmentMainBinding
import com.example.maps.mappers.toModel
import com.example.maps.ui.adapters.ReviewAdapter
import com.example.maps.ui.base.BaseFragment
import com.example.maps.utils.Constants
import com.example.maps.utils.GoogleMapUtil
import com.example.maps.utils.InternetUtil
import com.example.maps.utils.extensions.setHtml
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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

    private lateinit var reviewAdapter: ReviewAdapter

    private var firstInit = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setHint(getString(R.string.search))

        reviewAdapter = ReviewAdapter()

        firstInit = savedInstanceState == null

        internetObserver = InternetUtil(requireContext())

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
        viewModel.googleMapUtil.initMap(googleMap)

        if (viewModel.requireGoogleMapUtil().checkCoarseAndFineLocationPermissions()) {

            if(viewModel.googleMapUtil.currentCameraPosition == null)
                getDeviceLocation()
            else
                viewModel.requireGoogleMapUtil().moveCamera(
                    viewModel.googleMapUtil.currentCameraPosition!!
                )

            viewModel.requireGoogleMapUtil().setDefaultSettings()

            initViews()
        }
    }

    private fun initViews() {
        if(viewModel.googleMapUtil.placeMarker == null && viewModel.googleMapUtil.markerMode != GoogleMapUtil.MAP_MODE.DIRECTION)
            binding.motionLayout.transitionToState(R.id.hiddenPlaceInfo)

        binding.myLocationButton.setOnClickListener {
            getDeviceLocation()
        }

        viewModel.requireGoogleMapUtil().initTouchEvents()

        viewModel.requireGoogleMapUtil().mapClickHandler = {
            viewModel.getInfoByLocation(it)
        }

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: " + place.name + ", " + place.id + ", " + place.latLng)
                if(place.latLng != null) {
                    viewModel.setPlace(place)
                }
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })

        binding.searchPlaceWrapper.voiceSearchButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            startActivityForResult(intent, Constants.SPEECH_REQUEST_CODE)
        }



        if(viewModel.googleMapUtil.markerMode == GoogleMapUtil.MAP_MODE.PLACE) {
            binding.directionsButton.setImageResource(R.drawable.ic_baseline_directions_car_24)
        } else {
            binding.directionsButton.setImageResource(R.drawable.ic_baseline_close_24)
        }

        binding.directionsButton.setOnClickListener {
            if(viewModel.requireGoogleMapUtil().markerMode == GoogleMapUtil.MAP_MODE.PLACE) {
                binding.motionLayout.transitionToState(R.id.visibleDirections)
                binding.motionLayout.setTransition(R.id.directionsTransition)
                viewModel.requireGoogleMapUtil().markerMode = GoogleMapUtil.MAP_MODE.DIRECTION
                binding.directionsButton.setImageResource(R.drawable.ic_baseline_close_24)
            } else {
                binding.motionLayout.transitionToState(R.id.hiddenDirections)
                binding.motionLayout.setTransition(R.id.directionsTransition)
                viewModel.requireGoogleMapUtil().markerMode = GoogleMapUtil.MAP_MODE.PLACE
                binding.directionsButton.setImageResource(R.drawable.ic_baseline_directions_car_24)
            }
        }



        binding.placeInfo.reviewsList.adapter = reviewAdapter

        binding.directionsChoosing.originButton.setOnClickListener {
            viewModel.requireGoogleMapUtil().currentDirectionMarker = GoogleMapUtil.DIRECTION_MARKER.ORIGIN
            autocompleteFragment.startActivity(
                Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                .build(requireContext()))
        }

        binding.directionsChoosing.destinationButton.setOnClickListener {
            viewModel.requireGoogleMapUtil().currentDirectionMarker = GoogleMapUtil.DIRECTION_MARKER.DESTINATION
        }

        binding.directionsChoosing.buildDirectionButton.setOnClickListener {
            if(viewModel.requireGoogleMapUtil().origin != null && viewModel.requireGoogleMapUtil().destination != null) {
                val originLocation = viewModel.requireGoogleMapUtil().origin!!.position.toModel()
                val destinationLocation = viewModel.requireGoogleMapUtil().destination!!.position.toModel()
                viewModel.getDirection(originLocation, destinationLocation)
            }
        }

        viewModel.requireGoogleMapUtil().originChangeListener = {
            if(viewModel.requireGoogleMapUtil().origin != null) {
                val address = viewModel.requireGoogleMapUtil().getAddress(viewModel.requireGoogleMapUtil().origin!!.position)
                binding.directionsChoosing.originButton.text = address?.getAddressLine(0)
            } else {
                binding.directionsChoosing.originButton.text = getString(R.string.choose_origin)
            }
        }

        viewModel.requireGoogleMapUtil().destinationChangeListener = {
            if(viewModel.requireGoogleMapUtil().destination != null) {
                val address = viewModel.requireGoogleMapUtil().getAddress(viewModel.requireGoogleMapUtil().destination!!.position)
                binding.directionsChoosing.destinationButton.text = address?.getAddressLine(0)
            } else {
                binding.directionsChoosing.originButton.text = getString(R.string.choose_destination)
            }
        }

        observe()
    }

    private fun observe() {
        viewModel.placeData.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.placeInfo.progressBar.visibility = View.VISIBLE
                    binding.placeInfo.placeInfoContainer.visibility = View.GONE

                    binding.motionLayout.transitionToState(R.id.particallyVisiblePlaceInfo)
                    binding.motionLayout.setTransition(R.id.expandPlaceInfoTransition)
                }
                is Result.Success -> {
                    binding.placeInfo.progressBar.visibility = View.GONE
                    binding.placeInfo.placeInfoContainer.visibility = View.VISIBLE

                    val place = it.value
                    Log.w("MapsActivity", "place found: ${place}")

                    setCurrentPlaceData(place)
                }
                is Result.Failure -> {
                    binding.placeInfo.progressBar.visibility = View.GONE
                    Log.e("MapsActivity", "place found exception: ${it.throwable.message}")
                    showSnackBar(it.throwable.message.orEmpty())
                }
            }
        }

        viewModel.direction.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.directionsChoosing.duration.visibility = View.GONE
                    binding.directionsChoosing.distance.visibility = View.GONE
                    binding.directionsChoosing.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.directionsChoosing.duration.visibility = View.VISIBLE
                    binding.directionsChoosing.distance.visibility = View.VISIBLE
                    binding.directionsChoosing.progressBar.visibility = View.GONE

                    val direction = it.value
                    Log.w("MapsActivity", "direction found: ${direction}")

                    viewModel.requireGoogleMapUtil().createDirection(direction)
                    updateDirectionInfo(direction)
                }
                is Result.Failure -> {
                    Log.e("MapsActivity", "direction exception: ${it.throwable.message}")
                    showSnackBar(it.throwable.message.orEmpty())
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
            binding.placeInfo.placeWebsite.text = content
        } else {
            binding.placeInfo.placeWebsite.visibility = View.GONE
        }

        binding.placeInfo.placeTypes.text = resources.getString(R.string.place_types, place.types?.joinToString(", "))

        binding.placeInfo.placeRating.rating = place.rating?.toFloat() ?: 0f
        binding.placeInfo.ratingText.text = "%.1f".format(place.rating)

        reviewAdapter.submitList(place.reviews)
    }

    private fun updateDirectionInfo(direction: Direction) {
        binding.directionsChoosing.distance.setHtml(resources.getString(R.string.total_distance, "%.2f".format(direction.distance)))
        binding.directionsChoosing.duration.setHtml(resources.getString(R.string.total_duration, "%.1f".format(direction.distance)))
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
            if (viewModel.requireGoogleMapUtil().checkCoarseAndFineLocationPermissions()) {
                val location = viewModel.requireGoogleMapUtil().fusedLocationProviderClient.lastLocation
                location.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val currentLocation = task.result
                        Log.d(TAG, "onComplete: found location! ${currentLocation}")

                        if(currentLocation != null) {
                            if(viewModel.requireGoogleMapUtil().origin == null) {
                                viewModel.requireGoogleMapUtil().createOriginMarker(LatLng(currentLocation.latitude, currentLocation.longitude))
                            }

                            if(internetObserver.isInternetOn()) {
                                getDeviceAddress(currentLocation)
                            } else {
                                showSnackBar("device location not found. Turn on the internet")
                            }

                            viewModel.requireGoogleMapUtil().moveCamera(
                                LatLng(currentLocation.latitude, currentLocation.longitude)
                            )

                        } else {
                            viewModel.requireGoogleMapUtil().getDeviceLocation()
                        }

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

    private fun getDeviceAddress(currentLocation: Location) {
        job?.cancel()
        job = lifecycleScope.launch(Dispatchers.IO) {
            val currentAddress = viewModel.googleMapUtil.getAddress(currentLocation)
            autocompleteFragment.setCountry(currentAddress?.countryCode)
        }
    }
}