package com.skyline.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import com.skyline.app.databinding.ActivityHomeBinding;
import com.skyline.app.utils.SessionManager;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupClicks();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String target = intent != null ? intent.getStringExtra("TARGET_FRAGMENT") : null;
        if ("BOOK".equals(target)) {
            String destCode = intent.getStringExtra("DESTINATION_CODE");
            BookFragment fragment = new BookFragment();
            if (destCode != null) {
                Bundle args = new Bundle();
                args.putString("destCode", destCode);
                fragment.setArguments(args);
            }
            updateNavSelection(binding.bottomNavigation.navBook);
            showFragment(fragment);
        } else if ("FLIGHTS".equals(target)) {
            updateNavSelection(binding.bottomNavigation.navFlights);
            showFragment(new FlightsFragment2());
        } else {
            updateNavSelection(binding.bottomNavigation.navHome);
            showFragment(new HomeFragment());
        }
    }

    private void setupClicks() {
        binding.bottomNavigation.navHome.setOnClickListener(v -> {
            updateNavSelection(v);
            showFragment(new HomeFragment());
        });
        binding.bottomNavigation.navBook.setOnClickListener(v -> {
            updateNavSelection(v);
            showFragment(new BookFragment());
        });
        binding.bottomNavigation.navFlights.setOnClickListener(v -> {
            updateNavSelection(v);
            showFragment(new FlightsFragment2());
        });
        binding.bottomNavigation.navProfile.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                updateNavSelection(v);
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Loại bỏ hiệu ứng trượt để giống Fragment
            } else {
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        });

        binding.fabAiChat.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ChatActivity.class)));
    }

    private void updateNavSelection(View selectedView) {
        binding.bottomNavigation.navHome.setSelected(selectedView.getId() == binding.bottomNavigation.navHome.getId());
        binding.bottomNavigation.navBook.setSelected(selectedView.getId() == binding.bottomNavigation.navBook.getId());
        binding.bottomNavigation.navFlights.setSelected(selectedView.getId() == binding.bottomNavigation.navFlights.getId());
        binding.bottomNavigation.navProfile.setSelected(selectedView.getId() == binding.bottomNavigation.navProfile.getId());
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
