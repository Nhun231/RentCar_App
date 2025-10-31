package com.anhbhn.rentcar.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;
import com.anhbhn.rentcar.data.repository.auth.AuthRepository;
import com.anhbhn.rentcar.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        authRepository = new AuthRepository();

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show());
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

                            //  Save token to SharedPreferences
                            TokenManager.saveToken(LoginActivity.this, token);

                            runOnUiThread(() -> Toast.makeText(LoginActivity.this,
                                    "Welcome " + fullName,
                                    Toast.LENGTH_SHORT).show());

                            // TODO: Navigate to next screen
                            // startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            // finish();
                        } else {
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, res.message, Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}
