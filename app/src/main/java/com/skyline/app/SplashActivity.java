package com.skyline.app;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.SplashActivityBinding;

public class SplashActivity extends AppCompatActivity {
    private SplashActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SplashActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBackground();
        showContent();

        binding.getRoot().setOnClickListener(v -> {
            animatePlane();
            binding.getRoot().setClickable(false);
        });
    }

    private void setupBackground() {
        binding.ivBackground.post(() -> {
            Drawable drawable = binding.ivBackground.getDrawable();
            if (drawable == null) return;

            int viewWidth = binding.ivBackground.getWidth();
            int viewHeight = binding.ivBackground.getHeight();
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();

            float scale;
            float dx = 0f;

            if (drawableWidth * viewHeight > viewWidth * drawableHeight) {
                scale = (float) viewHeight / (float) drawableHeight;
                dx = (float) viewWidth - (float) drawableWidth * scale;
            } else {
                scale = (float) viewWidth / (float) drawableWidth;
            }

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, 0f);
            binding.ivBackground.setImageMatrix(matrix);
        });
    }

    private void showContent() {
        binding.ivLogo.animate().alpha(1f).setDuration(1000).start();
        binding.tvSplash.animate().alpha(1f).setDuration(1000).start();
    }

    private void animatePlane() {
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float deltaX = screenWidth - binding.ivPlane.getX();

        binding.ivPlane.animate()
            .translationX(deltaX)
            .translationY(-screenWidth * 0.5f)
            .setDuration(1500)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(() -> {
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            })
            .start();
    }
}
