package com.example.peairbrakes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

data class ManualSection(
    val category: String, // "Technician", "Customer", "Admin", "FAQ"
    val title: String,
    val summary: String,
    val fullSteps: String
)

class UserManualActivity : AppCompatActivity() {

    private lateinit var containerSections: LinearLayout
    private lateinit var etSearch: EditText
    private var currentFilter = "ALL"

    private val manualData = listOf(
        ManualSection(
            category = "Technician",
            title = "🛠️ Accepting & Managing Breakdown Assignments",
            summary = "How to accept/reject breakdown jobs assigned by Dispatch.",
            fullSteps = "STEP 1: Open your Technician Dashboard and tap 'Active Breakdowns'.\n" +
                    "STEP 2: Review assigned breakdown description, truck reg, and location.\n" +
                    "STEP 3: Tap 'Accept Job' (which sets your status to En Route) or 'Reject Job' (providing a reason for Dispatch re-allocation).\n" +
                    "STEP 4: Tap '🗺️ Map Nav' to launch turn-by-turn navigation to the breakdown site."
        ),
        ManualSection(
            category = "Technician",
            title = "🛡️ Roadside Safety Inspection & Escalation",
            summary = "Mandatory area safety check before beginning repair work.",
            fullSteps = "STEP 1: Upon arrival at the breakdown, tap '🛡️ Safety Inspection'.\n" +
                    "STEP 2: Assess roadside traffic, warning triangles placement, and ground stability.\n" +
                    "STEP 3: Check PPE (vest & boots) and vehicle wheel chocks/isolation.\n" +
                    "STEP 4: If safe, submit to proceed. If UNSAFE, toggle Unsafe to trigger immediate Safety Escalation to Dispatch."
        ),
        ManualSection(
            category = "Technician",
            title = "📋 Breakdown Assessment & Quotation Submission",
            summary = "Diagnose faults, estimate labor & parts, and submit quotation for Admin approval.",
            fullSteps = "STEP 1: Tap '📋 Assessment & Quote' on the job card.\n" +
                    "STEP 2: Fill in truck details (Make/Model, VIN, Odometer, Engine specs).\n" +
                    "STEP 3: Record identified fault, cause, and diagnostic results.\n" +
                    "STEP 4: Enter estimated labor hours, parts cost, and travel expenses.\n" +
                    "STEP 5: Tap 'Calculate Total Quote' and 'Submit Assessment & Quotation'. Wait for Admin approval notification before starting work."
        ),
        ManualSection(
            category = "Technician",
            title = "🔄 Repair Progress Tracking & ETA Updates",
            summary = "Publish milestone updates and updated repair ETAs.",
            fullSteps = "STEP 1: Tap '🔄 Progress Log' on the active job card.\n" +
                    "STEP 2: Select progress milestone (e.g. Pneumatic Air Lines Flushed, Brake Chambers Replaced).\n" +
                    "STEP 3: Enter progress description and revised completion ETA.\n" +
                    "STEP 4: Tap 'Publish Progress Update' to notify Dispatch and Customer."
        ),
        ManualSection(
            category = "Technician",
            title = "⚙️ Spare Parts & Additional Work Approval",
            summary = "Record parts used and request authorization for extra costs.",
            fullSteps = "STEP 1: Tap '⚙️ Parts & Approval'.\n" +
                    "STEP 2: Enter part name, OEM part number, and quantity used on job card.\n" +
                    "STEP 3: If additional out-of-scope work is discovered, fill out the Additional Work Request form and submit to Admin for approval."
        ),
        ManualSection(
            category = "Technician",
            title = "🚩 Flagging Return Jobs & Customer Complaints",
            summary = "Flag unsuccessful repairs or complaints for return visits.",
            fullSteps = "STEP 1: If a repair cannot be completed or is unsuccessful, tap '🚩 Flag Return'.\n" +
                    "STEP 2: Select flag reason (Repair Unsuccessful, Specialist Required, Parts Unavailable, etc.).\n" +
                    "STEP 3: Provide detailed explanatory notes and submit to Dispatch."
        ),
        ManualSection(
            category = "Customer",
            title = "🚚 Requesting Roadside Assistance",
            summary = "How to log a breakdown request and attach breakdown location.",
            fullSteps = "STEP 1: Open Customer Dashboard and tap 'Request Roadside Assistance'.\n" +
                    "STEP 2: Enter truck registration number and describe the breakdown problem.\n" +
                    "STEP 3: Tap 'Take Photo of Problem' to capture evidence.\n" +
                    "STEP 4: Tap 'Attach My Pin Location' to use GPS or pick location on map.\n" +
                    "STEP 5: Tap 'Submit Assistance Request'."
        ),
        ManualSection(
            category = "Customer",
            title = "⏱️ Real-time Technician Allocation & ETA Tracking",
            summary = "Track allocated technician arrival and approve repair quotes.",
            fullSteps = "STEP 1: Open Customer Dashboard and tap 'My Requests History'.\n" +
                    "STEP 2: View assigned technician name, contact, status, and live arrival ETA.\n" +
                    "STEP 3: When a quotation is submitted by the technician, tap your request to review and approve the quote."
        ),
        ManualSection(
            category = "Admin",
            title = "🏢 Dispatching & Intelligent Technician Allocation",
            summary = "Using the AI Allocation Engine or manual overrides.",
            fullSteps = "STEP 1: Log into Dispatch Admin Dashboard (`admin@peairbrakes.co.za`).\n" +
                    "STEP 2: View incoming breakdown requests.\n" +
                    "STEP 3: Tap '🤖 Auto-Allocate' to run the 10-factor ranking engine (Specialty, qualifications, experience, availability, workload, distance).\n" +
                    "STEP 4: Or tap '✍️ Override Tech' to select a technician manually with reason logging."
        ),
        ManualSection(
            category = "Admin",
            title = "🗺️ Live Operations Map & Tracking",
            summary = "Monitor technician locations and active breakdown sites.",
            fullSteps = "STEP 1: Tap '🗺️ Live Operations Map' on the Admin Dashboard.\n" +
                    "STEP 2: View real-time technician markers, availability statuses, and breakdown job sites across the region."
        ),
        ManualSection(
            category = "Admin",
            title = "📋 Quotation Approval & Invoicing Verification",
            summary = "Approve repair quotes and transmit completed job cards.",
            fullSteps = "STEP 1: Tap '📋 Review Quote' on submitted breakdown requests to approve/reject quotations.\n" +
                    "STEP 2: Tap '💵 Approve Extra Work' for additional cost requests.\n" +
                    "STEP 3: Tap '✅ Verify & Invoice' on completed job cards to transmit data to invoicing system."
        ),
        ManualSection(
            category = "FAQ",
            title = "🚨 Emergency SOS Feature",
            summary = "How to trigger an immediate emergency alert.",
            fullSteps = "Tap the 🚨 SOS button located at the top right of any screen. Confirm the alert to notify Dispatch and nearby technicians immediately."
        ),
        ManualSection(
            category = "FAQ",
            title = "🤖 AI Technician Assistant",
            summary = "Using AI for technical truck troubleshooting.",
            fullSteps = "Tap 'AI Assistant' on the Technician Dashboard. Ask questions about pneumatic air brake pressure loss, Cummins/Volvo SPN fault codes, or WABCO ABS testing."
        ),
        ManualSection(
            category = "FAQ",
            title = "📶 Offline Mode & Sync",
            summary = "Working in remote areas without mobile coverage.",
            fullSteps = "All safety inspections, job cards, assessments, and progress updates are saved locally in SQLite when offline. Records automatically sync to Dispatch once connectivity returns."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_manual)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manual_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        containerSections = findViewById(R.id.container_manual_sections)
        etSearch = findViewById(R.id.et_manual_search)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this) }
        findViewById<Button>(R.id.btn_contact_support).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("📞 PE Airbrakes Dispatch Help Desk")
                .setMessage("Dispatch Center Phone: +27 41 555 0100\nEmergency Line: +27 800 911 911\nEmail: support@peairbrakes.co.za")
                .setPositiveButton("OK", null)
                .show()
        }

        findViewById<Button>(R.id.btn_filter_all).setOnClickListener { filterCategory("ALL") }
        findViewById<Button>(R.id.btn_filter_tech).setOnClickListener { filterCategory("Technician") }
        findViewById<Button>(R.id.btn_filter_customer).setOnClickListener { filterCategory("Customer") }
        findViewById<Button>(R.id.btn_filter_admin).setOnClickListener { filterCategory("Admin") }
        findViewById<Button>(R.id.btn_filter_faq).setOnClickListener { filterCategory("FAQ") }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderManual(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        renderManual("")
    }

    private fun filterCategory(cat: String) {
        currentFilter = cat
        renderManual(etSearch.text.toString())
    }

    private fun renderManual(query: String) {
        containerSections.removeAllViews()
        val q = query.lowercase().trim()

        val filtered = manualData.filter { item ->
            (currentFilter == "ALL" || item.category == currentFilter) &&
            (q.isEmpty() || item.title.lowercase().contains(q) || item.summary.lowercase().contains(q) || item.fullSteps.lowercase().contains(q))
        }

        if (filtered.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No manual topics match your search."
            tvEmpty.textSize = 14f
            tvEmpty.setTextColor(getColor(R.color.text_secondary_light))
            containerSections.addView(tvEmpty)
            return
        }

        for (item in filtered) {
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
            tvTitle.text = item.title
            tvTitle.textSize = 16f
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            tvTitle.setTextColor(getColor(R.color.red))

            val tvSummary = TextView(this)
            tvSummary.text = item.summary
            tvSummary.textSize = 13f
            tvSummary.setTextColor(getColor(R.color.text_secondary_light))
            tvSummary.setPadding(0, 4, 0, 8)

            val tvSteps = TextView(this)
            tvSteps.text = item.fullSteps
            tvSteps.textSize = 14f
            tvSteps.setTextColor(getColor(R.color.text_primary_light))

            layout.addView(tvTitle)
            layout.addView(tvSummary)
            layout.addView(tvSteps)
            card.addView(layout)

            containerSections.addView(card)
        }
    }
}
