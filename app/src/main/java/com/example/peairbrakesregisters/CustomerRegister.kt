package com.example.peairbrakesregisters

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterCustomer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.customer_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_customer_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Link EditText fields from XML to Kotlin variables
        val etName = findViewById<EditText>(R.id.et_reg_name)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etPhone = findViewById<EditText>(R.id.et_reg_phone)
        val etCompanyName = findViewById<EditText>(R.id.et_company_name)
        val etCompanyAddress = findViewById<EditText>(R.id.et_company_address)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)

        // Link Button and TextView from XML
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)

        // Register button click event
        btnRegister.setOnClickListener {

            // Get user input from all fields
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()
            val companyName = etCompanyName.text.toString()
            val companyAddress = etCompanyAddress.text.toString()
            val password = etPassword.text.toString()

            // Check if required fields are empty
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                // Show error message if required fields are missing
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                // Placeholder message for failed registration
                Toast.makeText(this, "Registration failed or email already exists", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to Login page when TextView is clicked
        tvGoToLogin.setOnClickListener {
            Toast.makeText(this, "Navigate to the Login Page will be implemented here", Toast.LENGTH_SHORT).show()
        }
    }
}