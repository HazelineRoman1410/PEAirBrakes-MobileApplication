package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class TechnicianDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_technician_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tech_dashboard_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btn_help_header).setOnClickListener {
            HelpHelper.showHelpDialog(this, "TechnicianDashboard")
        }

        findViewById<Button>(R.id.btn_sos_header).setOnClickListener {
            SosHelper.showSosDialog(this)
        }

        findViewById<MaterialCardView>(R.id.card_active_breakdowns).setOnClickListener {
            startActivity(Intent(this, ActiveBreakdownsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_ai_assistant).setOnClickListener {
            startActivity(Intent(this, AiAssistantActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_user_manual).setOnClickListener {
            startActivity(Intent(this, UserManualActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_vehicle_history).setOnClickListener {
            startActivity(Intent(this, VehicleHistoryActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_performance).setOnClickListener {
            startActivity(Intent(this, TechnicianPerformanceActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_diesel_slip).setOnClickListener {
            startActivity(Intent(this, DieselSlipsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_technician_settings).setOnClickListener {
            startActivity(Intent(this, TechnicianSettingActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_technician_logout).setOnClickListener {
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
}
