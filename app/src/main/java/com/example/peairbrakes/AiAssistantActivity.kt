package com.example.peairbrakes

import android.os.Bundle
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

class AiAssistantActivity : AppCompatActivity() {

    private lateinit var containerChat: LinearLayout
    private lateinit var etPrompt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_assistant)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ai_assistant_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        containerChat = findViewById(R.id.container_chat)
        etPrompt = findViewById(R.id.et_ai_prompt)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this) }

        findViewById<Button>(R.id.btn_send_ai).setOnClickListener {
            val query = etPrompt.text.toString().trim()
            if (query.isNotEmpty()) {
                askAi(query)
                etPrompt.setText("")
            }
        }

        findViewById<Button>(R.id.btn_send_ai).setOnClickListener {
            val query = etPrompt.text.toString().trim()
            if (query.isNotEmpty()) {
                askAi(query)
                etPrompt.setText("")
            }
        }

        findViewById<Button>(R.id.chip_brakes).setOnClickListener { askAi("How to diagnose Air Brake Pressure Loss on a heavy truck?") }
        findViewById<Button>(R.id.chip_spn).setOnClickListener { askAi("What is SPN 651 FMI 5 Injector Cylinder 1 fault and how to repair?") }
        findViewById<Button>(R.id.chip_abs).setOnClickListener { askAi("How to test Wabco ABS Modulator Valves and Wheel Speed Sensors?") }
    }

    private fun askAi(query: String) {
        addBubble("🔧 Technician:", query, isUser = true)

        val aiAnswer = generateAiResponse(query)
        addBubble("🤖 PE AI Assistant:", aiAnswer, isUser = false)
    }

    private fun addBubble(title: String, message: String, isUser: Boolean) {
        val card = MaterialCardView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16)
        }
        card.layoutParams = params
        card.radius = 24f
        card.cardElevation = 4f
        card.setCardBackgroundColor(if (isUser) getColor(R.color.surface_light) else getColor(R.color.white))

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(24, 24, 24, 24)

        val tvTitle = TextView(this)
        tvTitle.text = title
        tvTitle.textSize = 14f
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        tvTitle.setTextColor(if (isUser) getColor(R.color.text_secondary_light) else getColor(R.color.red))

        val tvMsg = TextView(this)
        tvMsg.text = message
        tvMsg.textSize = 14f
        tvMsg.setTextColor(getColor(R.color.text_primary_light))
        tvMsg.setPadding(0, 8, 0, 0)

        layout.addView(tvTitle)
        layout.addView(tvMsg)
        card.addView(layout)

        containerChat.addView(card)
    }

    private fun generateAiResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("brake") || q.contains("air") || q.contains("pressure") ->
                "📌 AIR BRAKE DIAGNOSTIC PROCEDURE:\n" +
                "1. Check Air Compressor output pressure & Governor cut-out settings (standard cut-out 120-135 PSI).\n" +
                "2. Perform leak test: Chock wheels, release spring brake, pump system to 100 PSI, observe gauge for 1 minute (max 2 PSI drop for straight truck, 3 PSI for combination).\n" +
                "3. Inspect Air Dryer purge valve & Four-Circuit Protection Valve for blockages or diaphragm rupture.\n" +
                "4. Check Foot Brake Valve (Dual Brake Valve) & Relay Valve exhaust ports for continuous leakage."

            q.contains("spn") || q.contains("injector") || q.contains("engine") ->
                "📌 DIESEL ENGINE FAULT CODE DIAGNOSIS (SPN 651 / INJECTOR CIRCUIT):\n" +
                "1. SPN 651 FMI 5 indicates Open Circuit / High Resistance on Injector #1 Solenoid.\n" +
                "2. Check valve cover pass-through harness connector for oil contamination or loose pins.\n" +
                "3. Measure Injector Solenoid Resistance: Expected range is 0.2 to 1.5 Ohms. Resistance above 2.0 Ohms indicates failed injector coil.\n" +
                "4. Check ECM harness continuity between ECM Pin 15 & Injector Pin A."

            q.contains("abs") || q.contains("sensor") || q.contains("wheel") ->
                "📌 WABCO ABS MODULATOR & SENSOR DIAGNOSTIC:\n" +
                "1. Check Wheel Speed Sensor Air Gap: Push sensor in until it touches tone wheel; wheel rotation self-adjusts gap.\n" +
                "2. Resistance test: WABCO passive sensor resistance must be 900 - 2000 Ohms.\n" +
                "3. Test Modulator Valve Solenoids: Resistance across Inlet/Outlet pins should be 12-18 Ohms.\n" +
                "4. Verify 24V supply to ABS ECU."

            q.contains("safety") || q.contains("roadside") ->
                "📌 ROADSIDE SAFETY DIRECTIVE:\n" +
                "1. Park vehicle on stable shoulder. Turn on hazard lights.\n" +
                "2. Place Warning Triangles at 45m, 90m, and 150m behind breakdown.\n" +
                "3. High-visibility reflective vest and safety boots are MANDATORY.\n" +
                "4. Chock truck drive wheels before going beneath vehicle."

            else ->
                "📌 GENERAL TRUCK TECHNICAL ASSISTANCE:\n" +
                "For query: \"$query\"\n" +
                "1. Inspect primary mechanical/electrical connections.\n" +
                "2. Perform digital OBD scan using heavy-duty diagnostic scanner.\n" +
                "3. Refer to OEM Service Manual & PE Airbrakes Safety Regulations.\n" +
                "4. If hazard exists, submit Safety Escalation to Dispatch Admin immediately."
        }
    }
}
