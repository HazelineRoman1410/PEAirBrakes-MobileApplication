package com.example.peairbrakes

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val truckLogo = findViewById<ImageView>(R.id.truck_logo)

        // Animation: Move the truck from left to right
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val animation = ObjectAnimator.ofFloat(truckLogo, "translationX", 0f, screenWidth + 300f)
        animation.duration = 2000 // 2 seconds
        animation.start()

        // Transition to LoginActivity after the animation
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2500) // Delay slightly longer than animation
    }
}