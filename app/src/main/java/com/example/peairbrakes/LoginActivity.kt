package com.example.peairbrakes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Toast.makeText(this, "Failed to get Google ID token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
                Toast.makeText(this, "Google sign-in failed (${e.statusCode})", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        mAuth = FirebaseAuth.getInstance()

        techniciansDatabase = FirebaseDatabase.getInstance()
            .getReference("technicians")

        customersDatabase = FirebaseDatabase.getInstance()
            .getReference("customers")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnGoogleLogin = findViewById<ImageView>(R.id.btn_google_login)
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

        btnGoogleLogin.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener(this) {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    val email = user?.email
                    val displayName = user?.displayName ?: "Customer"

                    if (!email.isNullOrEmpty()) {
                        signInGoogleAsCustomer(email, displayName)
                    } else {
                        Toast.makeText(this, "Google account has no email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Firebase authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInGoogleAsCustomer(email: String, googleName: String) {
        customersDatabase.orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val customer = snapshot.children.first()
                            .getValue(CustomerData::class.java)
                        saveLogin(email, customer?.name ?: googleName, "Customer")
                    } else {
                       val newCustomerId = customersDatabase.push().key ?: email.replace(".", "_")
                        val newCustomer = CustomerData(
                            name = googleName,
                            email = email,
                            phone = "",
                            companyName = "",
                            companyAddress = "",
                            password = "",
                            role = "Customer"
                        )

                        customersDatabase.child(newCustomerId).setValue(newCustomer)
                            .addOnSuccessListener {
                                saveLogin(email, googleName, "Customer")
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@LoginActivity, "Failed to create customer account", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun loginWithFirebase(email: String, hashedPassword: String, onFirebaseUnavailable: () -> Unit) {
        techniciansDatabase.orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val technician = snapshot.children.first()
                            .getValue(TechnicianData::class.java)

                        if (technician != null && technician.password == hashedPassword) {
                            saveLogin(email, technician.name ?: "", technician.role ?: "Technician")
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

    private fun checkCustomerFirebase(email: String, hashedPassword: String, onFirebaseUnavailable: () -> Unit) {
        customersDatabase.orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val customer = snapshot.children.first()
                            .getValue(CustomerData::class.java)
                        if (customer != null && customer.password == hashedPassword) {
                            saveLogin(email, customer.name ?: "", customer.role ?: "Customer")
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
            saveLogin(user.email, user.name, user.role)
        } else {
            onLoginFailed()
        }
    }
    private fun saveLogin(email: String?, name: String, role: String) {
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
        Toast.makeText(this, "Welcome back, $name ($normalizedRole)!", Toast.LENGTH_SHORT).show()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun onLoginFailed() {
        findViewById<Button>(R.id.btn_login).isEnabled = true

        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
    }
}