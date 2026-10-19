package com.example.peairbrakes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
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
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class JobCardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var reqId: Int = -1
    private lateinit var jobCard: JobCard

    private var startLocation: Location? = null
    private var endLocation: Location? = null
    private var currentPhotoType: String = "" // "Job" or "Refused"
    private var currentPhotoPath: String = ""

    private lateinit var tvStartTime: TextView
    private lateinit var tvTravelStatus: TextView
    private lateinit var etTruckReg: EditText
    private lateinit var etTruckMake: EditText
    private lateinit var etKilometers: EditText
    private lateinit var etDescription: EditText
    private lateinit var etParts: EditText
    private lateinit var etRefused: EditText
    private lateinit var signatureView: SignatureView
    private lateinit var btnStartKm: Button
    private lateinit var btnStopKm: Button

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            if (currentPhotoType == "Refused") {
                jobCard.refusedPhotoPath = currentPhotoPath
                Toast.makeText(this, "Refused work photo attached", Toast.LENGTH_SHORT).show()
            } else {
                // For simplicity, we just store one optional photo in this demo
                Toast.makeText(this, "Photo attached to job card", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_card)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.job_card_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        reqId = intent.getIntExtra("req_id", -1)
        val truckReg = intent.getStringExtra("truck_reg") ?: ""

        // Initialize UI
        tvStartTime = findViewById(R.id.tv_start_time_display)
        tvTravelStatus = findViewById(R.id.tv_travel_status)
        etTruckReg = findViewById(R.id.et_truck_reg)
        etTruckMake = findViewById(R.id.et_truck_make)
        etKilometers = findViewById(R.id.et_kilometers)
        etDescription = findViewById(R.id.et_job_description)
        etParts = findViewById(R.id.et_parts_used)
        etRefused = findViewById(R.id.et_refused_repairs)
        signatureView = findViewById(R.id.signature_view)
        btnStartKm = findViewById(R.id.btn_start_km)
        btnStopKm = findViewById(R.id.btn_stop_km)

        etTruckReg.setText(truckReg)

        // Load existing job card or create new one
        jobCard = db.getJobCard(reqId) ?: JobCard(reqId = reqId, status = "Not Started")
        if (jobCard.startTime.isEmpty()) {
            jobCard.startTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            jobCard.status = "Started"
            db.updateJobCard(jobCard)
        }
        tvStartTime.text = "Started: ${jobCard.startTime}"

        btnStartKm.setOnClickListener {
            checkLocationPermission {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        startLocation = location
                        jobCard.status = "Traveling"
                        btnStartKm.isEnabled = false
                        btnStopKm.isEnabled = true
                        tvTravelStatus.text = "Status: Tracking KM..."
                        Toast.makeText(this, "KM Tracking Started", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnStopKm.setOnClickListener {
            checkLocationPermission {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null && startLocation != null) {
                        endLocation = location
                        val distanceMeters = startLocation!!.distanceTo(endLocation!!)
                        val distanceKm = distanceMeters / 1000.0
                        jobCard.travelKm = distanceKm * 2.0 // Double the KM as requested
                        jobCard.status = "On Site"
                        btnStopKm.isEnabled = false
                        tvTravelStatus.text = "Status: Arrived. Travel KM: ${String.format("%.2f", jobCard.travelKm)} (Doubled)"
                        Toast.makeText(this, "KM Tracking Stopped", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.btn_job_photo).setOnClickListener {
            currentPhotoType = "Job"
            openCamera()
        }

        findViewById<Button>(R.id.btn_refused_photo).setOnClickListener {
            currentPhotoType = "Refused"
            openCamera()
        }

        findViewById<Button>(R.id.btn_clear_sig).setOnClickListener {
            signatureView.clear()
        }

        findViewById<Button>(R.id.btn_submit_job).setOnClickListener {
            submitJobCard()
        }
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }
        photoFile?.also {
            val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            takePictureLauncher.launch(photoUri)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JOB_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun checkLocationPermission(onGranted: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) onGranted()
            }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            onGranted()
        }
    }

    private fun submitJobCard() {
        jobCard.truckMake = etTruckMake.text.toString()
        jobCard.kilometers = etKilometers.text.toString().toDoubleOrNull() ?: 0.0
        jobCard.partsUsed = etParts.text.toString()
        jobCard.refusedRepairs = etRefused.text.toString()
        jobCard.endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        jobCard.status = "Completed"

        // Save signature
        val sigBitmap = signatureView.getSignatureBitmap()
        val sigFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SIG_${reqId}.png")
        val out = FileOutputStream(sigFile)
        sigBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        out.flush()
        out.close()
        jobCard.signaturePath = sigFile.absolutePath

        if (db.updateJobCard(jobCard)) {
            Toast.makeText(this, "Job Card Submitted Successfully!", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save job card", Toast.LENGTH_SHORT).show()
        }
    }
}