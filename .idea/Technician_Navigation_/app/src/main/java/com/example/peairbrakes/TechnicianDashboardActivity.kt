package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

/**
 * Technician dashboard navigation.
 * Contribution notes:
 * - Sets up the technician dashboard screen and handles system-bar insets.
 * - Opens the Active Breakdowns screen when its card is selected.
 * - Opens the Diesel Slips screen when its card is selected.
 * - Shows temporary Toast feedback for Completed Jobs and Log Breakdown;
 *   those two destinations are not implemented as navigation screens yet.
 */
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

        // Navigate to the list of active breakdown requests.
        findViewById<MaterialCardView>(R.id.card_active_breakdowns).setOnClickListener {
            startActivity(Intent(this, ActiveBreakdownsActivity::class.java))
        }

        // Placeholder action: completed jobs currently show a message only.
        findViewById<MaterialCardView>(R.id.card_completed_jobs).setOnClickListener {
            Toast.makeText(this, "Completed Jobs", Toast.LENGTH_SHORT).show()
        }

        // Navigate to the diesel slips screen.
        findViewById<MaterialCardView>(R.id.card_diesel_slip).setOnClickListener {
            startActivity(Intent(this, DieselSlipsActivity::class.java))
        }

        // Placeholder action: logging a breakdown is not wired to a screen yet.
        findViewById<MaterialCardView>(R.id.card_log_breakdown).setOnClickListener {
            Toast.makeText(this, "Log Breakdown", Toast.LENGTH_SHORT).show()
        }
    }
}