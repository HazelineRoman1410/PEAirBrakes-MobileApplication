package com.example.peairbrakes

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ReturnJobActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var reqId: Int = -1

    private lateinit var spinnerReasons: Spinner
    private lateinit var etDesc: EditText

    private val reasons = arrayOf(
        "Repair Unsuccessful",
        "Additional Specialist Required",
        "Parts Unavailable",
        "Repair Incomplete",
        "Further Diagnosis Required",
        "Customer Declined Repair",
        "Unsafe Working Conditions",
        "Workmanship Concern",
        "Warranty Return Visit",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_return_job)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.return_job_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        reqId = intent.getIntExtra("req_id", -1)

        spinnerReasons = findViewById(R.id.spinner_flag_reasons)
        etDesc = findViewById(R.id.et_flag_desc)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reasons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReasons.adapter = adapter

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this, reqId) }

        findViewById<Button>(R.id.btn_submit_return_flag).setOnClickListener { submitReturnFlag() }
    }

    private fun submitReturnFlag() {
        val reason = spinnerReasons.selectedItem.toString()
        val desc = etDesc.text.toString().trim()

        if (desc.isEmpty()) {
            Toast.makeText(this, "Please provide detailed explanation for the return flag", Toast.LENGTH_SHORT).show()
            return
        }

        val request = db.getAssistanceRequest(reqId)
        val customerEmail = request?.userEmail ?: "customer@peairbrakes.co.za"

        val complaintRef = "CMP-${System.currentTimeMillis() % 100000}"

        val complaint = ComplaintReturnJob(
            complaintRef = complaintRef,
            originalReqId = reqId,
            customerEmail = customerEmail,
            description = desc,
            flagReason = reason,
            status = "Open"
        )

        if (db.createComplaintReturnJob(complaint)) {
            db.updateRequestStatus(reqId, "Flagged for Return Visit ($reason)")

            val notif = AppNotification(
                userEmail = "admin@peairbrakes.co.za",
                title = "🚨 RETURN JOB / COMPLAINT FLAGGED",
                message = "Job #$reqId flagged as $reason: $desc",
                reqIdRef = reqId,
                type = "RETURN_JOB"
            )
            db.addNotification(notif)

            Toast.makeText(this, "Return Job Flag ($complaintRef) Submitted to Administrator!", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to submit return flag", Toast.LENGTH_SHORT).show()
        }
    }
}
