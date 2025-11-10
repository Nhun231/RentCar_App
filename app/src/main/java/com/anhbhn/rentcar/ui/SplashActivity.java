package com.anhbhn.rentcar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.anhbhn.rentcar.MainActivity;
import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.ui.auth.LoginActivity;
import com.anhbhn.rentcar.ui.car.search.SearchCarActivity;
import com.anhbhn.rentcar.utils.TokenManager;

import java.net.CookieManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000; // 1 second delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Restore cookies on app startup before checking token
        restoreCookiesFromStorage();

        // Check token and navigate after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkTokenAndNavigate();
            finish();
        }, SPLASH_DELAY);
    }

    private void restoreCookiesFromStorage() {
        // Restore cookies from SharedPreferences to CookieManager
        CookieManager cookieManager = ApiClient.getCookieManager();
        TokenManager.restoreCookies(this, cookieManager);
    }

    private void checkTokenAndNavigate() {
        String token = TokenManager.getToken(this);
        String userRole = TokenManager.getUserRole(this);

        Intent intent;

        if (token == null || token.isEmpty()) {
            // No token -> go to Login
            intent = new Intent(this, LoginActivity.class);
        } else {
            // Token exists -> check role and navigate to appropriate screen
            if ("CUSTOMER".equals(userRole)) {
                // Customer -> Customer Main Activity with bottom navigation
                intent = new Intent(this, com.anhbhn.rentcar.ui.customer.CustomerMainActivity.class);
            } else if ("CAR_OWNER".equals(userRole)) {
                // Car Owner -> Add Car screen (MainActivity)
                intent = new Intent(this, MainActivity.class);
            } else {
                // Unknown role or no role -> default to Login
                intent = new Intent(this, LoginActivity.class);
            }
        }

        startActivity(intent);
    }
}

