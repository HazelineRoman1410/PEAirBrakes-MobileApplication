
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var techniciansDatabase: DatabaseReference
    private lateinit var customersDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)

        techniciansDatabase = FirebaseDatabase.getInstance()
            .getReference("technicians")

        customersDatabase = FirebaseDatabase.getInstance()
            .getReference("customers")

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvGoToRegister = findViewById<TextView>(R.id.tv_go_to_register)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            val hashedPassword = hashPassword(password)

            loginWithFirebase(email, hashedPassword) {
                loginWithSQLite(email, hashedPassword)
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun loginWithFirebase(
        email: String,
        hashedPassword: String,
        onFirebaseUnavailable: () -> Unit
    ) {
        techniciansDatabase.orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val technician = snapshot.children.first()
                            .getValue(TechnicianData::class.java)

                        if (technician != null && technician.password == hashedPassword) {
                            saveLogin(
                                email,
                                technician.name ?: "",
                                technician.role ?: "Technician"
                            )
                        } else {
                            onLoginFailed()
                        }
                    } else {
                        checkCustomerFirebase(email, hashedPassword, onFirebaseUnavailable)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFirebaseUnavailable()
                }
            })
    }

    private fun checkCustomerFirebase(
        email: String,
        hashedPassword: String,
        onFirebaseUnavailable: () -> Unit
    ) {
        customersDatabase.orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val customer = snapshot.children.first()
                            .getValue(CustomerData::class.java)

                        if (customer != null && customer.password == hashedPassword) {
                            saveLogin(
                                email,
                                customer.name ?: "",
                                customer.role ?: "Customer"
                            )
                        } else {
                            onLoginFailed()
                        }
                    } else {
                        onFirebaseUnavailable()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFirebaseUnavailable()
                }
            })
    }

    private fun loginWithSQLite(email: String, hashedPassword: String) {
        val user = db.getUser(email)

        if (user != null && user.password == hashedPassword) {
            saveLogin(
                user.email,
                user.name,
                user.role
            )
        } else {
            onLoginFailed()
        }
    }

    private fun saveLogin(
        email: String?,
        name: String,
        role: String
    ) {
        val normalizedRole = role.trim()

        val intent = when {
            normalizedRole.equals("Technician", ignoreCase = true) ->
                Intent(this, TechnicianDashboardActivity::class.java)

            normalizedRole.equals("Customer", ignoreCase = true) ->
                Intent(this, CustomerDashboardActivity::class.java)

            else -> {
                onLoginFailed()
                return
            }
        }

        val sharedPref = getSharedPreferences(
            "PeAirbrakesPrefs",
            Context.MODE_PRIVATE
        )

        sharedPref.edit().apply {
            putString("user_email", email)
            putString("user_role", normalizedRole)
            apply()
        }

        Toast.makeText(
            this,
            "Welcome back, $name!",
            Toast.LENGTH_SHORT
        ).show()

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun onLoginFailed() {
        findViewById<Button>(R.id.btn_login).isEnabled = true

        Toast.makeText(
            this,
            "Invalid email or password",
            Toast.LENGTH_SHORT
        ).show()
    }
}
