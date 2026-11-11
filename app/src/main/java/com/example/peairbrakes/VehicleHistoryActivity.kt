package com.example.peairbrakes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class VehicleHistoryActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var etSearchReg: EditText
    private lateinit var containerRecords: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vehicle_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        etSearchReg = findViewById(R.id.et_search_reg)
        containerRecords = findViewById(R.id.container_history_records)

        val initReg = intent.getStringExtra("truck_reg") ?: ""
        if (initReg.isNotEmpty()) {
            etSearchReg.setText(initReg)
            searchVehicleHistory(initReg)
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this) }

        findViewById<Button>(R.id.btn_search_history).setOnClickListener {
            val reg = etSearchReg.text.toString().trim()
            if (reg.isNotEmpty()) {
                searchVehicleHistory(reg)
            } else {
                Toast.makeText(this, "Please enter a truck registration number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchVehicleHistory(truckReg: String) {
        containerRecords.removeAllViews()
        val requests = db.getVehicleHistory(truckReg)

        if (requests.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No previous service records found for truck: $truckReg"
            tvEmpty.textSize = 14f
            tvEmpty.setTextColor(getColor(R.color.text_secondary_light))
            containerRecords.addView(tvEmpty)
            return
        }

        for (req in requests) {
            val card = MaterialCardView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            card.layoutParams = params
            card.radius = 16f
            card.cardElevation = 4f
            card.setCardBackgroundColor(getColor(R.color.surface_light))

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(24, 24, 24, 24)

            val tvTitle = TextView(this)
            tvTitle.text = "Job #${req.id} • ${req.timestamp}"
            tvTitle.textSize = 15f
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            tvTitle.setTextColor(getColor(R.color.red))

            val tvDesc = TextView(this)
            tvDesc.text = "Complaint: ${req.description}\nSpecialty: ${req.specialtyRequired}\nTechnician: ${req.assignedTo.ifEmpty { "Unassigned" }}\nStatus: ${req.status}"
            tvDesc.textSize = 14f
            tvDesc.setTextColor(getColor(R.color.text_primary_light))
            tvDesc.setPadding(0, 8, 0, 8)

            layout.addView(tvTitle)
            layout.addView(tvDesc)

            // Add assessment & job card details if available
            val assessment = db.getAssessment(req.id)
            if (assessment != null) {
                val tvAssessment = TextView(this)
                tvAssessment.text = "Assessment: ${assessment.faultIdentified} (${assessment.recommendations})\nEstimated Cost: R ${String.format(Locale.US, "%.2f", assessment.totalEstimatedCost)}"
                tvAssessment.textSize = 13f
                tvAssessment.setTextColor(getColor(R.color.text_secondary_light))
                layout.addView(tvAssessment)
            }

            val jobCard = db.getJobCard(req.id)
            if (jobCard != null && jobCard.partsUsed.isNotEmpty()) {
                val tvParts = TextView(this)
                tvParts.text = "Parts Used: ${jobCard.partsUsed}"
                tvParts.textSize = 13f
                tvParts.setTextColor(getColor(R.color.text_secondary_light))
                layout.addView(tvParts)
            }

            card.addView(layout)
            containerRecords.addView(card)
        }
    }
}
