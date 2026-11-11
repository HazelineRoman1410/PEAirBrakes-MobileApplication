package com.example.peairbrakes

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SafetyInspectionActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var reqId: Int = -1
    private var techEmail: String = ""

    private lateinit var rbSafe: RadioButton
    private lateinit var rbUnsafe: RadioButton
    private lateinit var etAreaNotes: EditText
    private lateinit var cbPpeVest: CheckBox
    private lateinit var cbPpeGloves: CheckBox
    private lateinit var cbChocks: CheckBox
    private lateinit var cbIsolation: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_safety_inspection)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.safety_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        reqId = intent.getIntExtra("req_id", -1)

        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        techEmail = sharedPref.getString("user_email", "tech1@peairbrakes.co.za") ?: ""

        rbSafe = findViewById(R.id.rb_area_safe)
        rbUnsafe = findViewById(R.id.rb_area_unsafe)
        etAreaNotes = findViewById(R.id.et_area_notes)
        cbPpeVest = findViewById(R.id.cb_ppe_vest)
        cbPpeGloves = findViewById(R.id.cb_ppe_gloves)
        cbChocks = findViewById(R.id.cb_chocks)
        cbIsolation = findViewById(R.id.cb_isolation)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this, reqId) }

        findViewById<Button>(R.id.btn_submit_safety).setOnClickListener { submitSafetyInspection() }
    }

    private fun submitSafetyInspection() {
        val isSafe = rbSafe.isChecked
        val notes = etAreaNotes.text.toString().trim()

        if (!isSafe && notes.isEmpty()) {
            Toast.makeText(this, "Please describe the hazard notes for an unsafe area!", Toast.LENGTH_SHORT).show()
            return
        }

        val status = if (isSafe) "Passed" else "Escalated"

        val inspection = SafetyInspection(
            reqIdRef = reqId,
            techEmail = techEmail,
            areaSafe = if (isSafe) "Safe" else "Unsafe",
            areaNotes = notes,
            ppeOk = cbPpeVest.isChecked && cbPpeGloves.isChecked,
            vehicleSecured = cbChocks.isChecked,
            jobSafetyOk = cbIsolation.isChecked,
            status = status
        )

        if (db.saveSafetyInspection(inspection)) {
            if (isSafe) {
                db.updateRequestStatus(reqId, "Safety Inspection Passed")
                Toast.makeText(this, "Safety Inspection Passed! Proceed with breakdown assessment.", Toast.LENGTH_LONG).show()
            } else {
                db.updateRequestStatus(reqId, "Safety Escalated - Work Stopped")

                val notif = AppNotification(
                    userEmail = "admin@peairbrakes.co.za",
                    title = "⚠️ SAFETY HAZARD ESCALATION",
                    message = "Technician $techEmail has flagged Job #$reqId as UNSAFE: $notes",
                    reqIdRef = reqId,
                    type = "SAFETY_ESCALATION"
                )
                db.addNotification(notif)

                Toast.makeText(this, "⚠️ WORK STOPPED: Safety escalation report sent to Administrator!", Toast.LENGTH_LONG).show()
            }
            finish()
        } else {
            Toast.makeText(this, "Failed to submit safety record", Toast.LENGTH_SHORT).show()
        }
    }
}
