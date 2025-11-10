package com.anhbhn.rentcar.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.ui.auth.LoginActivity;
import com.anhbhn.rentcar.ui.main.SearchFragment;
import com.anhbhn.rentcar.ui.main.MyBookingFragment;
import com.anhbhn.rentcar.ui.main.SettingsFragment;
import com.anhbhn.rentcar.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMainActivity extends AppCompatActivity {
    
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private static final String TAG_SEARCH = "search";
    private static final String TAG_MY_BOOKING = "my_booking";
    private static final String TAG_SETTINGS = "settings";
    private String currentTag = TAG_SEARCH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check token - should not happen as SplashActivity checks, but safety check
        String token = TokenManager.getToken(this);
        if (token == null || token.isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_customer_main);
        
        fragmentManager = getSupportFragmentManager();
        setupBottomNavigation();
        
        // Check if a specific tab should be selected
        String selectedTab = getIntent().getStringExtra("selectedTab");
        
        // Load default fragment (Search) or selected tab
        if (savedInstanceState == null) {
            if ("my_booking".equals(selectedTab)) {
                loadFragment(new MyBookingFragment(), TAG_MY_BOOKING);
                bottomNavigation.setSelectedItemId(R.id.nav_my_booking);
            } else if ("settings".equals(selectedTab)) {
                loadFragment(new SettingsFragment(), TAG_SETTINGS);
                bottomNavigation.setSelectedItemId(R.id.nav_settings);
            } else {
                loadFragment(new SearchFragment(), TAG_SEARCH);
                bottomNavigation.setSelectedItemId(R.id.nav_search);
            }
        } else {
            // Restore current fragment
            currentTag = savedInstanceState.getString("currentTag", TAG_SEARCH);
            restoreFragment();
        }
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTag", currentTag);
    }
    
    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_search) {
                loadFragment(new SearchFragment(), TAG_SEARCH);
                return true;
            } else if (itemId == R.id.nav_my_booking) {
                loadFragment(new MyBookingFragment(), TAG_MY_BOOKING);
                return true;
            } else if (itemId == R.id.nav_settings) {
                loadFragment(new SettingsFragment(), TAG_SETTINGS);
                return true;
            }
            return false;
        });
        
        // Set default selection
        bottomNavigation.setSelectedItemId(R.id.nav_search);
    }
    
    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Check if fragment already exists
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment != null) {
            // Hide all fragments
            hideAllFragments(transaction);
            // Show the selected fragment
            transaction.show(existingFragment);
        } else {
            // Hide all fragments
            hideAllFragments(transaction);
            // Add new fragment
            transaction.add(R.id.fragment_container, fragment, tag);
        }
        
        currentTag = tag;
        transaction.commit();
    }
    
    private void hideAllFragments(FragmentTransaction transaction) {
        Fragment searchFragment = fragmentManager.findFragmentByTag(TAG_SEARCH);
        Fragment myBookingFragment = fragmentManager.findFragmentByTag(TAG_MY_BOOKING);
        Fragment settingsFragment = fragmentManager.findFragmentByTag(TAG_SETTINGS);
        
        if (searchFragment != null) {
            transaction.hide(searchFragment);
        }
        if (myBookingFragment != null) {
            transaction.hide(myBookingFragment);
        }
        if (settingsFragment != null) {
            transaction.hide(settingsFragment);
        }
    }
    
    private void restoreFragment() {
        Fragment fragment = fragmentManager.findFragmentByTag(currentTag);
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            hideAllFragments(transaction);
            transaction.show(fragment);
            transaction.commit();
            
            // Update bottom navigation selection
            if (TAG_SEARCH.equals(currentTag)) {
                bottomNavigation.setSelectedItemId(R.id.nav_search);
            } else if (TAG_MY_BOOKING.equals(currentTag)) {
                bottomNavigation.setSelectedItemId(R.id.nav_my_booking);
            } else if (TAG_SETTINGS.equals(currentTag)) {
                bottomNavigation.setSelectedItemId(R.id.nav_settings);
            }
        } else {
            // Fragment doesn't exist, load default
            loadFragment(new SearchFragment(), TAG_SEARCH);
        }
    }
}

