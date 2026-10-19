package com.example.peairbrakes

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DieselSlipsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: DieselSlipsAdapter
    private var userEmail: String = ""
    private var photoUri: Uri? = null
    private var currentPhotoPath: String = ""

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            db.addDieselSlip(userEmail, currentPhotoPath)
            refreshList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_diesel_slips)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.diesel_slips_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("PeAirbrakesPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPref.getString("user_email", "") ?: ""

        val rv = findViewById<RecyclerView>(R.id.rv_diesel_slips)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = DieselSlipsAdapter(mutableListOf())
        rv.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab_add_slip).setOnClickListener {
            openCamera()
        }

        refreshList()
    }

    private fun refreshList() {
        val slips = db.getDieselSlips(userEmail)
        adapter.updateData(slips)
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }
        
        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            takePictureLauncher.launch(photoUri!!)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}

class DieselSlipsAdapter(private var slips: List<DieselSlip>) :
    RecyclerView.Adapter<DieselSlipsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPreview: ImageView = view.findViewById(R.id.iv_slip_preview)
        val tvDate: TextView = view.findViewById(R.id.tv_slip_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diesel_slip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slip = slips[position]
        holder.tvDate.text = slip.timestamp
        
        val bitmap = BitmapFactory.decodeFile(slip.photoPath)
        holder.ivPreview.setImageBitmap(bitmap)
    }

    override fun getItemCount() = slips.size

    fun updateData(newSlips: List<DieselSlip>) {
        slips = newSlips
        notifyDataSetChanged()
    }
}