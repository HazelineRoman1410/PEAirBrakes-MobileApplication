package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)
        val cbIsTechnician = findViewById<CheckBox>(R.id.cb_is_technician)

        btnRegister.setOnClickListener {
            val role = if (cbIsTechnician.isChecked) "Technician" else "User"
            Toast.makeText(this, "Registered as $role", Toast.LENGTH_SHORT).show()
            
            // For now, just navigate back to Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        tvGoToLogin.setOnClickListener {
            finish() // Goes back to LoginActivity
        }
    }
}