package com.anhbhn.rentcar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.anhbhn.rentcar.ui.auth.LoginActivity
import com.anhbhn.rentcar.ui.car.addCar.AddCarActivity
import com.anhbhn.rentcar.ui.car.myCar.MyCarsActivity
import com.anhbhn.rentcar.ui.theme.RentCarTheme
import com.anhbhn.rentcar.utils.TokenManager   // <--- Make sure this import is added

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check token here
        val token = TokenManager.getToken(this)
        if (token.isNullOrEmpty()) {
            // No token → go to Login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        startActivity(Intent(this, MyCarsActivity::class.java))
        finish()
    }
}
