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

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = DatabaseHelper(this)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvGoToRegister = findViewById<TextView>(R.id.tv_go_to_register)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = db.getUser(email)
            if (user != null && user.password == password) {
                // Save user email to SharedPreferences
                val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("user_email", user.email)
                    apply()
                }

                Toast.makeText(this, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()
                
                val intent = if (user.role == "Technician") {
                    // Insert sample job for demonstration
                    val sampleReq = AssistanceRequest(
                        userEmail = "customer@example.com",
                        truckReg = "CA 123-456",
                        description = "Air leak on trailer brakes near rear axle.",
                        latitude = -33.9249, // Cape Town
                        longitude = 18.4241,
                        assignedTo = user.email
                    )
                    db.addAssistanceRequest(sampleReq)

                    Intent(this, TechnicianDashboardActivity::class.java)
                } else {
                    Intent(this, CustomerDashboardActivity::class.java)
                }
                
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
        }
    }
}