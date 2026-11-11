package com.example.peairbrakes

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class RepairProgressActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var reqId: Int = -1
    private var techEmail: String = ""

    private lateinit var tvStatus: TextView
    private lateinit var tvEta: TextView
    private lateinit var spinnerMilestones: Spinner
    private lateinit var etDesc: EditText
    private lateinit var etEta: EditText
    private lateinit var containerLog: LinearLayout

    private val milestones = arrayOf(
        "Repair in Progress",
        "Fault Identified",
        "Pneumatic Air Lines Flushed",
        "Brake Chambers Replaced",
        "Awaiting Spare Parts",
        "Additional Work Approval Required",
        "Repair Completed"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_repair_progress)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.progress_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        reqId = intent.getIntExtra("req_id", -1)

        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        techEmail = sharedPref.getString("user_email", "tech1@peairbrakes.co.za") ?: ""

        tvStatus = findViewById(R.id.tv_current_job_status)
        tvEta = findViewById(R.id.tv_current_eta)
        spinnerMilestones = findViewById(R.id.spinner_milestones)
        etDesc = findViewById(R.id.et_progress_desc)
        etEta = findViewById(R.id.et_update_eta)
        containerLog = findViewById(R.id.container_log)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, milestones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMilestones.adapter = adapter

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this, reqId) }

        refreshStatus()

        findViewById<Button>(R.id.btn_send_update).setOnClickListener { submitProgressUpdate() }
    }

    private fun refreshStatus() {
        val request = db.getAssistanceRequest(reqId)
        if (request != null) {
            tvStatus.text = "Current Status: ${request.status}"
            tvEta.text = "Estimated Completion ETA: ${request.eta.ifEmpty { "Not specified" }}"
        }

        // Load logs from Job Messages
        containerLog.removeAllViews()
        val messages = db.getMessagesForJob(reqId)
        for (msg in messages) {
            addLogCard(msg.senderName, msg.messageText, msg.timestamp)
        }
    }

    private fun submitProgressUpdate() {
        val milestone = spinnerMilestones.selectedItem.toString()
        val desc = etDesc.text.toString().trim()
        val newEta = etEta.text.toString().trim()

        val fullMsg = "Status: $milestone ${if (desc.isNotEmpty()) " - $desc" else ""}"

        db.updateRequestStatus(reqId, milestone, newEta)

        val user = db.getUser(techEmail)
        val techName = user?.name ?: "Technician"

        // Save progress log as job message
        val msg = JobMessage(
            reqIdRef = reqId,
            senderEmail = techEmail,
            senderName = techName,
            receiverEmail = "admin@peairbrakes.co.za",
            messageText = fullMsg
        )
        db.addMessage(msg)

        // Notify Admin and Customer
        val notif = AppNotification(
            userEmail = "admin@peairbrakes.co.za",
            title = "🔄 Job Progress Update (#$reqId)",
            message = "$techName updated status: $milestone",
            reqIdRef = reqId,
            type = "PROGRESS"
        )
        db.addNotification(notif)

        Toast.makeText(this, "Progress update recorded and synced!", Toast.LENGTH_SHORT).show()
        etDesc.setText("")
        etEta.setText("")
        refreshStatus()
    }

    private fun addLogCard(sender: String, text: String, time: String) {
        val card = MaterialCardView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 12)
        }
        card.layoutParams = params
        card.radius = 16f
        card.setCardBackgroundColor(getColor(R.color.surface_light))

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)

        val tvHeader = TextView(this)
        tvHeader.text = "$sender • $time"
        tvHeader.textSize = 12f
        tvHeader.setTextColor(getColor(R.color.red))

        val tvBody = TextView(this)
        tvBody.text = text
        tvBody.textSize = 14f
        tvBody.setTextColor(getColor(R.color.text_primary_light))
        tvBody.setPadding(0, 4, 0, 0)

        layout.addView(tvHeader)
        layout.addView(tvBody)
        card.addView(layout)

        containerLog.addView(card, 0) // newest top
    }
}
