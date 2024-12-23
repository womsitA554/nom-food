package com.example.nom_food_kotlin.Activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import com.example.nom_food_kotlin.R
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.example.nom_food_kotlin.databinding.ActivityMapBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.squareup.okhttp.*
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.IOException
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val FINE_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var binding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (isLocationEnabled()) {
            getLastLocation()
        } else {
            showMotionToast("Please enable location services", MotionToastStyle.ERROR)
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnSearch.setOnClickListener {
            val location = binding.etSearch.text.toString().trim()

            if (location.isNotEmpty()) {
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val addressList = geocoder.getFromLocationName(location, 1)
                    if (addressList != null && addressList.isNotEmpty()) {
                        val address = addressList[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        googleMap.clear() // Clear previous markers
                        googleMap.addMarker(MarkerOptions().position(latLng).title(location))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                    } else {
                        showMotionToast("Location not found", MotionToastStyle.ERROR)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    showMotionToast("Error retrieving location: ${e.localizedMessage}", MotionToastStyle.ERROR)
                }
            } else {
                showMotionToast("Please enter a location", MotionToastStyle.ERROR)
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_PERMISSIONS_REQUEST_CODE)
            return
        }

        val task: Task<Location> = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            } else {
                Log.e("MapActivity", "Current location is null, requesting new location data")
                requestNewLocationData()
            }
        }
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult.locations.isNotEmpty()) {
                        currentLocation = locationResult.lastLocation
                        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                        mapFragment.getMapAsync(this@MapActivity)
                    } else {
                        Log.e("MapActivity", "LocationResult is empty")
                        showMotionToast("Unable to get current location", MotionToastStyle.ERROR)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        currentLocation?.let {
            val defaultLocation = LatLng(it.latitude, it.longitude)
            googleMap.addMarker(MarkerOptions().position(defaultLocation).title("My Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        } ?: run {
            Log.e("MapActivity", "Current location is null in onMapReady")
            showMotionToast("Unable to get current location", MotionToastStyle.ERROR)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                showMotionToast("Location Permission Denied, please allow the permission to access your location", MotionToastStyle.ERROR)
            }
        }
    }

    private fun showMotionToast(message: String, style: MotionToastStyle) {
        MotionToast.createColorToast(
            this,
            null,
            message,
            style,
            Gravity.TOP,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
        )
    }
}
