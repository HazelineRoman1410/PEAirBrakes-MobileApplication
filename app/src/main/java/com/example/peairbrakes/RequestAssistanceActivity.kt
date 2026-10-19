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
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLastLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

        findViewById<Button>(R.id.btn_get_location).setOnClickListener {
            checkLocationPermission()
        }

        findViewById<Button>(R.id.btn_submit_request).setOnClickListener {
            val truckReg = etTruckReg.text.toString()
            val description = etDescription.text.toString()

            if (truckReg.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please provide truck registration and description", Toast.LENGTH_SHORT).show()
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    tvLocationStatus.text = "Location: Attached (${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)})"
                    Toast.makeText(this, "Location captured!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Unable to get location. Make sure GPS is on.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}