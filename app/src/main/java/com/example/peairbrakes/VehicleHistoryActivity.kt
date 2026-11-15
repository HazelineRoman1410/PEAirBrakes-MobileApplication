package com.example.peairbrakes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class VehicleHistoryActivity : AppCompatActivity() {

    private lateinit var etSearchReg: EditText
    private lateinit var btnSearch: Button
    private lateinit var containerRecords: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vehicle_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etSearchReg = findViewById(R.id.et_search_reg)
        btnSearch = findViewById(R.id.btn_search_history)
        containerRecords = findViewById(R.id.container_history_records)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        btnSearch.setOnClickListener {
            val regQuery = etSearchReg.text.toString().trim().uppercase()
            if (regQuery.isEmpty()) {
                Toast.makeText(this, "Please enter a truck registration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchTruckHistory(regQuery)
        }
    }

    private fun searchTruckHistory(regQuery: String) {
        containerRecords.removeAllViews()
        val combinedResults = mutableListOf< VehicleHistoryData>()

        val sqliteRecords = searchSqlite(regQuery)
        combinedResults.addAll(sqliteRecords)
        renderResults(combinedResults, isLoadingFirebase = true)

        val dbRef = FirebaseDatabase.getInstance().getReference("assistanceRequests")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val reg = child.child("registration").getValue(String::class.java) ?: ""

                    val normalizedDbReg = reg.replace(" ", "").uppercase()
                    val normalizedQuery = regQuery.replace(" ", "")

                    if (normalizedDbReg.contains(normalizedQuery)) {
                        val problem = child.child("problem").getValue(String::class.java) ?: "No problem description"
                        val status = child.child("status").getValue(String::class.java) ?: "Pending"
                        val assignedTo = child.child("assignedTo").getValue(String::class.java) ?: "Unassigned"
                        val userEmail = child.child("userEmail").getValue(String::class.java) ?: ""

                        val details = buildString {
                            append("Cloud (Firebase)")
                            if (assignedTo.isNotEmpty()) append("  |  Tech: $assignedTo")
                            if (userEmail.isNotEmpty()) append("  |  Client: $userEmail")
                        }

                        combinedResults.add(
                            VehicleHistoryData(
                                source = details,
                                truckReg = reg.uppercase(),
                                problem = problem,
                                status = status,
                                date = ""
                            )
                        )
                    }
                }
                renderResults(combinedResults, isLoadingFirebase = false)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@VehicleHistoryActivity,
                    "Firebase error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                renderResults(combinedResults, isLoadingFirebase = false)
            }
        })
    }

    private fun searchSqlite(regQuery: String): List<VehicleHistoryData> {
        val list = mutableListOf<VehicleHistoryData>()
        try {
            val db = openOrCreateDatabase("PeAirbrakes.db", MODE_PRIVATE, null)
            val cursor = db.rawQuery(
                "SELECT * FROM assistanceRequests " +
                        "WHERE REPLACE(UPPER(registration), ' ', '') " +
                        "LIKE ?",
                arrayOf("%${regQuery.replace(" ", "")}%")
            )

            while (cursor.moveToNext()) {
                val regIdx = cursor.getColumnIndex("registration")
                val probIdx = cursor.getColumnIndex("problem")
                val statusIdx = cursor.getColumnIndex("status")

                val reg = if (regIdx != -1) cursor.getString(regIdx) else regQuery
                val problem = if (probIdx != -1) cursor.getString(probIdx) else "No description"
                val status = if (statusIdx != -1) cursor.getString(statusIdx) else "Saved Offline"

                list.add(
                    VehicleHistoryData(
                        source = "Local (SQLite)",
                        truckReg = reg.uppercase(),
                        problem = problem,
                        status = status,
                        date = ""
                    )
                )
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {

        }
        return list
    }

    private fun renderResults(records: List<VehicleHistoryData>, isLoadingFirebase: Boolean) {
        containerRecords.removeAllViews()

        if (records.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = if (isLoadingFirebase) "Searching assistance requests..." else "No service or breakdown records found."
                textSize = 14f
                setPadding(8, 24, 8, 8)
            }
            containerRecords.addView(tvEmpty)
            return
        }

        for (item in records) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 0, 0, 16) }
                radius = 16f
                cardElevation = 4f
            }

            val innerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
            }

            val tvHeader = TextView(this).apply {
                text = "${item.truckReg}  •  ${item.status}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.red))
            }

            val tvProblem = TextView(this).apply {
                text = "Problem: ${item.problem}"
                textSize = 14f
                setPadding(0, 8, 0, 8)
            }

            val tvMeta = TextView(this).apply {
                text = item.source
                textSize = 12f
            }

            innerLayout.addView(tvHeader)
            innerLayout.addView(tvProblem)
            innerLayout.addView(tvMeta)
            card.addView(innerLayout)
            containerRecords.addView(card)
        }
    }
}
