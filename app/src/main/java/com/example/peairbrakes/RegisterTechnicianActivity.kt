
package com.example.peairbrakes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import java.security.MessageDigest

class RegisterTechnicianActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_technician)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_technician_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance().getReference("technicians")
        db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.et_reg_name)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val spinnerSpecialty = findViewById<Spinner>(R.id.spinner_tech_specialty)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)
        val btnScanQr = findViewById<Button>(R.id.btn_scan_qr)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        btnScanQr.setOnClickListener {
            Toast.makeText(this, "QR Scanner will be implemented here", Toast.LENGTH_SHORT).show()
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase()
            val specialty = spinnerSpecialty.selectedItem?.toString() ?: ""
            val password = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (spinnerSpecialty.selectedItemPosition == 0) {
                Toast.makeText(this, "Please select your specialty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!name.matches(Regex("^[a-zA-Z ]+$"))) {
                Toast.makeText(this, "Name must contain only letters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@(gmail\\.com|hotmail\\.com|yahoo\\.com)$"))) {
                Toast.makeText(this, "Email must end with @gmail.com, @hotmail.com, or @yahoo.com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!password.matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$"))) {
                Toast.makeText(this, "Password must be at least 8 characters, include uppercase, lowercase, digit, and special character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false

            database.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterTechnicianActivity, "Email already exists", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val hashedPassword = hashPassword(password)
                        val id = database.push().key

                        if (id == null) {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterTechnicianActivity, "Unable to generate technician ID", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val technician = TechnicianData(
                            name = name,
                            email = email,
                            specialty = specialty,
                            password = hashedPassword,
                            role = "Technician"
                        )

                        val user = User(
                            email = email,
                            password = hashedPassword,
                            name = name,
                            role = "Technician",
                            specialty = specialty
                        )

                        database.child(id).setValue(technician).addOnCompleteListener { firebaseTask ->
                            if (firebaseTask.isSuccessful) {
                                if (db.addUser(user)) {
                                    btnRegister.isEnabled = true

                                    getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("user_email", email)
                                        .apply()

                                    etName.text.clear()
                                    etEmail.text.clear()
                                    etPassword.text.clear()

                                    Toast.makeText(this@RegisterTechnicianActivity, "Technician Registered Successfully", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@RegisterTechnicianActivity, TechnicianDashboardActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    database.child(id).removeValue()
                                    btnRegister.isEnabled = true
                                    Toast.makeText(this@RegisterTechnicianActivity, "SQLite registration failed or email already exists", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                btnRegister.isEnabled = true
                                Toast.makeText(this@RegisterTechnicianActivity, "Firebase error: ${firebaseTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        btnRegister.isEnabled = true
                        Toast.makeText(this@RegisterTechnicianActivity, "Firebase error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
