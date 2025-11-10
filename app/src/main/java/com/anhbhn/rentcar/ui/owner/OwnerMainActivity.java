package com.anhbhn.rentcar.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.ui.auth.LoginActivity;
import com.anhbhn.rentcar.ui.main.SettingsFragment;
import com.anhbhn.rentcar.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class OwnerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;

    // Tags cho Fragment
    private static final String TAG_MY_CARS = "my_cars";
    private static final String TAG_MY_RENTALS = "my_rentals";
    private static final String TAG_OWNER_SETTINGS = "settings";
    private String currentTag = TAG_MY_CARS;

    // IDs từ menu_owner_bottom_nav.xml
    private static final int NAV_MY_CARS_ID = R.id.nav_my_cars;
    private static final int NAV_MY_RENTALS_ID = R.id.nav_my_rentals;
    private static final int NAV_SETTINGS_ID = R.id.nav_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = TokenManager.getToken(this);
        String userRole = TokenManager.getUserRole(this);

        if (token == null || token.isEmpty() || !"CAR_OWNER".equals(userRole)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        bottomNavigation = findViewById(R.id.bottom_navigation_owner);

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new MyCarsFragment(), TAG_MY_CARS);
            bottomNavigation.setSelectedItemId(NAV_MY_CARS_ID);
        } else {
            currentTag = savedInstanceState.getString("currentTag", TAG_MY_CARS);
            restoreFragment();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTag", currentTag);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == NAV_MY_CARS_ID) {
                loadFragment(new MyCarsFragment(), TAG_MY_CARS);
                return true;
            } else if (itemId == NAV_MY_RENTALS_ID) {
                loadFragment(new MyRentalsFragment(), TAG_MY_RENTALS);
                return true;
            } else if (itemId == NAV_SETTINGS_ID) {
                loadFragment(new SettingsFragment(), TAG_OWNER_SETTINGS);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        // ✅ FIX LỖI OVERLAY: Ẩn tất cả các Fragment đang được quản lý
        hideAllFragments(transaction);

        if (existingFragment != null) {
            // Show Fragment đã tồn tại
            transaction.show(existingFragment);
        } else {
            // Thêm Fragment mới vào Container ID (nav_host_fragment_owner)
            transaction.add(R.id.nav_host_fragment_owner, fragment, tag);
        }

        currentTag = tag;
        transaction.commit();
        updateToolbarTitle(tag);
    }

    /**
     * ✅ FIX: Ẩn TẤT CẢ các Fragments hiện đang được quản lý bởi FragmentManager
     * (Đây là logic cốt lõi để ngăn chặn lỗi đè lên nhau).
     */
    private void hideAllFragments(FragmentTransaction transaction) {
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null && fragment.isAdded() && !fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
    }

    private void restoreFragment() {
        // Logic phục hồi trạng thái Fragment sau khi Activity bị recreate
        Fragment fragment = fragmentManager.findFragmentByTag(currentTag);
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            hideAllFragments(transaction);
            transaction.show(fragment);
            transaction.commit();

            // Cập nhật Bottom Navigation
            if (TAG_MY_CARS.equals(currentTag)) {
                bottomNavigation.setSelectedItemId(NAV_MY_CARS_ID);
            } else if (TAG_MY_RENTALS.equals(currentTag)) {
                bottomNavigation.setSelectedItemId(NAV_MY_RENTALS_ID);
            } else if (TAG_OWNER_SETTINGS.equals(currentTag)) {
                bottomNavigation.setSelectedItemId(NAV_SETTINGS_ID);
            }
        } else {
            // Fragment không tồn tại, tải mặc định
            loadFragment(new MyCarsFragment(), TAG_MY_CARS);
        }
    }
    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Tiêu đề sẽ được set trong loadFragment
    }
    private void updateToolbarTitle(String tag) {
        String title = "";
        if (TAG_MY_CARS.equals(tag)) {
            title = getString(R.string.title_my_cars);
        } else if (TAG_MY_RENTALS.equals(tag)) {
            title = getString(R.string.title_my_rentals);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}