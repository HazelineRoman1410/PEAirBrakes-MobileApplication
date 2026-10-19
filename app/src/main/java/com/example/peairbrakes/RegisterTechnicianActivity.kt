package com.example.peairbrakes

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
        setContentView(R.layout.activity_register_technician)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_technician_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.et_reg_name)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etSpecialty = findViewById<EditText>(R.id.et_tech_specialty)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)
        val btnScanQr = findViewById<Button>(R.id.btn_scan_qr)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)

        btnScanQr.setOnClickListener {
            Toast.makeText(this, "QR Scanner will be implemented here", Toast.LENGTH_SHORT).show()
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val specialty = etSpecialty.text.toString()
            val password = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (!name.matches(Regex("^[a-zA-Z ]+$"))) {
                Toast.makeText(this, "Name must contain only letters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@(gmail\\.com|hotmail\\.com|yahoo\\.com)$"))) {
                Toast.makeText(this, "Email must contain letters, numbers, +, _, ., or - before @, and end with @gmail.com, @hotmail.com, or @yahoo.com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (!password.matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$"))) {
                Toast.makeText(this, "Password must be at least 8 characters, include uppercase, lowercase, digit, and special character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

                val user = User(
                email = email,
                password = password,
                name = name,
                role = "Technician",
                specialty = specialty
            )

            if (db.addUser(user)) {
                // Save user email to SharedPreferences
                val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("user_email", user.email)
                    apply()
                }

                Toast.makeText(this, "Technician Registered Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, TechnicianDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Registration failed or email already exists", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}