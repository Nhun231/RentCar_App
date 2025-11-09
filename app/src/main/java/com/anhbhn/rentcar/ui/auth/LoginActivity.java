package com.anhbhn.rentcar.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.MainActivity;
import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.ui.car.search.SearchCarActivity;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.repository.auth.AuthRepository;
import com.anhbhn.rentcar.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    EditText inputEmail, inputPassword;
    Button btnLogin;
    AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);

        authRepository = new AuthRepository(this);

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please enter email and password", false);
                return;
            }

            LoginRequest request = new LoginRequest(email, password);

            authRepository.login(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse res = response.body();

                        if (res.code == 1000 && res.data != null) {
                            String token = res.data.csrfToken;
                            String fullName = res.data.fullName;
                            String userRole = res.data.userRole;

                            TokenManager.saveToken(LoginActivity.this, token);
                            TokenManager.saveUserRole(LoginActivity.this, userRole);

                            // Extract and save JWT cookies from response headers for persistence
                            extractAndSaveCookies(response);

                            showToast("Welcome " + fullName, true);

                            // Navigate based on user role
                            Intent intent;
                            if ("CUSTOMER".equals(userRole)) {
                                // Customer -> Search Car screen
                                intent = new Intent(LoginActivity.this, SearchCarActivity.class);
                            } else if ("CAR_OWNER".equals(userRole)) {
                                // Car Owner -> Add Car screen (MainActivity with add car flow)
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                            } else {
                                // Default to Search Car for unknown roles
                                intent = new Intent(LoginActivity.this, SearchCarActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            showToast(res.message, false);
                        }
                    } else {
                        showToast("Invalid response from server", false);
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    showToast("Network error: " + t.getMessage(), false);
                }
            });
        });
    }
    private void extractAndSaveCookies(Response<LoginResponse> response) {
        // Extract Set-Cookie headers
        List<String> cookies = response.headers().values("Set-Cookie");
        for (String cookie : cookies) {
            // Parse cookie header: "karental-jwt=TOKEN; Path=/karental; Domain=localhost; Max-Age=3600; ..."
            if (cookie.contains("karental-jwt=") && !cookie.contains("refresh")) {
                // Extract JWT cookie value
                int startIndex = cookie.indexOf("karental-jwt=") + "karental-jwt=".length();
                int endIndex = cookie.indexOf(";", startIndex);
                if (endIndex == -1) endIndex = cookie.length();
                String jwtValue = cookie.substring(startIndex, endIndex).trim();
                TokenManager.saveJwtCookie(this, jwtValue);
            } else if (cookie.contains("karental-jwt-refresh=")) {
                // Extract refresh token cookie value
                int startIndex = cookie.indexOf("karental-jwt-refresh=") + "karental-jwt-refresh=".length();
                int endIndex = cookie.indexOf(";", startIndex);
                if (endIndex == -1) endIndex = cookie.length();
                String refreshValue = cookie.substring(startIndex, endIndex).trim();
                TokenManager.saveRefreshTokenCookie(this, refreshValue);
            }
        }
    }

    private void showToast(String message, boolean success) {
        if (success) {
            Toasty.success(this, message, Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.error(this, message, Toast.LENGTH_SHORT, true).show();
        }
    }
}
