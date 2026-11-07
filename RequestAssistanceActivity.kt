package com.example.peairbrakes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestAssistanceActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userEmail: String = ""
    private var currentPhotoPath: String = ""
    private var locationSelected = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var ivPreview: ImageView
    private lateinit var tvLocationStatus: TextView

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            ivPreview.visibility = View.VISIBLE
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            ivPreview.setImageBitmap(bitmap)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            getLastLocation()
        } else {
            locationSelected = false
            latitude = 0.0
            longitude = 0.0
            tvLocationStatus.text = "Location: Not attached"
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val chooseLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {

                val data = result.data

                if (data != null && data.hasExtra("selected_latitude") && data.hasExtra("selected_longitude")) {

                    latitude = data.getDoubleExtra("selected_latitude", 0.0)

                    longitude = data.getDoubleExtra("selected_longitude", 0.0)

                    locationSelected = true

                    tvLocationStatus.text = "Location: Manually selected on map"

                    Toast.makeText(this, "Breakdown location selected!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun showLocationOptions() {
        val options = arrayOf("Use My Current Location", "Choose a Different Location")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Attach Breakdown Location")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        checkLocationPermission()
                    }
                    1 -> {
                        val intent = Intent(
                            this,
                            ChooseLocationActivity::class.java
                        )
                        chooseLocationLauncher.launch(intent)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isValidSouthAfricanPlate(plate: String): Boolean {

        // Remove spaces and hyphens and convert to uppercase
        val cleanedPlate = plate
            .uppercase(Locale.ROOT)
            .replace(" ", "")
            .replace("-", "")

        // Common South African registration formats
        val patterns = listOf(
            Regex("^[A-Z]{2,3}[0-9]{3,6}[A-Z]{0,3}$")
        )

        return patterns.any { it.matches(cleanedPlate) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_request_assistance)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.request_assistance_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "") ?: ""

        val etTruckReg = findViewById<EditText>(R.id.et_truck_reg)
        val etDescription = findViewById<EditText>(R.id.et_problem_description)
        ivPreview = findViewById(R.id.iv_problem_photo)
        tvLocationStatus = findViewById(R.id.tv_location_status)

        findViewById<Button>(R.id.btn_upload_photo).setOnClickListener {
            openCamera()
        }

        findViewById<Button>(R.id.btn_get_location)
            .setOnClickListener {
                showLocationOptions()
            }

        findViewById<Button>(R.id.btn_submit_request).setOnClickListener {
            val truckReg = etTruckReg.text.toString()
            val description = etDescription.text.toString()

            if (truckReg.isEmpty()) {
                Toast.makeText(this, "Please enter truck registration number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidSouthAfricanPlate(truckReg)) {
                Toast.makeText(this, "Invalid truck registration number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                Toast.makeText(this, "Please describe the problem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = AssistanceRequest(
                userEmail = userEmail,
                truckReg = truckReg,
                description = description,
                photoPath = currentPhotoPath,
                latitude = latitude,
                longitude = longitude
            )

            if (db.addAssistanceRequest(request)) {
                Toast.makeText(this, "Assistance request submitted!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            val customerIntent = Intent(this@RequestAssistanceActivity, CustomerDashboardActivity::class.java)
            startActivity(customerIntent)
            finish()
        }
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }
        
        photoFile?.also {
            val photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            takePictureLauncher.launch(photoUri)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun checkLocationPermission() {
        val fineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineLocation || coarseLocation) {
            getLastLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun getLastLocation() {
        val fineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineLocation && !coarseLocation) {
            return
        }
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        locationSelected = true
                        tvLocationStatus.text = "Location: Current location attached"

                        Toast.makeText(this, "Last known location captured!", Toast.LENGTH_SHORT).show()

                    } else {

                        locationSelected = false
                        latitude = 0.0
                        longitude = 0.0

                        tvLocationStatus.text = "Location unavailable. Choose a location on the map."

                        Toast.makeText(this, "Unable to get your last location. Please choose a location on the map.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {

                    locationSelected = false
                    latitude = 0.0
                    longitude = 0.0

                    tvLocationStatus.text = "Location: Not attached"

                    Toast.makeText(this, "Could not retrieve location. Please choose it on the map.", Toast.LENGTH_LONG).show()
                }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show()
        }
    }
}