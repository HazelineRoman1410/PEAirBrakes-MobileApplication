package com.example.peairbrakes

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TechnicianPerformanceActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var techEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_technician_performance)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tech_perf_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        techEmail = sharedPref.getString("user_email", "tech1@peairbrakes.co.za") ?: ""

        val tech = db.getUser(techEmail)

        val tvName = findViewById<TextView>(R.id.tv_tech_name)
        val tvSpecialty = findViewById<TextView>(R.id.tv_tech_specialty)
        val tvQuals = findViewById<TextView>(R.id.tv_tech_qualifications)

        if (tech != null) {
            tvName.text = tech.name
            tvSpecialty.text = "Specialty: ${tech.specialty}"
            tvQuals.text = "Qualifications: ${tech.qualifications.ifEmpty { "Certified Heavy Duty Technician" }}"
        }

        val allAssigned = db.getAssignedJobs(techEmail)
        val completed = allAssigned.count { it.status == "Completed" || it.status == "Verified and Ready for Invoicing" || it.status == "Closed" }
        val total = allAssigned.size

        findViewById<TextView>(R.id.tv_stat_assigned).text = total.toString()
        findViewById<TextView>(R.id.tv_stat_completed).text = completed.toString()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this) }
    }
}
