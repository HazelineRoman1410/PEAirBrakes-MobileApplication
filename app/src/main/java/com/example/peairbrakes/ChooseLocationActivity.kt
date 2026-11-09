
package com.example.peairbrakes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.library.BuildConfig

class ChooseLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var tvCoordinates: TextView
    private lateinit var btnConfirm: Button

    private var selectedLatitude = -29.0
    private var selectedLongitude = 24.0

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                moveToCurrentLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        Configuration.getInstance().userAgentValue = "PeAirbrakes/1.0 (za.co.peairbrakes.app)"

        setContentView(R.layout.activity_choose_location)

        mapView = findViewById(R.id.osm_map)
        tvCoordinates = findViewById(R.id.tv_selected_coordinates)
        btnConfirm = findViewById(R.id.btn_confirm_location)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)


        mapView.controller.setZoom(5.0)
        mapView.controller.setCenter(GeoPoint(-29.0, 24.0))

        btnConfirm.setOnClickListener {

            val centre = mapView.mapCenter as GeoPoint

            selectedLatitude = centre.latitude
            selectedLongitude = centre.longitude

            val result = intent.apply {
                putExtra("selected_latitude", selectedLatitude)
                putExtra("selected_longitude", selectedLongitude)
            }

            setResult(RESULT_OK, result)
            finish()
        }

        tvCoordinates.text = "Finding your location..."

        if (hasLocationPermission()) {
            moveToCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }


        mapView.addMapListener(
            object : org.osmdroid.events.MapListener {

                override fun onScroll(
                    event: org.osmdroid.events.ScrollEvent?
                ): Boolean {
                    updateCoordinates()
                    return true
                }

                override fun onZoom(
                    event: org.osmdroid.events.ZoomEvent?
                ): Boolean {
                    updateCoordinates()
                    return true
                }
            }
        )
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    private fun moveToCurrentLocation() {
        if (!hasLocationPermission()) return

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {

                        val currentPosition = GeoPoint(
                            location.latitude,
                            location.longitude
                        )

                        mapView.controller.setZoom(16.0)
                        mapView.controller.animateTo(currentPosition)

                        updateCoordinates()
                    } else {
                        updateCoordinates()
                    }
                }
        } catch (e: SecurityException) {
            updateCoordinates()
        }
    }

    private fun updateCoordinates() {
        val centre = mapView.mapCenter as GeoPoint

        selectedLatitude = centre.latitude
        selectedLongitude = centre.longitude

        tvCoordinates.text =
            "Selected location: %.6f, %.6f".format(
                selectedLatitude,
                selectedLongitude
            )
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDetach()
        super.onDestroy()
    }
}
