package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.customer_dashboard_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialCardView>(R.id.card_request_assistance).setOnClickListener {
            startActivity(Intent(this, RequestAssistanceActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_view_requests).setOnClickListener {
            Toast.makeText(this, "View Request History still to be implemented in the future", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.card_customer_settings).setOnClickListener {
            startActivity(Intent(this, CustomerSettingActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_customer_user_manual).setOnClickListener {
            startActivity(Intent(this, UserManualActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_customer_ai_assistance).setOnClickListener {
            startActivity(Intent(this, AiAssistantActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_customer_logout).setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags("en")
        )

        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("DarkMode", false)
            .apply()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
