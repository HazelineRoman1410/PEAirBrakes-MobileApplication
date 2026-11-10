package com.example.peairbrakes

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object SosHelper {

    fun showSosDialog(context: Context, reqId: Int = 0) {
        val sharedPref = context.getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "technician@peairbrakes.co.za") ?: ""
        val db = DatabaseHelper(context)

        AlertDialog.Builder(context)
            .setTitle("🚨 EMERGENCY SOS ACTIVATED 🚨")
            .setMessage("Do you need immediate emergency roadside assistance, safety escalation, or administrator intervention?")
            .setPositiveButton("TRIGGER SOS ALERT") { _, _ ->
                // Create emergency notification for Admin
                val notif = AppNotification(
                    userEmail = "admin@peairbrakes.co.za",
                    title = "🚨 EMERGENCY SOS ALERT",
                    message = "User $userEmail has triggered an Emergency SOS Alert on Job #$reqId!",
                    reqIdRef = reqId,
                    type = "EMERGENCY"
                )
                db.addNotification(notif)

                // If associated with a job, update status to Emergency
                if (reqId > 0) {
                    db.updateRequestStatus(reqId, "EMERGENCY SOS TRIGGERED")
                }

                Toast.makeText(context, "🚨 EMERGENCY SOS SENT! Dispatch and Admin notified.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
