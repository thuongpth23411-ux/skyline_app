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
        updateBottomNavBadge();
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
        } else if ("TICKET_DETAIL".equals(target)) {
            com.skyline.model.Ticket ticket = (com.skyline.model.Ticket) intent.getSerializableExtra("ticket_data");
            if (ticket != null) {
                updateNavSelection(binding.bottomNavigation.navFlights);
                showFragment(TicketDetailFragment.newInstance(ticket));
            } else {
                updateNavSelection(binding.bottomNavigation.navHome);
                showFragment(new HomeFragment());
            }
        } else {
            updateNavSelection(binding.bottomNavigation.navHome);
            showFragment(new HomeFragment());
        }
        updateBottomNavBadge();
    }

    private void setupClicks() {
        View.OnClickListener homeListener = v -> {
            updateNavSelection(binding.bottomNavigation.navHome);
            showFragment(new HomeFragment());
        };
        binding.bottomNavigation.navHome.setOnClickListener(homeListener);
        binding.bottomNavigation.tvNavHomeText.setOnClickListener(homeListener);

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
        boolean isHome = selectedView.getId() == binding.bottomNavigation.navHome.getId();
        binding.bottomNavigation.navHome.setSelected(isHome);
        binding.bottomNavigation.tvNavHomeText.setSelected(isHome);

        binding.bottomNavigation.navBook.setSelected(selectedView.getId() == binding.bottomNavigation.navBook.getId());
        binding.bottomNavigation.navFlights.setSelected(selectedView.getId() == binding.bottomNavigation.navFlights.getId());
        binding.bottomNavigation.navProfile.setSelected(selectedView.getId() == binding.bottomNavigation.navProfile.getId());
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }

    public void updateBottomNavBadge() {
        int count = sessionManager.getUnreadNotifCount();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).updateNotificationBadge(count);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
