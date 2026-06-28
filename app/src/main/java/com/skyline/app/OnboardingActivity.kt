package com.skyline.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.skyline.app.databinding.OnboardingActivityBinding
import com.skyline.app.databinding.ItemOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: OnboardingActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cấu hình hiển thị full màn hình (Edge-to-Edge)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        binding = OnboardingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = listOf(
            OnboardingStep(
                R.drawable.onboarding_background1,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
            ),
            OnboardingStep(
                R.drawable.onboarding_background2,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
            )
        )

        binding.viewPager.adapter = OnboardingAdapter(items)

        // Setup dots (TabLayoutMediator)
        TabLayoutMediator(binding.tabDots, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == items.size - 1) {
                    binding.btnNext.text = getString(R.string.btn_experience_now)
                    binding.tvSkip.visibility = View.GONE
                    binding.layoutLang.visibility = View.VISIBLE
                    binding.tvLangLabel.visibility = View.VISIBLE
                } else {
                    binding.btnNext.text = getString(R.string.btn_next)
                    binding.tvSkip.visibility = View.VISIBLE
                    binding.layoutLang.visibility = View.GONE
                    binding.tvLangLabel.visibility = View.GONE
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < items.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }

        binding.tvSkip.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    data class OnboardingStep(val imageRes: Int, val title: String, val desc: String)

    inner class OnboardingAdapter(private val items: List<OnboardingStep>) :
        RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.b.ivOnboarding.setImageResource(item.imageRes)
            holder.b.tvTitle.text = item.title
            holder.b.tvDesc.text = item.desc
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val b: ItemOnboardingBinding) : RecyclerView.ViewHolder(b.root)
    }
}