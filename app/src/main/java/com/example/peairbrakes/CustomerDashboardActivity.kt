package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    }
}