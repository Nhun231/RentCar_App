package com.anhbhn.rentcar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import com.anhbhn.rentcar.ui.auth.LoginActivity
import com.anhbhn.rentcar.utils.TokenManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check token and role here
        val token = TokenManager.getToken(this)
        val userRole = TokenManager.getUserRole(this)
        
        if (token.isNullOrEmpty()) {
            // No token → go to Login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Verify this is CAR_OWNER (should be checked in SplashActivity, but safety check)
        if (!"CAR_OWNER".equals(userRole)) {
            // Not car owner → redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Car Owner -> Add Car flow
        setContentView(R.layout.activity_main)
        
        // Setup navigation for add car flow (nav_add_car)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_add_car) as? NavHostFragment
        if (navHostFragment == null) {
            // Initialize if not already initialized
            val fragmentContainer = findViewById<FragmentContainerView>(R.id.nav_host_fragment_add_car)
            if (fragmentContainer != null) {
                // FragmentContainerView will auto-initialize with nav_add_car graph
            }
        }
    }
}
