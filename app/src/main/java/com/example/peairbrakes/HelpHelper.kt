package com.example.peairbrakes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog

object HelpHelper {

    fun showHelpDialog(context: Context, screenName: String = "General") {
        val helpMessage = getHelpMessageForScreen(screenName)

        AlertDialog.Builder(context)
            .setTitle("❓ App Help & Navigation Support")
            .setMessage(helpMessage)
            .setPositiveButton("📖 Open Full User Manual") { _, _ ->
                val intent = Intent(context, UserManualActivity::class.java)
                context.startActivity(intent)
            }
            .setNeutralButton("🤖 Ask AI Assistant") { _, _ ->
                val intent = Intent(context, AiAssistantActivity::class.java)
                context.startActivity(intent)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun getHelpMessageForScreen(screenName: String): String {
        return when (screenName) {
            "TechnicianDashboard" ->
                "💡 TECHNICIAN DASHBOARD HELP:\n" +
                "• Tap 'Active Breakdowns' to view assigned jobs and start travel/safety inspection.\n" +
                "• Tap 'AI Assistant' for technical troubleshooting, SPN error codes & brake specs.\n" +
                "• Tap 'Vehicle History' to check previous breakdown repairs by Truck Reg/VIN.\n" +
                "• Tap 'My Performance' to view your fix rates & metrics.\n" +
                "• Need immediate emergency aid? Tap 🚨 SOS at the top right."

            "CustomerDashboard" ->
                "💡 CUSTOMER DASHBOARD HELP:\n" +
                "• Tap 'Request Roadside Assistance' to log a breakdown with photos & map location.\n" +
                "• Tap 'My Requests History' to view assigned technicians & track real-time ETA.\n" +
                "• When a quotation is submitted by a technician, tap your request to review and approve it."

            "ActiveBreakdowns" ->
                "💡 ACTIVE BREAKDOWNS HELP:\n" +
                "• 'Accept Job' / 'Reject Job': Accept assignment or reject with a reason.\n" +
                "• '🗺️ Map Nav': Opens Google Maps navigation to breakdown site.\n" +
                "• '🛡️ Safety Inspection': Mandatory area & vehicle safety checklist.\n" +
                "• '📋 Assessment & Quote': Complete breakdown diagnosis and submit repair quote.\n" +
                "• '🔄 Progress Log': Submit progress milestones and updated ETAs.\n" +
                "• '⚙️ Parts & Approval': Record parts used or request additional work authorization.\n" +
                "• '🚩 Flag Return': Flag unsuccessful jobs or complaints for return visits."

            "JobCard" ->
                "💡 DIGITAL JOB CARD HELP:\n" +
                "• Select 'Start KM' when leaving for job and 'Arrived' upon arrival on site.\n" +
                "• Complete truck make, odometer reading, work done, and parts used.\n" +
                "• Capture driver signature before submitting the job card to Admin for verification."

            "AdminDashboard" ->
                "💡 DISPATCH ADMIN DASHBOARD HELP:\n" +
                "• '🤖 Auto-Allocate': Runs Intelligent Allocation Engine to rank best qualified technicians.\n" +
                "• '✍️ Override Tech': Manually reassign a technician with reason logging.\n" +
                "• '📋 Review Quote': Approve or reject technician repair quotations.\n" +
                "• '🗺️ Open Map': View live technician locations and breakdown sites.\n" +
                "• '✅ Verify & Invoice': Transmit verified job cards to invoicing system."

            else ->
                "💡 NEED HELP FINDING SOMETHING?\n" +
                "• Explore the app features using the dashboard cards.\n" +
                "• Tap 'Open Full User Manual' to view detailed step-by-step instructions for Technicians, Customers, and Administrators."
        }
    }
}
