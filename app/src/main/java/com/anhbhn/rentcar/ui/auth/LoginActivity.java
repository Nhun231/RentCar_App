package com.anhbhn.rentcar.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.repository.auth.AuthRepository;
import com.anhbhn.rentcar.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

                            TokenManager.saveToken(LoginActivity.this, token);

                            showToast("Welcome " + fullName, true);

                            // TODO: Navigate to Home
                            // startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            // finish();
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
    private void showToast(String message, boolean success) {
        if (success) {
            Toasty.success(this, message, Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.error(this, message, Toast.LENGTH_SHORT, true).show();
        }
    }
}
