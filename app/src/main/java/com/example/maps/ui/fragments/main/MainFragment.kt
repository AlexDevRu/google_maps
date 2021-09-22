package com.example.maps.ui.fragments.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.domain.common.DIRECTION_TYPE
import com.example.domain.common.Result
import com.example.domain.models.directions.Direction
import com.example.maps.R
import com.example.maps.databinding.FragmentMainBinding
import com.example.maps.mappers.toModel
import com.example.maps.ui.adapters.PlaceTabsAdapter
import com.example.maps.ui.adapters.StepAdapter
import com.example.maps.ui.fragments.base.BaseFragment
import com.example.maps.utils.Constants
import com.example.maps.utils.DataTableUtil
import com.example.maps.utils.GoogleMapUtil
import com.example.maps.utils.extensions.hide
import com.example.maps.utils.extensions.show
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MainFragment: BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate),
    OnMapReadyCallback {

    companion object {
        private const val TAG = "MapsActivity"
    }

    private val viewModel by sharedViewModel<MainVM>()

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private val args: MainFragmentArgs by navArgs()

    private val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

    private var firstInit = false

    private lateinit var stepAdapter: StepAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(placeFields)
        updateMapMode()

        firstInit = savedInstanceState == null

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

        if (viewModel.googleMapUtil.checkCoarseAndFineLocationPermissions()) {

            if(firstInit && args.markdown != null) {
                viewModel.setPlace(args.markdown!!.toModel())
            }

            if(viewModel.googleMapUtil.currentCameraPosition == null) {
                viewModel.googleMapUtil.getDeviceLocation()

                compositeDisposable.add(
                    viewModel.currentAddress.subscribe {
                        autocompleteFragment.setCountry(it.countryCode)
                    }
                )
            }
            else if(!firstInit && args.markdown == null)
                viewModel.googleMapUtil.moveCameraToCurrentLocation()

            viewModel.googleMapUtil.setDefaultSettings()

            initViews()
        }
    }

    private fun initViews() {
        viewModel.googleMapUtil.initTouchEvents()
        initViewPager()
        initDirectionTypes()

        setDirectionButtonEnable()

        binding.myLocationButton.setOnClickListener {
            viewModel.googleMapUtil.moveCameraToCurrentLocation()
        }

        viewModel.googleMapUtil.mapClickHandler = {
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
            startActivityForResult(intent, Constants.SPEECH_RESULT_CODE)
        }


        binding.directionsButton.setOnClickListener {
            viewModel.toggleMapMode()
        }



        binding.directionsChoosing.originButton.setOnClickListener {
            viewModel.googleMapUtil.currentDirectionMarker.onNext(GoogleMapUtil.DIRECTION_MARKER.ORIGIN)
        }

        binding.directionsChoosing.destinationButton.setOnClickListener {
            viewModel.googleMapUtil.currentDirectionMarker.onNext(GoogleMapUtil.DIRECTION_MARKER.DESTINATION)
        }

        compositeDisposable.add(
            viewModel.googleMapUtil.currentDirectionMarker.subscribe {
                updateMapMode()
            }
        )

        binding.directionsChoosing.buildDirectionButton.setOnClickListener {
            viewModel.getDirection()
        }

        compositeDisposable.add(
            viewModel.googleMapUtil.origin.subscribe {
                if(it != null) {
                    val address = viewModel.googleMapUtil.getAddress(it.position)
                    binding.directionsChoosing.originButton.text = address?.getAddressLine(0)
                } else {
                    binding.directionsChoosing.originButton.text = getString(R.string.choose_origin)
                }
                setDirectionButtonEnable()
            }
        )

        compositeDisposable.add(
            viewModel.googleMapUtil.destination.subscribe {
                if(it != null) {
                    val address = viewModel.googleMapUtil.getAddress(it.position)
                    binding.directionsChoosing.destinationButton.text = address?.getAddressLine(0)
                } else {
                    binding.directionsChoosing.originButton.text = getString(R.string.choose_destination)
                }
                setDirectionButtonEnable()
            }
        )

        stepAdapter = StepAdapter {
            val polyline = viewModel.googleMapUtil.stepMap[it]
            val marker = viewModel.googleMapUtil.directionPolylines[polyline]
            if(marker != null) {
                marker.showInfoWindow()
                viewModel.googleMapUtil.moveCamera(marker.position)
                binding.motionLayout.transitionToStart()
            }
        }
        binding.directionsChoosing.stepList.adapter = stepAdapter

        observe()
    }

    private fun setDirectionButtonEnable() {
        binding.directionsChoosing.buildDirectionButton.isEnabled =
            viewModel.googleMapUtil.origin.value != null && viewModel.googleMapUtil.destination.value != null
    }

    private fun updateMapMode() {
        if(viewModel.currentMapMode.value == GoogleMapUtil.MAP_MODE.DIRECTION
            && viewModel.googleMapUtil.currentDirectionMarker.value == GoogleMapUtil.DIRECTION_MARKER.ORIGIN) {

                autocompleteFragment.setHint(resources.getString(R.string.choose_origin))
                highlightDirectionButton(binding.directionsChoosing.originButton as MaterialButton, true)
                highlightDirectionButton(binding.directionsChoosing.destinationButton as MaterialButton, false)
        }
        else if(viewModel.currentMapMode.value == GoogleMapUtil.MAP_MODE.DIRECTION
            && viewModel.googleMapUtil.currentDirectionMarker.value == GoogleMapUtil.DIRECTION_MARKER.DESTINATION) {

                autocompleteFragment.setHint(resources.getString(R.string.choose_destination))
                highlightDirectionButton(binding.directionsChoosing.originButton as MaterialButton, false)
                highlightDirectionButton(binding.directionsChoosing.destinationButton as MaterialButton, true)
        }
        else {
            autocompleteFragment.setHint(resources.getString(R.string.search))
        }
    }

    private fun highlightDirectionButton(button: MaterialButton, highlight: Boolean) {
        val highlightColor = if(highlight) R.color.rose else R.color.blue

        button.setStrokeColorResource(highlightColor)
        button.setTextColor(ContextCompat.getColor(requireContext(), highlightColor))
    }

    private fun initDirectionTypes() {
        // Настраиваем адаптер
        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.direction_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Вызываем адаптер
        binding.directionsChoosing.directionTypeSpinner.adapter = adapter

        binding.directionsChoosing.directionTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.directionType = when(p2) {
                    1 -> DIRECTION_TYPE.WALKING
                    2 -> DIRECTION_TYPE.BICYCLING
                    3 -> DIRECTION_TYPE.TRANSIT
                    else -> DIRECTION_TYPE.DRIVING
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                viewModel.directionType = DIRECTION_TYPE.DRIVING
            }

        }
    }

    private fun initViewPager() {
        binding.placeInfo.viewpager.adapter = PlaceTabsAdapter(childFragmentManager, lifecycle)

        binding.placeInfo.tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab != null) {
                    binding.placeInfo.viewpager.currentItem = tab.position
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        binding.placeInfo.viewpager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.placeInfo.tabs.selectTab(binding.placeInfo.tabs.getTabAt(position))
            }
        })
    }



    private fun observe() {
        viewModel.placeData.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.placeInfo.progressBar.show()
                    binding.placeInfo.iconArrowUp.hide()
                    binding.placeInfo.viewpager.hide()
                    binding.placeInfo.tabs.hide()

                    binding.motionLayout.transitionToState(R.id.particallyVisiblePlaceInfo)
                    binding.motionLayout.setTransition(R.id.expandPlaceInfoTransition)
                }
                is Result.Success -> {
                    binding.placeInfo.progressBar.hide()
                    binding.placeInfo.iconArrowUp.show()
                    binding.placeInfo.viewpager.show()
                    binding.placeInfo.tabs.show()
                }
                is Result.Failure -> {
                    binding.placeInfo.progressBar.hide()
                    Log.e(TAG, "place found exception: ${it.throwable.message}")
                    showSnackBar(it.throwable.message.orEmpty())
                }
            }
        }

        viewModel.direction.observe(viewLifecycleOwner) {
            when(it) {
                is Result.Loading -> {
                    binding.directionsChoosing.directionDataTable.hide()
                    binding.directionsChoosing.stepList.hide()
                    binding.directionsChoosing.progressBar.show()
                }
                is Result.Success -> {
                    binding.directionsChoosing.directionDataTable.show()
                    binding.directionsChoosing.stepList.show()
                    binding.directionsChoosing.progressBar.hide()

                    val direction = it.value
                    Log.w(TAG, "direction found: ${direction}")

                    updateDirectionInfo(direction)
                }
                is Result.Failure -> {
                    binding.directionsChoosing.stepList.show()
                    Log.e(TAG, "direction exception: ${it.throwable.message}")
                    showSnackBar(it.throwable.message.orEmpty())
                }
            }
        }

        viewModel.currentMapMode.observe(viewLifecycleOwner) {
            if(it == GoogleMapUtil.MAP_MODE.PLACE) {
                if(viewModel.currentPlaceId == null)
                    binding.motionLayout.transitionToState(R.id.hiddenPlaceInfo)
                else {
                    binding.motionLayout.setTransition(R.id.expandPlaceInfoTransition)
                    binding.motionLayout.transitionToState(R.id.particallyVisiblePlaceInfo)
                }

                binding.directionsButton.setImageResource(R.drawable.ic_distance)

                updateMapMode()
            } else {
                binding.motionLayout.setTransition(R.id.expandDirectionsTransition)
                binding.motionLayout.transitionToState(R.id.particallyVisibleDirections)
                binding.directionsButton.setImageResource(R.drawable.ic_baseline_close_24)
                updateMapMode()
            }
        }
    }

    private fun updateDirectionInfo(direction: Direction) {
        val stepMap = viewModel.googleMapUtil.createDirection(direction)

        val distanceRow = listOf(resources.getString(R.string.total_distance), direction.total_distance?.text)
        val durationRow = listOf(resources.getString(R.string.total_duration), direction.total_duration?.text)

        DataTableUtil.createTable(
            binding.directionsChoosing.directionDataTable,
            listOf(resources.getString(R.string.name), resources.getString(R.string.value)),
            listOf(distanceRow, durationRow)
        )

        Log.w(TAG, "steps ${stepMap?.keys?.toList()}")

        stepAdapter.submitList(stepMap?.keys?.toList())
        //binding.directionsChoosing.stepList.isResultEmpty = stepMap?.keys.isNullOrEmpty()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            Constants.SPEECH_RESULT_CODE -> {
                if(resultCode == AppCompatActivity.RESULT_OK) {
                    val list = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val text = list?.firstOrNull()
                    text?.let {
                        val intent = Autocomplete.IntentBuilder(
                            AutocompleteActivityMode.OVERLAY, placeFields
                        ).setInitialQuery(it)
                            .setCountry(viewModel.currentAddress.value?.countryCode)
                            .build(requireContext())
                        startActivityForResult(intent, Constants.AUTOCOMPLETE_RESULT_CODE)
                    }
                }
            }
            Constants.AUTOCOMPLETE_RESULT_CODE -> {
                if(data == null) return

                if (resultCode == AppCompatActivity.RESULT_OK) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    Log.i(TAG, "Place: " + place.name + ", " + place.id)
                    viewModel.setPlace(place)
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    // TODO: Handle the error.
                    val status = Autocomplete.getStatusFromIntent(data)
                    Log.i(TAG, status.statusMessage.orEmpty())
                }
            }
        }
    }
}