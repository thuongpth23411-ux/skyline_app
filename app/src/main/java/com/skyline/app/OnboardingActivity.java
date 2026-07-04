package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;
import com.skyline.app.databinding.ItemOnboardingBinding;
import com.skyline.app.databinding.OnboardingActivityBinding;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private OnboardingActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        binding = OnboardingActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<OnboardingStep> items = new ArrayList<>();
        items.add(new OnboardingStep(R.drawable.onboarding_background1, getString(R.string.onboarding_title_1), getString(R.string.onboarding_desc_1)));
        items.add(new OnboardingStep(R.drawable.onboarding_background2, getString(R.string.onboarding_title_2), getString(R.string.onboarding_desc_2)));

        binding.viewPager.setAdapter(new OnboardingAdapter(items));

        new TabLayoutMediator(binding.tabDots, binding.viewPager, (tab, position) -> {}).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == items.size() - 1) {
                    binding.btnNext.setText(getString(R.string.btn_experience_now));
                    binding.tvSkip.setVisibility(View.GONE);
                    binding.layoutLang.setVisibility(View.VISIBLE);
                    binding.tvLangLabel.setVisibility(View.VISIBLE);
                } else {
                    binding.btnNext.setText(getString(R.string.btn_next));
                    binding.tvSkip.setVisibility(View.VISIBLE);
                    binding.layoutLang.setVisibility(View.GONE);
                    binding.tvLangLabel.setVisibility(View.GONE);
                }
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() < items.size() - 1) {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            } else {
                startActivity(new Intent(OnboardingActivity.this, HomeActivity.class));
                finish();
            }
        });

        binding.tvSkip.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, HomeActivity.class));
            finish();
        });
    }

    static class OnboardingStep {
        int imageRes;
        String title;
        String desc;
        OnboardingStep(int imageRes, String title, String desc) {
            this.imageRes = imageRes;
            this.title = title;
            this.desc = desc;
        }
    }

    class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        private List<OnboardingStep> items;

        OnboardingAdapter(List<OnboardingStep> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemOnboardingBinding b = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OnboardingStep item = items.get(position);
            holder.b.ivOnboarding.setImageResource(item.imageRes);
            holder.b.tvTitle.setText(item.title);
            holder.b.tvDesc.setText(item.desc);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemOnboardingBinding b;
            ViewHolder(ItemOnboardingBinding b) {
                super(b.getRoot());
                this.b = b;
            }
        }
    }
}
