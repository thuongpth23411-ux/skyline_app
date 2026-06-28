package com.skyline.app

import android.content.Intent
import android.graphics.Matrix
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.skyline.app.databinding.SplashActivityBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: SplashActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackground()

        // Hiện logo và text ngay
        showContent()

        // Chạm vào màn hình để máy bay cất cánh
        binding.root.setOnClickListener {
            animatePlane()
            // Vô hiệu hóa click để tránh nhấn nhiều lần
            binding.root.isClickable = false
        }
    }

    private fun setupBackground() {
        binding.ivBackground.post {
            val drawable = binding.ivBackground.drawable ?: return@post
            val viewWidth = binding.ivBackground.width
            val viewHeight = binding.ivBackground.height
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            val scale: Float
            var dx = 0f

            if (drawableWidth * viewHeight > viewWidth * drawableHeight) {
                scale = viewHeight.toFloat() / drawableHeight.toFloat()
                dx = viewWidth - drawableWidth * scale
            } else {
                scale = viewWidth.toFloat() / drawableWidth.toFloat()
            }

            val matrix = Matrix()
            matrix.setScale(scale, scale)
            matrix.postTranslate(dx, 0f)
            binding.ivBackground.imageMatrix = matrix
        }
    }

    private fun showContent() {
        binding.ivLogo.animate().alpha(1f).setDuration(1000).start()
        binding.tvSplash.animate().alpha(1f).setDuration(1000).start()
    }

    private fun animatePlane() {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        
        // Tính toán quãng đường bay để đuôi vừa khuất là chuyển cảnh ngay
        // translationX là khoảng cách dịch chuyển so với vị trí gốc
        // Vị trí gốc của mép trái máy bay là binding.ivPlane.x
        val deltaX = screenWidth - binding.ivPlane.x

        binding.ivPlane.animate()
            .translationX(deltaX)
            .translationY(-screenWidth * 0.5f) // Bay chéo hướng lên trên
            .setDuration(1500) 
            .setInterpolator(AccelerateInterpolator()) // Bay nhanh dần đều
            .withEndAction {
                startActivity(Intent(this, OnboardingActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            .start()
    }
}