package com.example.peairbrakesregisters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterTechnicianActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.technician_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_technician_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Link EditText fields from XML to Kotlin variables
        val etName = findViewById<EditText>(R.id.et_reg_name)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etSpecialty = findViewById<EditText>(R.id.et_tech_specialty)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)

        // Link Button and TextView from XML
        val btnScanQr = findViewById<Button>(R.id.btn_scan_qr)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)

        // Handle QR scan button click
        btnScanQr.setOnClickListener {
            // Display placeholder QR scanner message
            Toast.makeText(this, "QR Scanner will be implemented here", Toast.LENGTH_SHORT).show()
        }

        // Handle Register button click
        btnRegister.setOnClickListener {

            // Get user input from the form
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val specialty = etSpecialty.text.toString()
            val password = etPassword.text.toString()

            // Check if required fields are empty
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                // Show error message if required fields are missing
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Placeholder message for failed registration
            else {
                Toast.makeText(this, "Registration failed or email already exists", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to Login page when TextView is clicked
        tvGoToLogin.setOnClickListener {
            Toast.makeText(this, "Navigate to the Login Page will be implemented here", Toast.LENGTH_SHORT).show()
        }
    }
}