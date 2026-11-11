
package com.example.peairbrakes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
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
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class JobCardFirebaseData(
    var reqId: Int = -1,
    var kmTraveled: String = "0.0",
    var registration: String = "",
    var make: String = "",
    var odometer: String = "",
    var problem: String = "",
    var parts: String = "",
    var photo: String = "",
    var refusedPhoto: String = "",
    var refusedRepairs: String = "",
    var signature: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var status: String = "",
    var startLatitude: Double = 0.0,
    var startLongitude: Double = 0.0,
    var endLatitude: Double = 0.0,
    var endLongitude: Double = 0.0
)

class JobCardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var reqId: Int = -1
    private lateinit var jobCard: JobCard

    private var startLocation: Location? = null
    private var endLocation: Location? = null
    private var currentPhotoType: String = ""
    private var currentPhotoPath: String = ""
    private var jobPhotoPath: String = ""
    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0
    private var endLatitude: Double = 0.0
    private var endLongitude: Double = 0.0
    private var pendingLocationAction: (() -> Unit)? = null

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

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                if (currentPhotoType == "Refused") {
                    jobCard.refusedPhotoPath = currentPhotoPath
                    Toast.makeText(this, "Refused work photo attached", Toast.LENGTH_SHORT).show()
                } else {
                    jobPhotoPath = currentPhotoPath
                    Toast.makeText(this, "Photo attached to job card", Toast.LENGTH_SHORT).show()
                }
                saveJobCardToBoth(false)
            } else {
                Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                pendingLocationAction?.invoke()
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show()
            }

            pendingLocationAction = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_card)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.job_card_root)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        database = FirebaseDatabase.getInstance().getReference("jobCards")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        reqId = intent.getIntExtra("req_id", -1)
        val truckReg = intent.getStringExtra("truck_reg") ?: ""

        if (reqId == -1) {
            Toast.makeText(this, "Invalid assistance request", Toast.LENGTH_LONG).show()
            finish()
            return
        }

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

        jobCard = db.getJobCard(reqId) ?: JobCard(
            reqId = reqId,
            status = "Not Started"
        )

        if (jobCard.startTime.isEmpty()) {
            jobCard.startTime =
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            jobCard.status = "Started"
            saveJobCardToBoth(false)
        }

        tvStartTime.text = "Started: ${jobCard.startTime}"
        tvTravelStatus.text = "Status: ${jobCard.status}"

        btnStartKm.isEnabled = jobCard.status != "Traveling" &&
                jobCard.status != "On Site" &&
                jobCard.status != "Completed"

        btnStopKm.isEnabled = jobCard.status == "Traveling"

        btnStartKm.setOnClickListener {
            checkLocationPermission {
                getCurrentLocation { location ->
                    if (location != null) {
                        startLocation = location
                        startLatitude = location.latitude
                        startLongitude = location.longitude

                        jobCard.status = "Traveling"

                        btnStartKm.isEnabled = false
                        btnStopKm.isEnabled = true
                        tvTravelStatus.text = "Status: Tracking KM..."

                        saveJobCardToBoth(false)

                        Toast.makeText(
                            this,
                            "KM Tracking Started",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Unable to get current location. Please enable your device location.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        btnStopKm.setOnClickListener {
            checkLocationPermission {
                getCurrentLocation { location ->
                    if (location != null && startLocation != null) {
                        endLocation = location
                        endLatitude = location.latitude
                        endLongitude = location.longitude

                        val distanceMeters = startLocation!!.distanceTo(endLocation!!)
                        val distanceKm = distanceMeters / 1000.0

                        jobCard.travelKm = distanceKm * 2.0
                        jobCard.status = "On Site"

                        btnStopKm.isEnabled = false

                        tvTravelStatus.text =
                            "Status: Arrived. Travel KM: ${
                                String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    jobCard.travelKm
                                )
                            } (Doubled)"

                        saveJobCardToBoth(false)

                        Toast.makeText(
                            this,
                            "KM Tracking Stopped",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Unable to calculate travel distance. Start KM tracking again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        signatureView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> {
                    view.parent.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
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

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            startActivity(Intent(this, ActiveBreakdownsActivity::class.java))
            finish()
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoUri)
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Unable to open camera: ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IllegalStateException("Picture storage unavailable")

        return File.createTempFile("JOB_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun checkLocationPermission(onGranted: () -> Unit) {
        val fineLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocation || coarseLocation) {
            onGranted()
        } else {
            pendingLocationAction = onGranted
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        val fineLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocation && !coarseLocation) {
            onLocationReceived(null)
            return
        }

        try {
            val priority = if (fineLocation) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            fusedLocationClient.getCurrentLocation(
                priority,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location)
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            onLocationReceived(lastLocation)
                        }
                        .addOnFailureListener {
                            onLocationReceived(null)
                        }
                }
            }.addOnFailureListener {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        onLocationReceived(lastLocation)
                    }
                    .addOnFailureListener {
                        onLocationReceived(null)
                    }
            }
        } catch (ex: SecurityException) {
            onLocationReceived(null)
        }
    }

    private fun createFirebaseJobCard(): JobCardFirebaseData {
        return JobCardFirebaseData(
            reqId = reqId,
            kmTraveled = jobCard.travelKm.toString(),
            registration = etTruckReg.text.toString(),
            make = etTruckMake.text.toString(),
            odometer = etKilometers.text.toString(),
            problem = etDescription.text.toString(),
            parts = etParts.text.toString(),
            photo = jobPhotoPath,
            refusedPhoto = jobCard.refusedPhotoPath,
            refusedRepairs = etRefused.text.toString(),
            signature = jobCard.signaturePath,
            startTime = jobCard.startTime,
            endTime = jobCard.endTime,
            status = jobCard.status,
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            endLatitude = endLatitude,
            endLongitude = endLongitude
        )
    }

    private fun saveJobCardToBoth(showToast: Boolean) {
        val sqliteSaved = db.updateJobCard(jobCard)
        val firebaseJobCard = createFirebaseJobCard()

        database.child(reqId.toString())
            .setValue(firebaseJobCard)
            .addOnSuccessListener {
                if (showToast) {
                    if (sqliteSaved) {
                        Toast.makeText(
                            this,
                            "Job card saved successfully in SQLite and Firebase",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Firebase saved, but SQLite update failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .addOnFailureListener { error ->
                val message = if (sqliteSaved) {
                    "Saved in SQLite, but Firebase failed: ${error.message}"
                } else {
                    "SQLite and Firebase save failed: ${error.message}"
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }

        if (!sqliteSaved) {
            Toast.makeText(
                this,
                "Failed to update job card in SQLite",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun submitJobCard() {
        jobCard.truckMake = etTruckMake.text.toString()
        jobCard.kilometers = etKilometers.text.toString().toDoubleOrNull() ?: 0.0
        jobCard.partsUsed = etParts.text.toString()
        jobCard.refusedRepairs = etRefused.text.toString()
        jobCard.endTime =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        jobCard.status = "Completed"

        val sigBitmap = signatureView.getSignatureBitmap()
        val picturesDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (picturesDirectory == null) {
            Toast.makeText(this, "Unable to access picture storage", Toast.LENGTH_SHORT).show()
            return
        }

        val sigFile = File(picturesDirectory, "SIG_${reqId}.png")

        try {
            FileOutputStream(sigFile).use { out ->
                sigBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "Unable to save signature: ${ex.message}", Toast.LENGTH_LONG).show()
            return
        }

        jobCard.signaturePath = sigFile.absolutePath

        jobCard.signaturePath = sigFile.absolutePath

        val sqliteSaved = db.updateJobCard(jobCard)

        if (!sqliteSaved) {
            Toast.makeText(
                this,
                "Failed to save job card in SQLite",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val firebaseJobCard = createFirebaseJobCard()

        database.child(reqId.toString())
            .setValue(firebaseJobCard)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Job card submitted successfully to SQLite and Firebase!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Saved in SQLite, but Firebase failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
