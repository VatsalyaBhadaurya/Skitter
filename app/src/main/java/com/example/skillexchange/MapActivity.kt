package com.example.skillexchange

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vatty.skitter.R
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import android.location.LocationManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.skillexchange.data.AppDatabase
import android.widget.ImageButton
import com.google.android.material.button.MaterialButton
import android.content.Intent
import com.vatty.skitter.MainActivity

class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var locationEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var useGpsButton: Button
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var backButton: ImageButton
    private lateinit var continueButton: MaterialButton
    private val LOCATION_PERMISSION_REQUEST = 1
    private var selectedLocation: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, android.preference.PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.map)
        locationEditText = findViewById(R.id.locationEditText)
        searchButton = findViewById(R.id.searchButton)
        useGpsButton = findViewById(R.id.useGpsButton)
        fabMyLocation = findViewById(R.id.fabMyLocation)
        backButton = findViewById(R.id.backButton)
        continueButton = findViewById(R.id.continueButton)

        setupMap()
        setupButtons()
    }

    private fun setupMap() {
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(13.0)
    }

    private fun setupButtons() {
        searchButton.setOnClickListener {
            val location = locationEditText.text.toString()
            if (location.isNotEmpty()) {
                searchLocation(location)
            }
        }

        useGpsButton.setOnClickListener {
            checkLocationPermission()
        }

        fabMyLocation.setOnClickListener {
            checkLocationPermission()
        }

        backButton.setOnClickListener {
            if (selectedLocation != null) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Discard Location?")
                    .setMessage("Are you sure you want to go back? Your selected location will not be saved.")
                    .setPositiveButton("Yes") { _, _ ->
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                finish()
            }
        }

        continueButton.setOnClickListener {
            selectedLocation?.let { location ->
                saveUserLocation(location)
                startMatchingActivity()
            }
        }
    }

    private fun searchLocation(locationName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@MapActivity)
                val addresses = geocoder.getFromLocationName(locationName, 1)
                
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val location = GeoPoint(address.latitude, address.longitude)
                    
                    withContext(Dispatchers.Main) {
                        updateMapLocation(location)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapActivity, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "Error finding location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST)
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val currentLocation = GeoPoint(location.latitude, location.longitude)
                updateMapLocation(currentLocation)
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMapLocation(location: GeoPoint) {
        mapView.overlays.clear()
        val marker = Marker(mapView)
        marker.position = location
        marker.title = "Selected Location"
        mapView.overlays.add(marker)
        mapView.controller.animateTo(location)
        
        selectedLocation = location
        continueButton.isEnabled = true
    }

    private fun saveUserLocation(location: GeoPoint) {
        // Save to SharedPreferences
        getSharedPreferences("user_prefs", MODE_PRIVATE)
            .edit()
            .putString("user_latitude", location.latitude.toString())
            .putString("user_longitude", location.longitude.toString())
            .apply()

        // Save to database
        val userEmail = getSharedPreferences("login", MODE_PRIVATE)
            .getString("userEmail", "") ?: ""
        
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.userDao().updateUserLocation(userEmail, location.latitude, location.longitude)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MapActivity,
                        "Error saving location to database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun startMatchingActivity() {
        val userEmail = getSharedPreferences("login", MODE_PRIVATE)
            .getString("userEmail", "") ?: ""

        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                selectedLocation?.let { location ->
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@MapActivity, MatchingActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MapActivity,
                        "Error saving location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateBack() {
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()
    }

    override fun onBackPressed() {
        if (selectedLocation != null) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Discard Location?")
                .setMessage("Are you sure you want to go back? Your selected location will not be saved.")
                .setPositiveButton("Yes") { _, _ ->
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            finish()
        }
    }
}