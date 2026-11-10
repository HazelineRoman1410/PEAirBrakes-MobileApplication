package com.example.peairbrakes

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.Locale

class ActiveBreakdownsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ActiveBreakdownsAdapter
    private var techEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_active_breakdowns)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.active_breakdowns_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        techEmail = sharedPref.getString("user_email", "tech1@peairbrakes.co.za") ?: ""

        val rv = findViewById<RecyclerView>(R.id.rv_active_breakdowns)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = ActiveBreakdownsAdapter(
            mutableListOf(),
            onNavigateClick = { req -> openMap(req.latitude, req.longitude) },
            onAcceptClick = { req -> acceptJob(req) },
            onRejectClick = { req -> showRejectDialog(req) },
            onSafetyClick = { req ->
                val intent = Intent(this, SafetyInspectionActivity::class.java)
                intent.putExtra("req_id", req.id)
                startActivity(intent)
            },
            onAssessmentClick = { req ->
                val intent = Intent(this, BreakdownAssessmentActivity::class.java)
                intent.putExtra("req_id", req.id)
                startActivity(intent)
            },
            onProgressClick = { req ->
                val intent = Intent(this, RepairProgressActivity::class.java)
                intent.putExtra("req_id", req.id)
                startActivity(intent)
            },
            onPartsClick = { req ->
                val intent = Intent(this, SparePartsActivity::class.java)
                intent.putExtra("req_id", req.id)
                startActivity(intent)
            },
            onChatClick = { req ->
                val intent = Intent(this, JobMessagesActivity::class.java)
                intent.putExtra("req_id", req.id)
                startActivity(intent)
            },
            onReturnClick = { req ->
                val intent = Intent(this, ReturnJobActivity::class.java)
                intent.putExtra("req_id", req.id)
                startActivity(intent)
            },
            onJobCardClick = { req ->
                val intent = Intent(this, JobCardActivity::class.java)
                intent.putExtra("req_id", req.id)
                intent.putExtra("truck_reg", req.truckReg)
                startActivity(intent)
            }
        )
        rv.adapter = adapter

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            startActivity(Intent(this, TechnicianDashboardActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val jobs = db.getAssignedJobs(techEmail)
        adapter.updateData(jobs)
    }

    private fun acceptJob(req: AssistanceRequest) {
        db.updateRequestStatus(req.id, "Technician En Route", "30 mins")
        Toast.makeText(this, "Job #${req.id} Accepted! En Route.", Toast.LENGTH_SHORT).show()
        refreshList()
    }

    private fun showRejectDialog(req: AssistanceRequest) {
        val etReason = EditText(this)
        etReason.hint = "Reason for rejecting job..."

        AlertDialog.Builder(this)
            .setTitle("Reject Assignment #${req.id}")
            .setMessage("Please enter rejection reason. The job will be returned to Dispatch for reallocation.")
            .setView(etReason)
            .setPositiveButton("Submit Rejection") { _, _ ->
                val reason = etReason.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Reason is mandatory!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                db.rejectAssignment(req.id, techEmail, reason)
                Toast.makeText(this, "Assignment Rejected. Job returned to Dispatch.", Toast.LENGTH_LONG).show()
                refreshList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openMap(lat: Double, lon: Double) {
        val uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", lat, lon)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }
}

class ActiveBreakdownsAdapter(
    private var requests: List<AssistanceRequest>,
    private val onNavigateClick: (AssistanceRequest) -> Unit,
    private val onAcceptClick: (AssistanceRequest) -> Unit,
    private val onRejectClick: (AssistanceRequest) -> Unit,
    private val onSafetyClick: (AssistanceRequest) -> Unit,
    private val onAssessmentClick: (AssistanceRequest) -> Unit,
    private val onProgressClick: (AssistanceRequest) -> Unit,
    private val onPartsClick: (AssistanceRequest) -> Unit,
    private val onChatClick: (AssistanceRequest) -> Unit,
    private val onReturnClick: (AssistanceRequest) -> Unit,
    private val onJobCardClick: (AssistanceRequest) -> Unit
) : RecyclerView.Adapter<ActiveBreakdownsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStatusBadge: TextView = view.findViewById(R.id.tv_job_status_badge)
        val tvUrgencyBadge: TextView = view.findViewById(R.id.tv_urgency_badge)
        val ivPhoto: ImageView = view.findViewById(R.id.iv_breakdown_photo)
        val tvReg: TextView = view.findViewById(R.id.tv_truck_reg)
        val tvTime: TextView = view.findViewById(R.id.tv_timestamp)
        val tvDesc: TextView = view.findViewById(R.id.tv_description)

        val rowAcceptReject: LinearLayout = view.findViewById(R.id.row_accept_reject)
        val btnAccept: Button = view.findViewById(R.id.btn_accept_job)
        val btnReject: Button = view.findViewById(R.id.btn_reject_job)

        val btnNav: Button = view.findViewById(R.id.btn_view_location)
        val btnSafety: Button = view.findViewById(R.id.btn_safety)
        val btnAssessment: Button = view.findViewById(R.id.btn_assessment)
        val btnProgress: Button = view.findViewById(R.id.btn_progress)
        val btnParts: Button = view.findViewById(R.id.btn_parts)
        val btnChat: Button = view.findViewById(R.id.btn_messages)
        val btnReturn: Button = view.findViewById(R.id.btn_return_job)
        val btnJobCard: Button = view.findViewById(R.id.btn_open_job_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_breakdown_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val req = requests[position]
        holder.tvReg.text = req.truckReg
        holder.tvTime.text = "Logged: ${req.timestamp}"
        holder.tvDesc.text = req.description
        holder.tvStatusBadge.text = "Status: ${req.status}"
        holder.tvUrgencyBadge.text = "Urgency: ${req.urgency}"

        if (req.status == "Technician Assigned" || req.status == "Awaiting Technician Acceptance") {
            holder.rowAcceptReject.visibility = View.VISIBLE
        } else {
            holder.rowAcceptReject.visibility = View.GONE
        }

        if (req.photoPath.isNotEmpty() && File(req.photoPath).exists()) {
            val bitmap = BitmapFactory.decodeFile(req.photoPath)
            if (bitmap != null) {
                holder.ivPhoto.setImageBitmap(bitmap)
                holder.ivPhoto.visibility = View.VISIBLE
            } else {
                holder.ivPhoto.visibility = View.GONE
            }
        } else {
            holder.ivPhoto.visibility = View.GONE
        }

        holder.btnAccept.setOnClickListener { onAcceptClick(req) }
        holder.btnReject.setOnClickListener { onRejectClick(req) }
        holder.btnNav.setOnClickListener { onNavigateClick(req) }
        holder.btnSafety.setOnClickListener { onSafetyClick(req) }
        holder.btnAssessment.setOnClickListener { onAssessmentClick(req) }
        holder.btnProgress.setOnClickListener { onProgressClick(req) }
        holder.btnParts.setOnClickListener { onPartsClick(req) }
        holder.btnChat.setOnClickListener { onChatClick(req) }
        holder.btnReturn.setOnClickListener { onReturnClick(req) }
        holder.btnJobCard.setOnClickListener { onJobCardClick(req) }
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<AssistanceRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}
