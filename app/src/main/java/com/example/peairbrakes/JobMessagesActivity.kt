package com.example.peairbrakes

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class JobMessagesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var reqId: Int = -1
    private var userEmail: String = ""
    private var userName: String = ""

    private lateinit var containerMessages: LinearLayout
    private lateinit var etMsgText: EditText
    private lateinit var tvChatTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_messages)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.messages_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        reqId = intent.getIntExtra("req_id", -1)

        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "") ?: ""
        val currentUser = db.getUser(userEmail)
        userName = currentUser?.name ?: userEmail

        containerMessages = findViewById(R.id.container_messages)
        etMsgText = findViewById(R.id.et_message_text)
        tvChatTitle = findViewById(R.id.tv_chat_title)

        tvChatTitle.text = "Job #$reqId Messages"

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_sos).setOnClickListener { SosHelper.showSosDialog(this, reqId) }

        refreshMessages()

        findViewById<Button>(R.id.btn_send_message).setOnClickListener { sendMessage() }
    }

    private fun refreshMessages() {
        containerMessages.removeAllViews()
        val messages = db.getMessagesForJob(reqId)

        if (messages.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No messages yet for Job #$reqId. Start conversation below."
            tvEmpty.textSize = 14f
            tvEmpty.setTextColor(getColor(R.color.text_secondary_light))
            containerMessages.addView(tvEmpty)
            return
        }

        for (msg in messages) {
            val isMe = msg.senderEmail == userEmail

            val card = MaterialCardView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            card.layoutParams = params
            card.radius = 16f
            card.setCardBackgroundColor(if (isMe) getColor(R.color.surface_light) else getColor(R.color.white))

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(20, 20, 20, 20)

            val tvSender = TextView(this)
            tvSender.text = "${msg.senderName} (${msg.timestamp})"
            tvSender.textSize = 12f
            tvSender.setTypeface(null, android.graphics.Typeface.BOLD)
            tvSender.setTextColor(if (isMe) getColor(R.color.red) else getColor(R.color.text_primary_light))

            val tvText = TextView(this)
            tvText.text = msg.messageText
            tvText.textSize = 14f
            tvText.setTextColor(getColor(R.color.text_primary_light))
            tvText.setPadding(0, 4, 0, 0)

            layout.addView(tvSender)
            layout.addView(tvText)
            card.addView(layout)

            containerMessages.addView(card)
        }
    }

    private fun sendMessage() {
        val text = etMsgText.text.toString().trim()
        if (text.isEmpty()) return

        val msg = JobMessage(
            reqIdRef = reqId,
            senderEmail = userEmail,
            senderName = userName,
            receiverEmail = "admin@peairbrakes.co.za",
            messageText = text
        )

        if (db.addMessage(msg)) {
            etMsgText.setText("")
            refreshMessages()
        } else {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
        }
    }
}
