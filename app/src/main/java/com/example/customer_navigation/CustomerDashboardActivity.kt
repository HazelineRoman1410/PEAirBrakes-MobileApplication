package com.example.customer_navigation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class CustomerDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Loads the XML layout for the customer dashboard.
        setContentView(
            R.layout.activity_customer_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.customer_dashboard_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // this Adds padding to prevent content from being obscured by system bars.
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom)
            insets
        }

        findViewById<MaterialCardView>(R.id.card_request_assistance).setOnClickListener {
            startActivity(Intent(this, RequestAssistanceActivity::class.java))
        }
        // Finds the request history card and sets a click listener.
        findViewById<MaterialCardView>(
            R.id.card_view_requests
        ).setOnClickListener {
            // Future feature: View request history
        }
    }
}