package com.example.peairbrakes

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class AdminLiveMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var tvTechsCount: TextView
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "PeAirbrakes/1.0 (za.co.peairbrakes.app)"

        setContentView(R.layout.activity_admin_live_map)

        db = DatabaseHelper(this)
        mapView = findViewById(R.id.admin_osm_map)
        tvTechsCount = findViewById(R.id.tv_active_techs_count)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(11.0)
        mapView.controller.setCenter(GeoPoint(-33.9608, 25.6022)) // Port Elizabeth / Gqeberha

        plotMapOverlays()
    }

    private fun plotMapOverlays() {
        mapView.overlays.clear()

        val techs = db.getAllTechnicians()
        val requests = db.getAllRequests()

        var activeCount = 0

        // 1. Plot Technician Location Markers
        for (tech in techs) {
            if (tech.latitude != 0.0 && tech.longitude != 0.0) {
                val point = GeoPoint(tech.latitude, tech.longitude)
                val marker = Marker(mapView)
                marker.position = point
                marker.title = "👨‍🔧 ${tech.name} (${tech.specialty})\nStatus: ${tech.availabilityStatus}\nPhone: ${tech.phone}"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
                activeCount++
            }
        }

        // 2. Plot Active Breakdown Location Markers
        for (req in requests) {
            if (req.status != "Completed" && req.status != "Closed" && req.latitude != 0.0 && req.longitude != 0.0) {
                val point = GeoPoint(req.latitude, req.longitude)
                val marker = Marker(mapView)
                marker.position = point
                marker.title = "🚨 Breakdown Job #${req.id}\nTruck: ${req.truckReg}\nStatus: ${req.status}\nAssigned: ${req.assignedTo}"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
            }
        }

        tvTechsCount.text = "Active Technicians: $activeCount | Active Breakdown Sites: ${requests.count { it.status != "Completed" }}"
        mapView.invalidate()
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
