package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var containerRequests: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_dashboard_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        containerRequests = findViewById(R.id.container_admin_requests)

        findViewById<Button>(R.id.btn_help_header).setOnClickListener {
            HelpHelper.showHelpDialog(this, "AdminDashboard")
        }

        findViewById<MaterialCardView>(R.id.card_live_map).setOnClickListener {
            startActivity(Intent(this, AdminLiveMapActivity::class.java))
        }

        findViewById<Button>(R.id.btn_admin_logout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshRequests()
    }

    private fun refreshRequests() {
        containerRequests.removeAllViews()
        val requests = db.getAllRequests()

        if (requests.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No breakdown requests logged in system."
            tvEmpty.textSize = 14f
            tvEmpty.setTextColor(getColor(R.color.text_secondary_light))
            containerRequests.addView(tvEmpty)
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
            tvTitle.text = "Job #${req.id} • Truck ${req.truckReg} (${req.urgency})"
            tvTitle.textSize = 16f
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            tvTitle.setTextColor(getColor(R.color.red))

            val tvDetails = TextView(this)
            tvDetails.text = "Description: ${req.description}\nRequired Specialty: ${req.specialtyRequired}\nAssigned Tech: ${req.assignedTo.ifEmpty { "Unassigned" }}\nStatus: ${req.status}"
            tvDetails.textSize = 13f
            tvDetails.setTextColor(getColor(R.color.text_primary_light))
            tvDetails.setPadding(0, 8, 0, 8)

            layout.addView(tvTitle)
            layout.addView(tvDetails)

            // Button Action Row
            val rowActions = LinearLayout(this)
            rowActions.orientation = LinearLayout.HORIZONTAL
            rowActions.setPadding(0, 8, 0, 0)

            // 1. Auto Allocate / Re-allocate Button
            val btnAllocate = Button(this, null, android.R.attr.borderlessButtonStyle)
            btnAllocate.text = "🤖 Auto-Allocate"
            btnAllocate.setTextColor(getColor(R.color.red))
            btnAllocate.setOnClickListener { runIntelligentAllocation(req) }
            rowActions.addView(btnAllocate)

            // 2. Manual Override Button
            val btnOverride = Button(this, null, android.R.attr.borderlessButtonStyle)
            btnOverride.text = "✍️ Override Tech"
            btnOverride.setTextColor(getColor(R.color.red))
            btnOverride.setOnClickListener { showManualOverrideDialog(req) }
            rowActions.addView(btnOverride)

            // 3. Review Quotation Button (if submitted)
            val assessment = db.getAssessment(req.id)
            if (assessment != null && assessment.quotationStatus == "Submitted") {
                val btnQuote = Button(this, null, android.R.attr.borderlessButtonStyle)
                btnQuote.text = "📋 Review Quote"
                btnQuote.setTextColor(getColor(R.color.red))
                btnQuote.setOnClickListener { reviewQuotationDialog(req, assessment) }
                rowActions.addView(btnQuote)
            }

            // 4. Additional Work Request Button (if pending)
            val addWork = db.getAdditionalWorkRequests(req.id).firstOrNull { it.status == "Pending" }
            if (addWork != null) {
                val btnAddWork = Button(this, null, android.R.attr.borderlessButtonStyle)
                btnAddWork.text = "💵 Approve Extra Work"
                btnAddWork.setTextColor(getColor(R.color.red))
                btnAddWork.setOnClickListener { reviewAdditionalWorkDialog(req, addWork) }
                rowActions.addView(btnAddWork)
            }

            // 5. Job Verification & Invoicing Button (if completed)
            if (req.status == "Completed" || req.status == "Submitted for Administrator Verification") {
                val btnVerify = Button(this, null, android.R.attr.borderlessButtonStyle)
                btnVerify.text = "✅ Verify & Invoice"
                btnVerify.setTextColor(getColor(R.color.red))
                btnVerify.setOnClickListener { verifyAndSendToInvoicing(req) }
                rowActions.addView(btnVerify)
            }

            layout.addView(rowActions)
            card.addView(layout)
            containerRequests.addView(card)
        }
    }

    private fun runIntelligentAllocation(req: AssistanceRequest) {
        val ranked = AllocationEngine.rankTechnicians(db, req)

        if (ranked.isEmpty()) {
            Toast.makeText(this, "No available technicians found for allocation!", Toast.LENGTH_SHORT).show()
            return
        }

        val topTech = ranked.first()
        val techListNames = ranked.map { "${it.technician.name} (${it.suitabilityReason})" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Intelligent Allocation Engine")
            .setMessage("Top Recommended Technician:\n\n${topTech.technician.name}\n${topTech.suitabilityReason}\n\nDo you want to allocate this job?")
            .setPositiveButton("Assign Top Ranked Tech") { _, _ ->
                db.updateRequestAssignment(req.id, topTech.technician.email, isManual = false)
                Toast.makeText(this, "Job #${req.id} automatically allocated to ${topTech.technician.name}!", Toast.LENGTH_LONG).show()
                refreshRequests()
            }
            .setNeutralButton("View All Ranked") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Ranked Technician Candidates")
                    .setItems(techListNames) { _, idx ->
                        val selected = ranked[idx]
                        db.updateRequestAssignment(req.id, selected.technician.email, isManual = false)
                        Toast.makeText(this, "Job #${req.id} allocated to ${selected.technician.name}!", Toast.LENGTH_SHORT).show()
                        refreshRequests()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showManualOverrideDialog(req: AssistanceRequest) {
        val techs = db.getAllTechnicians()
        val techNames = techs.map { "${it.name} (${it.specialty})" }.toTypedArray()

        val etReason = EditText(this)
        etReason.hint = "Enter manual override reason (Mandatory)..."

        AlertDialog.Builder(this)
            .setTitle("Manual Technician Allocation Override")
            .setMessage("Select technician and provide override reason:")
            .setView(etReason)
            .setSingleChoiceItems(techNames, -1) { dialog, which ->
                val selectedTech = techs[which]
                val reason = etReason.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Please enter override reason!", Toast.LENGTH_SHORT).show()
                    return@setSingleChoiceItems
                }
                db.updateRequestAssignment(req.id, selectedTech.email, isManual = true, overrideReason = reason)
                Toast.makeText(this, "Manual Override: Job assigned to ${selectedTech.name}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                refreshRequests()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reviewQuotationDialog(req: AssistanceRequest, assessment: BreakdownAssessment) {
        AlertDialog.Builder(this)
            .setTitle("Review Breakdown Quotation #${req.id}")
            .setMessage("Fault: ${assessment.faultIdentified}\nRecommendation: ${assessment.recommendations}\nEstimated Total: R ${String.format(Locale.US, "%.2f", assessment.totalEstimatedCost)}")
            .setPositiveButton("Approve Quotation") { _, _ ->
                assessment.quotationStatus = "Approved"
                assessment.adminDecisionNotes = "Approved by Dispatch Admin"
                assessment.adminDecidedBy = "admin@peairbrakes.co.za"
                db.saveAssessment(assessment)
                db.updateRequestStatus(req.id, "Quotation Approved")

                val notif = AppNotification(
                    userEmail = req.assignedTo,
                    title = "✅ Quotation Approved",
                    message = "Admin approved quotation for Job #${req.id}. Authorised Total: R ${String.format(Locale.US, "%.2f", assessment.totalEstimatedCost)}",
                    reqIdRef = req.id,
                    type = "QUOTATION_APPROVED"
                )
                db.addNotification(notif)

                Toast.makeText(this, "Quotation Approved!", Toast.LENGTH_SHORT).show()
                refreshRequests()
            }
            .setNegativeButton("Reject Quotation") { _, _ ->
                assessment.quotationStatus = "Rejected"
                assessment.adminDecisionNotes = "Rejected by Dispatch Admin - Revision required"
                db.saveAssessment(assessment)
                db.updateRequestStatus(req.id, "Quotation Rejected")
                Toast.makeText(this, "Quotation Rejected", Toast.LENGTH_SHORT).show()
                refreshRequests()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun reviewAdditionalWorkDialog(req: AssistanceRequest, addWork: AdditionalWorkRequest) {
        AlertDialog.Builder(this)
            .setTitle("Approve Additional Cost Request")
            .setMessage("Technician: ${addWork.techEmail}\nWork Description: ${addWork.description}\nAdditional Cost: R ${String.format(Locale.US, "%.2f", addWork.additionalCost)}")
            .setPositiveButton("Approve Extra Work") { _, _ ->
                db.updateAdditionalWorkStatus(addWork.id, "Approved")
                db.updateRequestStatus(req.id, "Additional Work Approved")

                val notif = AppNotification(
                    userEmail = addWork.techEmail,
                    title = "✅ Additional Work Approved",
                    message = "Admin approved extra work of R ${String.format(Locale.US, "%.2f", addWork.additionalCost)} for Job #${req.id}",
                    reqIdRef = req.id,
                    type = "ADDITIONAL_APPROVED"
                )
                db.addNotification(notif)

                Toast.makeText(this, "Additional Work Cost Approved!", Toast.LENGTH_SHORT).show()
                refreshRequests()
            }
            .setNegativeButton("Reject") { _, _ ->
                db.updateAdditionalWorkStatus(addWork.id, "Rejected")
                Toast.makeText(this, "Additional Work Request Rejected", Toast.LENGTH_SHORT).show()
                refreshRequests()
            }
            .show()
    }

    private fun verifyAndSendToInvoicing(req: AssistanceRequest) {
        val card = db.getJobCard(req.id) ?: JobCard(reqId = req.id)
        card.verifiedByAdmin = true
        card.invoiced = true
        card.status = "Verified and Ready for Invoicing"
        db.updateJobCard(card)

        db.updateRequestStatus(req.id, "Verified and Ready for Invoicing")

        Toast.makeText(this, "Job #${req.id} verified and transmitted to Invoicing System!", Toast.LENGTH_LONG).show()
        refreshRequests()
    }
}
