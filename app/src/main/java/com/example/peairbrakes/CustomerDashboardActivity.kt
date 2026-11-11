package com.example.peairbrakes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class CustomerDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var customerEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.customer_dashboard_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        customerEmail = sharedPref.getString("user_email", "") ?: ""

        findViewById<Button>(R.id.btn_help_header).setOnClickListener {
            HelpHelper.showHelpDialog(this, "CustomerDashboard")
        }

        findViewById<Button>(R.id.btn_sos_header).setOnClickListener {
            SosHelper.showSosDialog(this)
        }

        findViewById<MaterialCardView>(R.id.card_request_assistance).setOnClickListener {
            startActivity(Intent(this, RequestAssistanceActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_view_requests).setOnClickListener {
            showCustomerRequestsDialog()
        }

        findViewById<MaterialCardView>(R.id.card_user_manual).setOnClickListener {
            startActivity(Intent(this, UserManualActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_customer_settings).setOnClickListener {
            startActivity(Intent(this, CustomerSettingActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_customer_logout).setOnClickListener {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("DarkMode", false).apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showCustomerRequestsDialog() {
        val requests = db.getCustomerRequests(customerEmail)
        if (requests.isEmpty()) {
            Toast.makeText(this, "No active or past breakdown requests found.", Toast.LENGTH_SHORT).show()
            return
        }

        val items = requests.map {
            "Job #${it.id} (${it.truckReg})\nStatus: ${it.status}\nTechnician: ${it.assignedTo.ifEmpty { "Pending Allocation" }}\nETA: ${it.eta.ifEmpty { "Calculating..." }}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("My Assistance Requests & Real-time ETA")
            .setItems(items) { _, idx ->
                val selected = requests[idx]
                val assessment = db.getAssessment(selected.id)
                if (assessment != null && assessment.quotationStatus == "Submitted") {
                    AlertDialog.Builder(this)
                        .setTitle("Quotation Approval Request")
                        .setMessage("Estimated Total: R ${assessment.totalEstimatedCost}\nFault: ${assessment.faultIdentified}")
                        .setPositiveButton("Approve Quote") { _, _ ->
                            assessment.quotationStatus = "Approved"
                            db.saveAssessment(assessment)
                            db.updateRequestStatus(selected.id, "Quotation Approved")
                            Toast.makeText(this, "Quotation Approved!", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Decline", null)
                        .show()
                } else {
                    Toast.makeText(this, "Job #${selected.id} Status: ${selected.status}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
