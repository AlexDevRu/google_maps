package com.example.maps.ui.fragments.main

import android.content.Intent
import android.location.Address
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
import com.example.maps.R
import com.example.maps.databinding.FragmentMainBinding
import com.example.maps.ui.adapters.CustomInfoWindowAdapter
import com.example.maps.ui.adapters.PlaceTabsAdapter
import com.example.maps.ui.adapters.StepAdapter
import com.example.maps.ui.custom.WorkaroundMapFragment
import com.example.maps.ui.fragments.base.GoogleMapBaseFragment
import com.example.maps.utils.Constants
import com.example.maps.utils.DataTableUtil
import com.example.maps.utils.extensions.hide
import com.example.maps.utils.extensions.show
import com.github.core.common.DIRECTION_MARKER
import com.github.core.common.DIRECTION_TYPE
import com.github.core.common.MAP_MODE
import com.github.core.common.Result
import com.github.core.models.directions.Direction
import com.github.core.models.place_info.PlaceInfo
import com.github.googlemapfragment.android.listeners.*
import com.github.googlemapfragment.android.models.DirectionSegmentUI
import com.github.googlemapfragment.android.utils.MapUtils
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.maps.android.ktx.utils.sphericalDistance
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MainFragment: GoogleMapBaseFragment<FragmentMainBinding>(R.id.map, FragmentMainBinding::inflate)
    , IMyLocationChangedListener, IDirectionListener, IMapModeChangedListener,
    IDirectionMarkersChangedListener, IPlaceInfoStatusChangedListener {

    companion object {
        private const val TAG = "MainFragment"
    }

    private val viewModel by sharedViewModel<MainVM>()

    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private val args: MainFragmentArgs by navArgs()

    private val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

    private var firstInit = false

    private lateinit var stepAdapter: StepAdapter

    private lateinit var googleMap: GoogleMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(placeFields)

        firstInit = savedInstanceState == null
    }

    override fun onCurrentAddressChange(address: Address) {
        viewModel.currentCountryCode = address.countryCode
        autocompleteFragment.setCountry(address.countryCode)
    }

    override fun onCurrentLocationChange(latLng: LatLng) {
        if(firstInit && args.markdown == null) {
            moveToCurrentLocation()
            firstInit = false
        }

        if(
            isDirectionBuildingAvailable &&
            myLocationSynchronizedWithOrigin
        ) {
            if(viewModel.originLatLng != null) {
                val distance = viewModel.originLatLng!!.sphericalDistance(latLng)
                if(distance > MainVM.minDistanceForUpdate) {
                    viewModel.originLatLng = latLng
                    getDirection(viewModel.directionType)
                }
            } else {
                viewModel.originLatLng = latLng
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        myLocationChangedListener = this
        directionListener = this
        placeInfoStatusChangedListener = this
        mapModeChangedListener = this
        directionMarkersChangedListener = this

        super.onMapReady(googleMap)

        this.googleMap = googleMap

        if (isCoarseAndFineLocationPermissionsGranted()) {
            Log.w(TAG, "firstinit $firstInit")

            if(firstInit && args.markdown != null) {
                val markdownLocation = args.markdown!!.location ?: return
                val latLng = LatLng(markdownLocation.latitude, markdownLocation.longitude)
                setMapMode(MAP_MODE.PLACE)
                setMarkerByPlace(args.markdown!!.placeId, latLng)
                moveCamera(latLng)
            }

            infoWindowAdapter = CustomInfoWindowAdapter(requireContext())

            initViews()
        }
    }

    override fun onOriginLocationChange(latLng: LatLng?) {
        if(latLng != null) {
            val address = MapUtils.getAddressByLocation(requireContext(), latLng)
            binding.directionsChoosing.originButton.text = address?.getAddressLine(0)
        } else {
            binding.directionsChoosing.originButton.text = getString(R.string.choose_origin)
        }
        setDirectionButtonEnable()
    }

    override fun onDestinationLocationChange(latLng: LatLng?) {
        if(latLng != null) {
            val address = MapUtils.getAddressByLocation(requireContext(), latLng)
            binding.directionsChoosing.destinationButton.text = address?.getAddressLine(0)
        } else {
            binding.directionsChoosing.destinationButton.text = getString(R.string.choose_destination)
        }
        setDirectionButtonEnable()
    }

    private fun initViews() {
        initViewPager()
        initDirectionTypes()

        binding.myLocationButton.setOnClickListener {
            moveToCurrentLocation()
        }

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: " + place.name + ", " + place.id + ", " + place.latLng)
                if(place.id != null && place.latLng != null) {
                    setMarkerByPlace(place.id!!, place.latLng!!)
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
            toggleMapMode()
        }

        binding.directionsChoosing.originButton.setOnClickListener {
            setDirectionMarkerType(DIRECTION_MARKER.ORIGIN)
        }

        binding.directionsChoosing.destinationButton.setOnClickListener {
            setDirectionMarkerType(DIRECTION_MARKER.DESTINATION)
        }

        binding.directionsChoosing.buildDirectionButton.setOnClickListener {
            getDirection(viewModel.directionType)
        }

        compositeDisposable.add(
            viewModel.myLocationSyncWithOrigin.subscribe {
                Log.e(TAG, "subscribe myLocationSyncWithOrigin $it")
                myLocationSynchronizedWithOrigin = it
                val colorRes = if(it) R.color.blue else R.color.black
                val color = ContextCompat.getColor(requireContext(), colorRes)
                binding.directionsChoosing.myLocationSyncButton.setColorFilter(color)
            }
        )

        binding.directionsChoosing.myLocationSyncButton.setOnClickListener {
            viewModel.toggleSyncOriginWithMyLocation()
        }
    }

    override fun onDirectionMarkerTypeChange(directionMarker: DIRECTION_MARKER) {
        Log.w(TAG, "directionMarkerTypeChanged ${getMapMode()} ${directionMarker}")
        val originButton = binding.directionsChoosing.originButton as MaterialButton
        val destinationButton = binding.directionsChoosing.destinationButton as MaterialButton

        if(getMapMode() == MAP_MODE.DIRECTION
            && getDirectionMarkerType() == DIRECTION_MARKER.ORIGIN) {

            autocompleteFragment.setHint(resources.getString(R.string.choose_origin))
            highlightDirectionButton(originButton, true)
            highlightDirectionButton(destinationButton, false)
        }
        else if(getMapMode() == MAP_MODE.DIRECTION
            && getDirectionMarkerType() == DIRECTION_MARKER.DESTINATION) {

            autocompleteFragment.setHint(resources.getString(R.string.choose_destination))
            highlightDirectionButton(originButton, false)
            highlightDirectionButton(destinationButton, true)
        }
        else {
            autocompleteFragment.setHint(resources.getString(R.string.search))
        }
    }

    private fun setDirectionButtonEnable() {
        binding.directionsChoosing.buildDirectionButton.isEnabled = isDirectionBuildingAvailable
    }

    override fun onMapModeChange(mapMode: MAP_MODE) {
        onDirectionMarkerTypeChange(getDirectionMarkerType())

        if(mapMode == MAP_MODE.PLACE) {
            if(viewModel.currentPlaceId == null)
                binding.motionLayout.transitionToState(R.id.hiddenPlaceInfo)
            else {
                binding.motionLayout.setTransition(R.id.expandPlaceInfoTransition)
                binding.motionLayout.transitionToState(R.id.particallyVisiblePlaceInfo)
            }

            binding.directionsButton.setImageResource(R.drawable.ic_distance)
        } else {
            binding.motionLayout.setTransition(R.id.expandDirectionsTransition)
            binding.motionLayout.transitionToState(R.id.particallyVisibleDirections)
            binding.directionsButton.setImageResource(R.drawable.ic_baseline_close_24)
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

    override fun onPlaceInfoStatusChange(placeInfoResult: Result<PlaceInfo>) {
        viewModel.placeInfo.onNext(placeInfoResult)
        when(placeInfoResult) {
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

                if(placeInfoResult.value.placeId != null)
                    viewModel.findPlaceInMarkdowns(placeInfoResult.value.placeId!!)
            }
            is Result.Failure -> {
                binding.placeInfo.progressBar.hide()
                Log.e(TAG, "place found exception: ${placeInfoResult.throwable.message}")
                showSnackBar(placeInfoResult.throwable.message.orEmpty())
            }
        }
    }

    override fun onDirectionChange(directionResult: Result<Direction>) {
        when(directionResult) {
            is Result.Loading -> {
                binding.directionsChoosing.directionDataTable.hide()
                binding.directionsChoosing.stepList.hide()
                binding.directionsChoosing.progressBar.show()
            }
            is Result.Success -> {
                binding.directionsChoosing.directionDataTable.show()
                binding.directionsChoosing.stepList.show()
                binding.directionsChoosing.progressBar.hide()

                val direction = directionResult.value
                Log.w(TAG, "direction found: ${direction}")

                DataTableUtil.createTable(
                    binding.directionsChoosing.directionDataTable,
                    listOf(resources.getString(R.string.total_distance), resources.getString(R.string.total_duration)),
                    listOf(listOf(direction.total_distance?.text.orEmpty(), direction.total_duration?.text.orEmpty()))
                )
            }
            is Result.Failure -> {
                binding.directionsChoosing.stepList.show()
                Log.e(TAG, "direction exception: ${directionResult.throwable.message}")
                showSnackBar(directionResult.throwable.message.orEmpty())
            }
        }
    }

    override fun onDirectionRender(directionsSegments: List<DirectionSegmentUI>) {
        Log.e(TAG, "directionRendered directionRendered $directionsSegments")

        viewModel.directionsSegments = directionsSegments

        directionsSegments.forEach {
            it.marker.title = resources.getString(R.string.distance, it.step.distance.text)
            it.marker.snippet =
                "${resources.getString(R.string.duration, it.step.duration.text)}\n\n${it.step.html_instructions}"
        }

        val steps = directionsSegments.map { it.step }
        Log.w(TAG, "steps ${steps}")

        stepAdapter.submitList(steps)

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
                            .setCountry(viewModel.currentCountryCode)
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
                    if(place.id != null && place.latLng != null)
                        setMarkerByPlace(place.id!!, place.latLng!!)
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    val status = Autocomplete.getStatusFromIntent(data)
                    Log.i(TAG, status.statusMessage.orEmpty())
                }
            }
        }
    }
}