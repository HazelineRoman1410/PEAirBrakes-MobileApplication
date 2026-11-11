package com.example.peairbrakes

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

class BreakdownAssessmentActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var reqId: Int = -1

    private lateinit var tvQuoteStatus: TextView
    private lateinit var tvAdminNotes: TextView
    private lateinit var etTruckMakeModel: EditText
    private lateinit var etVin: EditText
    private lateinit var etOdometer: EditText
    private lateinit var etEngineInfo: EditText

    private lateinit var etComplaint: EditText
    private lateinit var etObservations: EditText
    private lateinit var etFaultIdentified: EditText
    private lateinit var etCause: EditText
    private lateinit var etDiagnosticResults: EditText
    private lateinit var etCategory: EditText
    private lateinit var etRecommendations: EditText

    private lateinit var etEstTechs: EditText
    private lateinit var etEstHours: EditText
    private lateinit var etLabourRate: EditText
    private lateinit var etEstPartsCost: EditText
    private lateinit var etCalloutFee: EditText
    private lateinit var etTravelExpense: EditText
    private lateinit var tvCalculatedTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_breakdown_assessment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.assessment_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        reqId = intent.getIntExtra("req_id", -1)

        tvQuoteStatus = findViewById(R.id.tv_quote_status)
        tvAdminNotes = findViewById(R.id.tv_admin_notes)
        etTruckMakeModel = findViewById(R.id.et_truck_make_model)
        etVin = findViewById(R.id.et_vin)
        etOdometer = findViewById(R.id.et_odometer)
        etEngineInfo = findViewById(R.id.et_engine_info)

        etComplaint = findViewById(R.id.et_complaint)
        etObservations = findViewById(R.id.et_observations)
        etFaultIdentified = findViewById(R.id.et_fault_identified)
        etCause = findViewById(R.id.et_cause)
        etDiagnosticResults = findViewById(R.id.et_diagnostic_results)
        etCategory = findViewById(R.id.et_category)
        etRecommendations = findViewById(R.id.et_recommendations)

        etEstTechs = findViewById(R.id.et_est_techs)
        etEstHours = findViewById(R.id.et_est_hours)
        etLabourRate = findViewById(R.id.et_labour_rate)
        etEstPartsCost = findViewById(R.id.et_est_parts_cost)
        etCalloutFee = findViewById(R.id.et_callout_fee)
        etTravelExpense = findViewById(R.id.et_travel_expense)
        tvCalculatedTotal = findViewById(R.id.tv_calculated_total)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this, reqId) }

        // Populate request details if available
        val request = db.getAssistanceRequest(reqId)
        if (request != null) {
            etTruckMakeModel.setText(request.truckReg)
            etVin.setText(request.vin)
            etComplaint.setText(request.description)
            etCategory.setText(request.specialtyRequired)
        }

        // Populate existing assessment if saved
        val existing = db.getAssessment(reqId)
        if (existing != null) {
            tvQuoteStatus.text = "Quotation Status: ${existing.quotationStatus}"
            tvAdminNotes.text = "Admin Decision: ${existing.adminDecisionNotes.ifEmpty { "Pending review" }}"

            if (existing.truckMakeModel.isNotEmpty()) etTruckMakeModel.setText(existing.truckMakeModel)
            if (existing.vin.isNotEmpty()) etVin.setText(existing.vin)
            if (existing.odometer > 0) etOdometer.setText(existing.odometer.toString())
            if (existing.engineInfo.isNotEmpty()) etEngineInfo.setText(existing.engineInfo)

            if (existing.complaint.isNotEmpty()) etComplaint.setText(existing.complaint)
            if (existing.observations.isNotEmpty()) etObservations.setText(existing.observations)
            if (existing.faultIdentified.isNotEmpty()) etFaultIdentified.setText(existing.faultIdentified)
            if (existing.cause.isNotEmpty()) etCause.setText(existing.cause)
            if (existing.diagnosticResults.isNotEmpty()) etDiagnosticResults.setText(existing.diagnosticResults)
            if (existing.category.isNotEmpty()) etCategory.setText(existing.category)
            if (existing.recommendations.isNotEmpty()) etRecommendations.setText(existing.recommendations)

            etEstTechs.setText(existing.estimatedTechs.toString())
            etEstHours.setText(existing.estimatedHours.toString())
            etLabourRate.setText(existing.labourRate.toString())
            etEstPartsCost.setText(existing.estimatedPartsCost.toString())
            etCalloutFee.setText(existing.calloutFee.toString())
            etTravelExpense.setText(existing.travelExpense.toString())

            calculateTotal()
        } else {
            etEstTechs.setText("1")
            etEstHours.setText("1.5")
            etEstPartsCost.setText("650.00")
            calculateTotal()
        }

        findViewById<Button>(R.id.btn_calc_total).setOnClickListener { calculateTotal() }
        findViewById<Button>(R.id.btn_submit_quotation).setOnClickListener { submitQuotation() }
    }

    private fun calculateTotal(): Double {
        val techs = etEstTechs.text.toString().toIntOrNull() ?: 1
        val hours = etEstHours.text.toString().toDoubleOrNull() ?: 1.0
        val rate = etLabourRate.text.toString().toDoubleOrNull() ?: 450.0
        val parts = etEstPartsCost.text.toString().toDoubleOrNull() ?: 0.0
        val callout = etCalloutFee.text.toString().toDoubleOrNull() ?: 500.0
        val travel = etTravelExpense.text.toString().toDoubleOrNull() ?: 250.0

        val totalLabour = techs * hours * rate
        val total = totalLabour + parts + callout + travel

        tvCalculatedTotal.text = String.format(Locale.US, "TOTAL ESTIMATED COST: R %.2f", total)
        return total
    }

    private fun submitQuotation() {
        val total = calculateTotal()

        val assessment = BreakdownAssessment(
            reqIdRef = reqId,
            truckMakeModel = etTruckMakeModel.text.toString(),
            vin = etVin.text.toString(),
            odometer = etOdometer.text.toString().toDoubleOrNull() ?: 0.0,
            engineInfo = etEngineInfo.text.toString(),
            complaint = etComplaint.text.toString(),
            observations = etObservations.text.toString(),
            faultIdentified = etFaultIdentified.text.toString(),
            cause = etCause.text.toString(),
            diagnosticResults = etDiagnosticResults.text.toString(),
            category = etCategory.text.toString(),
            recommendations = etRecommendations.text.toString(),
            estimatedTechs = etEstTechs.text.toString().toIntOrNull() ?: 1,
            estimatedHours = etEstHours.text.toString().toDoubleOrNull() ?: 1.0,
            labourRate = etLabourRate.text.toString().toDoubleOrNull() ?: 450.0,
            estimatedPartsCost = etEstPartsCost.text.toString().toDoubleOrNull() ?: 0.0,
            calloutFee = etCalloutFee.text.toString().toDoubleOrNull() ?: 500.0,
            travelExpense = etTravelExpense.text.toString().toDoubleOrNull() ?: 250.0,
            totalEstimatedCost = total,
            quotationStatus = "Submitted"
        )

        if (db.saveAssessment(assessment)) {
            db.updateRequestStatus(reqId, "Quotation Submitted")

            // Send notification to Admin
            val notif = AppNotification(
                userEmail = "admin@peairbrakes.co.za",
                title = "📋 New Breakdown Quotation Submitted",
                message = "Quotation of R ${String.format(Locale.US, "%.2f", total)} submitted for Job #$reqId",
                reqIdRef = reqId,
                type = "QUOTATION"
            )
            db.addNotification(notif)

            Toast.makeText(this, "Breakdown Assessment & Quotation Submitted to Admin!", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to submit assessment", Toast.LENGTH_SHORT).show()
        }
    }
}
