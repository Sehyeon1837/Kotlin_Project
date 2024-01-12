package com.example.kotiln_project

import android.content.ContentValues
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.kotiln_project.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private var PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initializeWithNewPlacesApiEnabled(applicationContext, "Google_API_KEY")

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val findRestrtButton = findViewById<Button>(R.id.findRestrtButton)
        findRestrtButton.setOnClickListener {
            showCurrentPlace(restaurantTypes)
        }

        val findHotelButton = findViewById<Button>(R.id.findHotelButton)
        findHotelButton.setOnClickListener {
            showCurrentPlace(hotelTypes)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationPermission()
        getDeviceLocation()
        updateLocationUI()
    }

    private fun getLocationPermission() {
        if(ContextCompat.checkSelfPermission(this.applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION)
            ==  PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun getDeviceLocation() {
        try {
            if(locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        //현재 위치를 찾은 경우, 위치 정보 사용
                        val lastKnownLocation = task.result
                        if(lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation.latitude,
                                    lastKnownLocation.longitude), 18f))
                        }
                    } else {
                        //현재 위치 못찾음 위치 하나 설정하기
                        Log.e(ContentValues.TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(33.362361, 126.533277), 18f))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch(e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun updateLocationUI() {
        if(mMap == null) {
            return
        }
        try {
            if(locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
            }
        } catch(e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private val restaurantTypes = listOf(Place.Type.CAFE, Place.Type.BAKERY, Place.Type.RESTAURANT, Place.Type.MEAL_TAKEAWAY)
    private val hotelTypes = listOf(Place.Type.LODGING)
    private val atmType = listOf(Place.Type.ATM, Place.Type.BANK)

    private fun showCurrentPlace(types: List<Place.Type>) {
        if(mMap == null || !locationPermissionGranted) {
            return
        }

        mMap.clear()

        val placesClient = Places.createClient(this)
        val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES))

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val placeResponse = placesClient.findCurrentPlace(request)
        placeResponse.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val response = task.result
                for(placeLikelihood in response?.placeLikelihoods ?: emptyList()) {
                    val place = placeLikelihood.place
                    if(place.types.any {it in types}) {
                        mMap.addMarker(MarkerOptions()
                            .title(place.name)
                            .position(place.latLng))
                    }
                }
            } else {
                Log.e(ContentValues.TAG, "Exception: %s", task.exception)
            }
        }
    }
}
