package com.skyline.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skyline.app.databinding.ActivitySupportBinding

class SupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
    }

    private fun setupClicks() = with(binding) {
        btnBack.setOnClickListener {
            finish()
        }

        btnCall.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:19001234")
            startActivity(dialIntent)
        }

        btnChat.setOnClickListener {
            Toast.makeText(this@SupportActivity, "Đang kết nối với tư vấn viên...", Toast.LENGTH_SHORT).show()
        }
    }
}
