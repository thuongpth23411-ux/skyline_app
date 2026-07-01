package com.skyline.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.skyline.app.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
        // Default select Home
        binding.bottomNavigation.navHome.isSelected = true
    }

    private fun setupClicks() = with(binding) {
        bottomNavigation.navHome.setOnClickListener {
            updateNavSelection(it)
            showFragment(HomeFragment())
        }
        bottomNavigation.navBook.setOnClickListener {
            toast("Mở màn hình Đặt vé")
        }
        bottomNavigation.navFlights.setOnClickListener {
            updateNavSelection(it)
            showFragment(FlightsFragment())
        }
        bottomNavigation.navProfile.setOnClickListener {
            updateNavSelection(it)
            startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
        }
    }

    private fun updateNavSelection(selectedView: android.view.View) {
        with(binding.bottomNavigation) {
            navHome.isSelected = selectedView.id == navHome.id
            navBook.isSelected = selectedView.id == navBook.id
            navFlights.isSelected = selectedView.id == navFlights.id
            navProfile.isSelected = selectedView.id == navProfile.id
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
