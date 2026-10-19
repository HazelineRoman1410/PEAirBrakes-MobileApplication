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
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ActiveBreakdownsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ActiveBreakdownsAdapter
    private var techEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_active_breakdowns)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.active_breakdowns_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        techEmail = sharedPref.getString("user_email", "") ?: ""

        val rv = findViewById<RecyclerView>(R.id.rv_active_breakdowns)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ActiveBreakdownsAdapter(mutableListOf(), { request ->
            openMap(request.latitude, request.longitude)
        }, { request ->
            val intent = Intent(this, JobCardActivity::class.java)
            intent.putExtra("req_id", request.id)
            intent.putExtra("truck_reg", request.truckReg)
            startActivity(intent)
        })
        rv.adapter = adapter

        refreshList()
    }

    private fun refreshList() {
        val jobs = db.getAssignedJobs(techEmail)
        adapter.updateData(jobs)
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
    private val onItemClick: (AssistanceRequest) -> Unit
) : RecyclerView.Adapter<ActiveBreakdownsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        val ivPhoto: ImageView = view.findViewById(R.id.iv_breakdown_photo)
        val tvReg: TextView = view.findViewById(R.id.tv_truck_reg)
        val tvTime: TextView = view.findViewById(R.id.tv_timestamp)
        val tvDesc: TextView = view.findViewById(R.id.tv_description)
        val btnNav: Button = view.findViewById(R.id.btn_view_location)
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

        if (req.photoPath.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeFile(req.photoPath)
            holder.ivPhoto.setImageBitmap(bitmap)
        }

        holder.btnNav.setOnClickListener { onNavigateClick(req) }
        holder.root.setOnClickListener { onItemClick(req) }
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<AssistanceRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}