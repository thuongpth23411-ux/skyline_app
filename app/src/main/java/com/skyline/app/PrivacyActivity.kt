package com.skyline.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skyline.app.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() = with(binding) {
        btnBack.setOnClickListener {
            finish()
        }

        btnContactSupport.setOnClickListener {
            startActivity(Intent(this@PrivacyActivity, SupportActivity::class.java))
        }
    }
}
