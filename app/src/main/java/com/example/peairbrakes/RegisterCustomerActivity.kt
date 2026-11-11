
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*

import java.security.MessageDigest

class RegisterCustomerActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_customer)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_customer_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance().getReference("customers")
        db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.et_reg_name)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etPhone = findViewById<EditText>(R.id.et_reg_phone)
        val etCompanyName = findViewById<EditText>(R.id.et_company_name)
        val etCompanyAddress = findViewById<EditText>(R.id.et_company_address)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase()
            val phone = etPhone.text.toString().trim()
            val companyName = etCompanyName.text.toString().trim()
            val companyAddress = etCompanyAddress.text.toString().trim()
            val password = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!name.matches(Regex("^[a-zA-Z ]+$"))) {
                Toast.makeText(this, "Name must contain only letters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@(gmail\\.com|hotmail\\.com|yahoo\\.com)$"))) {
                Toast.makeText(this, "Email must contain letters, numbers, +, _, ., or - before @, and end with @gmail.com, @hotmail.com, or @yahoo.com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!password.matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$"))) {
                Toast.makeText(this, "Password must be at least 8 characters, include uppercase, lowercase, digit, and special character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (!phone.matches(Regex("^[0-9]+$"))) {
                Toast.makeText(this, "Phone number must contain only numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false

            database.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterCustomerActivity, "Email already exists", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val hashedPassword = hashPassword(password)
                        val id = database.push().key

                        if (id == null) {
                            btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterCustomerActivity, "Unable to generate customer ID", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val customer = CustomerData(
                            name = name,
                            email = email,
                            phone = phone,
                            companyName = companyName,
                            companyAddress = companyAddress,
                            password = hashedPassword,
                            role = "Customer"
                        )

                        val user = User(
                            email = email,
                            password = hashedPassword,
                            name = name,
                            role = "Customer",
                            phone = phone,
                            companyName = companyName,
                            companyAddress = companyAddress
                        )

                        database.child(id).setValue(customer).addOnCompleteListener { firebaseTask ->
                            if (firebaseTask.isSuccessful) {
                                if (db.addUser(user)) {
                                    btnRegister.isEnabled = true

                                    getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("user_email", email)
                                        .apply()

                                    etName.text.clear()
                                    etEmail.text.clear()
                                    etPhone.text.clear()
                                    etCompanyName.text.clear()
                                    etCompanyAddress.text.clear()
                                    etPassword.text.clear()

                                    Toast.makeText(this@RegisterCustomerActivity, "Customer Registered Successfully", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@RegisterCustomerActivity, CustomerDashboardActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    database.child(id).removeValue()
                                    btnRegister.isEnabled = true
                                    Toast.makeText(this@RegisterCustomerActivity, "SQLite registration failed or email already exists", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                btnRegister.isEnabled = true
                                Toast.makeText(this@RegisterCustomerActivity, "Firebase error: ${firebaseTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        btnRegister.isEnabled = true
                        Toast.makeText(this@RegisterCustomerActivity, "Firebase error: ${error.message}", Toast.LENGTH_LONG).show()
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
