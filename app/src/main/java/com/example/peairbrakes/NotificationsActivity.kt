package com.example.peairbrakes

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class NotificationsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userEmail: String = ""
    private lateinit var containerNotifs: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notifications_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "") ?: ""

        containerNotifs = findViewById(R.id.container_notifications)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this) }

        refreshNotifications()
    }

    private fun refreshNotifications() {
        containerNotifs.removeAllViews()
        val notifs = db.getNotifications(userEmail)

        if (notifs.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No notifications yet."
            tvEmpty.textSize = 14f
            tvEmpty.setTextColor(getColor(R.color.text_secondary_light))
            containerNotifs.addView(tvEmpty)
            return
        }

        for (n in notifs) {
            val card = MaterialCardView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            card.layoutParams = params
            card.radius = 16f
            card.setCardBackgroundColor(getColor(R.color.surface_light))

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(20, 20, 20, 20)

            val tvTitle = TextView(this)
            tvTitle.text = "${n.title} (${n.timestamp})"
            tvTitle.textSize = 14f
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            tvTitle.setTextColor(getColor(R.color.red))

            val tvMsg = TextView(this)
            tvMsg.text = n.message
            tvMsg.textSize = 13f
            tvMsg.setTextColor(getColor(R.color.text_primary_light))
            tvMsg.setPadding(0, 4, 0, 0)

            layout.addView(tvTitle)
            layout.addView(tvMsg)
            card.addView(layout)

            containerNotifs.addView(card)
        }
    }
}
