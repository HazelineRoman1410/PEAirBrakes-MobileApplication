package com.example.peairbrakes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class AiAssistantActivity : AppCompatActivity() {

    private lateinit var containerChat: LinearLayout
    private lateinit var etPrompt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_assistance)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ai_assistant_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        containerChat = findViewById(R.id.container_chat)
        etPrompt = findViewById(R.id.et_ai_prompt)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_send_ai).setOnClickListener {
            val query = etPrompt.text.toString().trim()
            if (query.isNotEmpty()) {
                askAi(query)
                etPrompt.setText("")
            }
        }

        findViewById<Button>(R.id.chip_brakes).setOnClickListener {
            askAi(getString(R.string.ai_chip_query_brakes))
        }
        findViewById<Button>(R.id.chip_spn).setOnClickListener {
            askAi(getString(R.string.ai_chip_query_spn))
        }
        findViewById<Button>(R.id.chip_abs).setOnClickListener {
            askAi(getString(R.string.ai_chip_query_abs))
        }
    }

    private fun askAi(query: String) {
        addBubble(getString(R.string.ai_sender_technician), query, isUser = true)

        val aiAnswer = generateAiResponse(query)
        addBubble(getString(R.string.ai_sender_assistant), aiAnswer, isUser = false)
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
            q.contains("brake") || q.contains("air") || q.contains("pressure") ||
                    q.contains("rem") || q.contains("lug") || q.contains("druk") ||
                    q.contains("ziqhoboshi") || q.contains("moya") || q.contains("oxinzelelo") ||
                    q.contains("mariki") || q.contains("mowa") || q.contains("kgatelelo") ->
                getString(R.string.ai_response_brakes)

            q.contains("spn") || q.contains("injector") || q.contains("engine") ||
                    q.contains("inspuiter") || q.contains("enjin") ||
                    q.contains("injini") || q.contains("enjene") ->
                getString(R.string.ai_response_spn)

            q.contains("abs") || q.contains("sensor") || q.contains("wheel") ||
                    q.contains("wiel") || q.contains("ivili") || q.contains("leotwana") ->
                getString(R.string.ai_response_abs)

            q.contains("safety") || q.contains("roadside") ||
                    q.contains("veiligheid") || q.contains("pad") ||
                    q.contains("khuseleko") || q.contains("ndlela") ||
                    q.contains("pabalesego") || q.contains("tsela") ->
                getString(R.string.ai_response_safety)

            else ->
                getString(R.string.ai_response_general, query)
        }
    }
}
