package com.example.peairbrakes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class UserManualActivity : AppCompatActivity() {

    private lateinit var containerSections: LinearLayout
    private lateinit var etSearch: EditText
    private var currentFilter = "ALL"

    private lateinit var manualData: List<UserManualData>

    private fun loadManualData() {
        manualData = listOf(
            UserManualData(
                category = "Technician",
                title = getString(R.string.manual_tech_assignments_title),
                summary = getString(R.string.manual_tech_assignments_summary),
                fullSteps = getString(R.string.manual_tech_assignments_steps)
            ),

            UserManualData(
                category = "Technician",
                title = getString(R.string.manual_tech_safety_title),
                summary = getString(R.string.manual_tech_safety_summary),
                fullSteps = getString(R.string.manual_tech_safety_steps)
            ),

            UserManualData(
                category = "Technician",
                title = getString(R.string.manual_tech_assessment_title),
                summary = getString(R.string.manual_tech_assessment_summary),
                fullSteps = getString(R.string.manual_tech_assessment_steps)
            ),

            UserManualData(
                category = "Technician",
                title = getString(R.string.manual_tech_progress_title),
                summary = getString(R.string.manual_tech_progress_summary),
                fullSteps = getString(R.string.manual_tech_progress_steps)
            ),

            UserManualData(
                category = "Technician",
                title = getString(R.string.manual_tech_parts_title),
                summary = getString(R.string.manual_tech_parts_summary),
                fullSteps = getString(R.string.manual_tech_parts_steps)
            ),

            UserManualData(
                category = "Technician",
                title = getString(R.string.manual_tech_return_title),
                summary = getString(R.string.manual_tech_return_summary),
                fullSteps = getString(R.string.manual_tech_return_steps)
            ),

            // CUSTOMER MANUAL

            UserManualData(
                category = "Customer",
                title = getString(R.string.manual_customer_assistance_title),
                summary = getString(R.string.manual_customer_assistance_summary),
                fullSteps = getString(R.string.manual_customer_assistance_steps)
            ),

            UserManualData(
                category = "Customer",
                title = getString(R.string.manual_customer_tracking_title),
                summary = getString(R.string.manual_customer_tracking_summary),
                fullSteps = getString(R.string.manual_customer_tracking_steps)
            ),

            // FAQS

            UserManualData(
                category = "FAQ",
                title = getString(R.string.manual_faq_sos_title),
                summary = getString(R.string.manual_faq_sos_summary),
                fullSteps = getString(R.string.manual_faq_sos_steps)
            ),

            UserManualData(
                category = "FAQ",
                title = getString(R.string.manual_faq_ai_title),
                summary = getString(R.string.manual_faq_ai_summary),
                fullSteps = getString(R.string.manual_faq_ai_steps)
            ),

            UserManualData(
                category = "FAQ",
                title = getString(R.string.manual_faq_offline_title),
                summary = getString(R.string.manual_faq_offline_summary),
                fullSteps = getString(R.string.manual_faq_offline_steps)
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_manual)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manual_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        containerSections = findViewById(R.id.container_manual_sections)
        etSearch = findViewById(R.id.et_manual_search)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }


        findViewById<Button>(R.id.btn_filter_all).setOnClickListener { filterCategory("ALL") }
        findViewById<Button>(R.id.btn_filter_tech).setOnClickListener { filterCategory("Technician") }
        findViewById<Button>(R.id.btn_filter_customer).setOnClickListener { filterCategory("Customer") }
        findViewById<Button>(R.id.btn_filter_faq).setOnClickListener { filterCategory("FAQ") }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderManual(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadManualData()
        renderManual("")
    }

    private fun filterCategory(cat: String) {
        currentFilter = cat
        renderManual(etSearch.text.toString())
    }

    private fun renderManual(query: String) {
        containerSections.removeAllViews()
        val q = query.lowercase().trim()

        val filtered = manualData.filter { item ->
            (currentFilter == "ALL" || item.category == currentFilter) &&
                    (q.isEmpty() || item.title.lowercase().contains(q) || item.summary.lowercase().contains(q) || item.fullSteps.lowercase().contains(q))
        }

        if (filtered.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No manual topics match your search."
            tvEmpty.textSize = 14f
            tvEmpty.setTextColor(getColor(R.color.text_secondary_light))
            containerSections.addView(tvEmpty)
            return
        }

        for (item in filtered) {
            val card = MaterialCardView(this)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 16)
            }
            card.layoutParams = params
            card.radius = 16f
            card.cardElevation = 4f
            card.setCardBackgroundColor(getColor(R.color.surface_light))

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(24, 24, 24, 24)

            val tvTitle = TextView(this)
            tvTitle.text = item.title
            tvTitle.textSize = 16f
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            tvTitle.setTextColor(getColor(R.color.red))

            val tvSummary = TextView(this)
            tvSummary.text = item.summary
            tvSummary.textSize = 13f
            tvSummary.setTextColor(getColor(R.color.text_secondary_light))
            tvSummary.setPadding(0, 4, 0, 8)

            val tvSteps = TextView(this)
            tvSteps.text = item.fullSteps
            tvSteps.textSize = 14f
            tvSteps.setTextColor(getColor(R.color.text_primary_light))

            layout.addView(tvTitle)
            layout.addView(tvSummary)
            layout.addView(tvSteps)
            card.addView(layout)

            containerSections.addView(card)
        }
    }
}