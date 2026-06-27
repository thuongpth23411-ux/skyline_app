package com.skyline.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.skyline.app.databinding.ActivityAboutBinding
import com.skyline.model.TeamMember

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFeatures()
        setupTeam()
        setupReasons()
        setupClicks()
    }

    private fun setupFeatures() = with(binding) {
        featureEasy.tvFeatureTitle.text = "Đặt vé dễ dàng\nNhanh chóng & Tiện lợi"
        featureEasy.tvFeatureDesc.text = "Tại Skyline, người dùng có thể tra cứu hàng trăm chuyến bay chỉ với vài thao tác. Chúng tôi tối ưu giao diện, giảm số bước đặt vé và đảm bảo mọi thông tin đều rõ ràng, trực quan."
        featureEasy.imgFeature.setImageResource(R.drawable.img_booking_easy)

        featurePrice.tvFeatureTitle.text = "Giá vé minh bạch\nKhông phí ẩn"
        featurePrice.tvFeatureDesc.text = "Skyline cam kết hiển thị đúng giá, đúng thuế, đúng phí. Mọi chi phí đều được liệt kê rõ ràng giúp khách hàng an tâm khi thanh toán và lựa chọn chuyến bay phù hợp nhất."
        featurePrice.imgFeature.setImageResource(R.drawable.img_transparent_price)

        featureSupport.tvFeatureTitle.text = "Hỗ trợ 24/7\nLuôn đồng hành cùng bạn"
        featureSupport.tvFeatureDesc.text = "Đội ngũ hỗ trợ của Skyline sẵn sàng tư vấn mọi lúc, giải đáp vé, hành lý, thay đổi chuyến bay và các dịch vụ liên quan."
        featureSupport.imgFeature.setImageResource(R.drawable.img_support_247)
    }

    private fun setupTeam() {
        val members = listOf(
            TeamMember("Trịnh Thị Thùy Trang", R.drawable.img_team1),
            TeamMember("Phạm Thị Hoài Thương", R.drawable.img_team2),
            TeamMember("Đào Thị Cẩm Vy", R.drawable.img_team3),
            TeamMember("Trần Thị Thiên Thảo", R.drawable.img_team4),
            TeamMember("Nguyễn Ngọc Tường Vy", R.drawable.img_team5),
        )
        binding.teamRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.teamRecycler.adapter = TeamAdapter(members)
    }

    private fun setupReasons() = with(binding) {
        reasonPrice.tvIcon.text = "◷"
        reasonPrice.tvReasonTitle.text = "Nội dung & giá vé\nđáng tin cậy"
        reasonPrice.tvReasonDesc.text = "Báo giá minh bạch, cập nhật theo thời gian thực từ các hãng bay trong nước và quốc tế."

        reasonTools.tvIcon.text = "⌂"
        reasonTools.tvReasonTitle.text = "Công cụ quản lý hành\ntrình thông minh"
        reasonTools.tvReasonDesc.text = "Theo dõi đặt chỗ, hành lý, thay đổi chuyến bay và thông báo tự động – all in one."

        reasonSupport.tvIcon.text = "♧"
        reasonSupport.tvReasonTitle.text = "Đội ngũ hỗ trợ\ntận tâm"
        reasonSupport.tvReasonDesc.text = "Skyline cam kết đồng hành trước, trong và sau chuyến bay, giúp bạn an tâm tuyệt đối."
    }

    private fun setupClicks() = with(binding) {
        btnNotification.setOnClickListener { Toast.makeText(this@AboutActivity, "Thông báo", Toast.LENGTH_SHORT).show() }
        bottomNav.navHome.setOnClickListener { finish() }
        bottomNav.navBook.setOnClickListener { Toast.makeText(this@AboutActivity, "Mở màn hình Đặt vé", Toast.LENGTH_SHORT).show() }
        bottomNav.navFlights.setOnClickListener { Toast.makeText(this@AboutActivity, "Mở màn hình Chuyến bay", Toast.LENGTH_SHORT).show() }
        bottomNav.navProfile.setOnClickListener { startActivity(Intent(this@AboutActivity, LoginActivity::class.java)) }
    }
}
