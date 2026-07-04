package com.skyline.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.skyline.app.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupMenuClicks()
    }

    private fun setupBottomNavigation() = with(binding.bottomNavigation) {
        // Highlight Profile tab
        navProfile.isSelected = true

        navHome.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, HomeActivity::class.java))
            finish()
        }
        navBook.setOnClickListener {
            toast("Mở màn hình Đặt vé")
        }
        navFlights.setOnClickListener {
            // Assuming Flights is a fragment in Home or another activity
            val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
            // You might want to pass an extra to show FlightsFragment
            startActivity(intent)
            finish()
        }
        navProfile.setOnClickListener {
            // Already here
        }
    }

    private fun setupMenuClicks() = with(binding) {
        itemMemberInfo.setOnClickListener {
            toast("Thông tin hội viên")
        }
        itemTerms.setOnClickListener {
            toast("Điều khoản & Điều kiện")
        }
        itemPrivacy.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, PrivacyActivity::class.java))
        }
        btnLogoutItem.setOnClickListener {
            toast("Đăng xuất")
            // Handle logout logic here
        }
        btnViewDetails.setOnClickListener {
            toast("Xem chi tiết điểm thưởng")
        }
        layoutQuickActions.getChildAt(3).setOnClickListener {
            startActivity(Intent(this@ProfileActivity, SupportActivity::class.java))
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
