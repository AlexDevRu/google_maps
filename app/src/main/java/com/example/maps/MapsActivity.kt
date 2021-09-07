package com.example.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.maps.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private val TAG = "MapsActivity"

    private var job: Job? = null

    private lateinit var googleMapUtil: GoogleMapUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        checkLocationPermission()
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
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
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                initMap()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    Constants.LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                Constants.LOCATION_PERMISSION_REQUEST_CODE
            )
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
        googleMapUtil = GoogleMapUtil(googleMap, applicationContext)

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

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: " + place.name + ", " + place.id + ", " + place.latLng)
                if(place.latLng != null) {
                    googleMapUtil.createSingleMarker(place)
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            Constants.SPEECH_REQUEST_CODE -> {
                if(resultCode == 200 && resultCode == RESULT_OK) {
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
                location.addOnCompleteListener(this) { task ->
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
                            applicationContext,
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