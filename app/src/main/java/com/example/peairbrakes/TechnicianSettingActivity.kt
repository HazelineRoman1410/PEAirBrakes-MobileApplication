package com.example.peairbrakes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.peairbrakes.databinding.ActivityTechnicianSettingBinding

class TechnicianSettingActivity: AppCompatActivity() {

    private var _binding: ActivityTechnicianSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        _binding = ActivityTechnicianSettingBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)

        val btnEng = findViewById<Button>(R.id.btn_english)
        val btnAfr = findViewById<Button>(R.id.btn_afrikaans)
        val btnXho = findViewById<Button>(R.id.btn_xhosa)
        val btnTsw = findViewById<Button>(R.id.btn_tswana)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        with(binding){
            btnEng.setOnClickListener {
                changeLanguage("en")
            }
            btnAfr.setOnClickListener {
                changeLanguage("af")
            }
            btnXho.setOnClickListener {
                changeLanguage("xh")
            }
            btnTsw.setOnClickListener {
                changeLanguage("tn")
            }
        }

        btnBack.setOnClickListener {
            val technicianIntent = Intent(this@TechnicianSettingActivity, TechnicianDashboardActivity::class.java)
            startActivity(technicianIntent)
            finish()
        }

        binding.tvThemeSwitch.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        binding.tvThemeSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun changeLanguage(langCode: String) {

        val appLocale = LocaleListCompat.forLanguageTags(langCode)

        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}