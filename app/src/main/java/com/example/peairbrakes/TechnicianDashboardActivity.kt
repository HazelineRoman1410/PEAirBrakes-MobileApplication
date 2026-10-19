package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class TechnicianDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_technician_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tech_dashboard_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialCardView>(R.id.card_active_breakdowns).setOnClickListener {
            startActivity(Intent(this, ActiveBreakdownsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_completed_jobs).setOnClickListener {
            Toast.makeText(this, "Completed Jobs still to be implemented in the future", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.card_diesel_slip).setOnClickListener {
            startActivity(Intent(this, DieselSlipsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_log_breakdown).setOnClickListener {
            Toast.makeText(this, "Log Breakdown still to be implemented in the future", Toast.LENGTH_SHORT).show()
        }
    }
}