package com.anhbhn.rentcar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.anhbhn.rentcar.ui.auth.LoginActivity
import com.anhbhn.rentcar.databinding.ActivityMainBinding // Giả định dùng View Binding
import com.anhbhn.rentcar.utils.TokenManager

/**
 * MainActivity: Hoạt động như Owner Main Dashboard (chứa Bottom Navigation).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Kiểm tra xác thực (Giống như logic cũ)
        val token = TokenManager.getToken(this)
        val userRole = TokenManager.getUserRole(this)

        if (token.isNullOrEmpty() || !"CAR_OWNER".equals(userRole)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 2. Tải Layout Chủ xe
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Setup Bottom Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_owner) as NavHostFragment

        navController = navHostFragment.navController
        binding.bottomNavigationOwner.post {
            binding.bottomNavigationOwner.setupWithNavController(navController)
        }

        // 4. Khởi tạo màn hình đầu tiên (My Cars)
        // (Nav Graph đã xử lý, không cần gọi thêm)
    }
}