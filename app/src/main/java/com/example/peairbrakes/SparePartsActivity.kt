package com.example.peairbrakes

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class SparePartsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var reqId: Int = -1
    private var techEmail: String = ""

    private lateinit var etPartName: EditText
    private lateinit var etPartNo: EditText
    private lateinit var etQty: EditText
    private lateinit var tvPartsSummary: TextView

    private lateinit var etAddWorkDesc: EditText
    private lateinit var etAddCost: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_spare_parts)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.parts_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        reqId = intent.getIntExtra("req_id", -1)

        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        techEmail = sharedPref.getString("user_email", "tech1@peairbrakes.co.za") ?: ""

        etPartName = findViewById(R.id.et_part_name)
        etPartNo = findViewById(R.id.et_part_no)
        etQty = findViewById(R.id.et_qty)
        tvPartsSummary = findViewById(R.id.tv_parts_summary)

        etAddWorkDesc = findViewById(R.id.et_add_work_desc)
        etAddCost = findViewById(R.id.et_add_cost)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this, reqId) }

        refreshPartsSummary()

        findViewById<Button>(R.id.btn_add_part).setOnClickListener { addPartToJobCard() }
        findViewById<Button>(R.id.btn_request_add_work).setOnClickListener { submitAdditionalWorkRequest() }
    }

    private fun refreshPartsSummary() {
        val card = db.getJobCard(reqId)
        if (card != null && card.partsUsed.isNotEmpty()) {
            tvPartsSummary.text = "Parts Recorded on Job Card:\n${card.partsUsed}"
        } else {
            tvPartsSummary.text = "Parts Added: None"
        }
    }

    private fun addPartToJobCard() {
        val name = etPartName.text.toString().trim()
        val partNo = etPartNo.text.toString().trim()
        val qty = etQty.text.toString().toIntOrNull() ?: 1

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter part name", Toast.LENGTH_SHORT).show()
            return
        }

        val jobCard = db.getJobCard(reqId) ?: JobCard(reqId = reqId)
        val newEntry = "$name (PN: ${partNo.ifEmpty { "N/A" }}, Qty: $qty)"
        jobCard.partsUsed = if (jobCard.partsUsed.isEmpty()) newEntry else "${jobCard.partsUsed}\n$newEntry"

        if (db.updateJobCard(jobCard)) {
            Toast.makeText(this, "Part added to Job Card", Toast.LENGTH_SHORT).show()
            etPartName.setText("")
            etPartNo.setText("")
            etQty.setText("1")
            refreshPartsSummary()
        }
    }

    private fun submitAdditionalWorkRequest() {
        val desc = etAddWorkDesc.text.toString().trim()
        val cost = etAddCost.text.toString().toDoubleOrNull() ?: 0.0

        if (desc.isEmpty() || cost <= 0.0) {
            Toast.makeText(this, "Please describe additional work and valid cost", Toast.LENGTH_SHORT).show()
            return
        }

        if (db.addAdditionalWorkRequest(reqId, techEmail, desc, cost)) {
            db.updateRequestStatus(reqId, "Additional Approval Required")

            val notif = AppNotification(
                userEmail = "admin@peairbrakes.co.za",
                title = "💵 Additional Work Cost Approval Request",
                message = "Technician $techEmail requests approval for R ${String.format(Locale.US, "%.2f", cost)} on Job #$reqId: $desc",
                reqIdRef = reqId,
                type = "ADDITIONAL_WORK"
            )
            db.addNotification(notif)

            Toast.makeText(this, "Additional Work Request submitted to Administrator for approval!", Toast.LENGTH_LONG).show()
            etAddWorkDesc.setText("")
            etAddCost.setText("")
            finish()
        } else {
            Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show()
        }
    }
}
