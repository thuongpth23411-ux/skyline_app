package com.skyline.app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.skyline.app.databinding.ActivityHomeBinding
import com.skyline.model.Destination
import com.skyline.model.Experience
import com.skyline.model.Promotion

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSectionTitles()
        setupPromotionPager()
        setupDestinations()
        setupExperiences()
        setupClicks()
    }

    private fun setupSectionTitles() {
        binding.promotionHeader.tvSectionTitle.text = getString(R.string.promotion_program)
        binding.destinationHeader.tvSectionTitle.text = getString(R.string.discover_destinations)
        binding.experienceHeader.tvSectionTitle.text = getString(R.string.experience_skyline)
    }

    private fun setupPromotionPager() {
        val promotions = listOf(
            Promotion(
                "Xin chào Bangkok! Ưu đãi ngay 20%",
                "11/06/2026 - 10/07/2026",
                R.drawable.img_promo_bangkok,
            ),
            Promotion(
                "Thứ 6 mở app – giảm đến 10%",
                "17/06/2026 - 01/07/2026",
                R.drawable.img_brand_banner,
            ),
        )

        binding.promoPager.adapter = PromotionAdapter(
            promotions,
        ) {
            toast("Đã chọn: ${it.title}")
        }

        createDots(binding.promoDots, promotions.size)
        selectDot(binding.promoDots, 0)

        binding.promoPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    selectDot(binding.promoDots, position)
                }
            }
        )
    }

    private fun setupDestinations() {
        val destinations = listOf(
            Destination(
                "Việt Nam",
                "Phú Quốc – “đảo ngọc” xinh đẹp của Việt Nam",
                R.drawable.img_destination_phuquoc,
            ),
            Destination(
                "Việt Nam",
                "Đà Nẵng – thành phố đáng sống nhất Việt Nam",
                R.drawable.img_destination_danang,
            ),
        )

        binding.destinationRecycler.apply {
            layoutManager = LinearLayoutManager(
                this@HomeActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = DestinationAdapter(
                destinations,
            ) {
                toast("Khám phá ${it.title}")
            }
        }
    }

    private fun setupExperiences() {
        val experiences = listOf(
            Experience(
                "Mức giá tốt, dịch vụ chu đáo.",
                "Hạng Phổ thông",
                "Thoải mái trong mọi hành trình",
                R.drawable.img_experience_economy,
            ),
            Experience(
                "Bay phong cách, tận hưởng khác biệt",
                "Hạng Business",
                "Trải nghiệm đẳng cấp cho mỗi hành trình.",
                R.drawable.img_experience_first,
            ),
            Experience(
                "Nâng tầm trải nghiệm",
                "Hạng Thương gia",
                "Không gian riêng tư, dịch vụ tinh tế.",
                R.drawable.img_experience_first,
            ),
        )

        binding.experienceRecycler.apply {
            layoutManager = LinearLayoutManager(
                this@HomeActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = ExperienceAdapter(
                experiences,
            ) {
                toast("Đã chọn ${it.title}")
            }
        }
    }

    private fun setupClicks() = with(binding) {
        btnNotification.setOnClickListener { toast("Thông báo") }
        btnExploreNow.setOnClickListener { toast("Mở ưu đãi Thứ 6") }

        promotionHeader.tvViewAll.setOnClickListener { toast("Tất cả ưu đãi") }
        destinationHeader.tvViewAll.setOnClickListener { toast("Tất cả điểm đến") }
        experienceHeader.tvViewAll.setOnClickListener { toast("Tất cả trải nghiệm") }

        binding.btnAboutUs.setOnClickListener { toast("Chuyển sang trang Về chúng tôi") }

        memberCard.btnRegister.setOnClickListener { toast("Đăng ký thành viên") }
        memberCard.tvLogin.setOnClickListener { toast("Đăng nhập") }

        bottomNavigation.navHome.setOnClickListener { scrollContent.smoothScrollTo(0, 0) }
        bottomNavigation.navBook.setOnClickListener { toast("Mở màn hình Đặt vé") }
        bottomNavigation.navFlights.setOnClickListener { toast("Mở màn hình Chuyến bay") }
        bottomNavigation.navProfile.setOnClickListener { toast("Mở màn hình Hồ sơ") }
    }

    private fun createDots(container: LinearLayout, count: Int) {
        container.removeAllViews()
        repeat(count) {
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(9.dp, 9.dp).also { params ->
                    params.marginStart = 5.dp
                    params.marginEnd = 5.dp
                }
                background = ContextCompat.getDrawable(
                    this@HomeActivity,
                    R.drawable.bg_dot,
                )
            }
            container.addView(dot)
        }
    }

    private fun selectDot(container: LinearLayout, selected: Int) {
        for (index in 0 until container.childCount) {
            val dot = container.getChildAt(index)
            dot.background = ContextCompat.getDrawable(
                this,
                if (index == selected) R.drawable.bg_dot_active else R.drawable.bg_dot,
            )
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}