package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RoleSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_role_selection)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.role_selection_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnCustomer = findViewById<Button>(R.id.btn_register_customer)
        val btnTechnician = findViewById<Button>(R.id.btn_register_technician)
        val tvBackToLogin = findViewById<TextView>(R.id.tv_back_to_login)

        btnCustomer.setOnClickListener {
            val intent = Intent(this, RegisterCustomerActivity::class.java)
            startActivity(intent)
        }

        btnTechnician.setOnClickListener {
            val intent = Intent(this, RegisterTechnicianActivity::class.java)
            startActivity(intent)
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}