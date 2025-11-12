package com.anhbhn.rentcar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.repository.auth.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import es.dmoral.toasty.Toasty;

import com.anhbhn.rentcar.data.dto.response.ApiResponse;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText inputForgotEmail;
    Button btnSendLink;
    TextView linkBackToLogin;
    AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        authRepository = new AuthRepository(this);

        setupListeners();
    }

    private void initializeViews() {
        inputForgotEmail = findViewById(R.id.inputForgotEmail);
        btnSendLink = findViewById(R.id.btnSendLink);
        linkBackToLogin = findViewById(R.id.linkBackToLogin);
    }

    private void setupListeners() {
        btnSendLink.setOnClickListener(v -> sendPasswordResetRequest());
        linkBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // 🎯 Logic Gửi Yêu cầu Reset Password ĐÃ CẬP NHẬT
    private void sendPasswordResetRequest() {
        String email = inputForgotEmail.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.", false);
            inputForgotEmail.requestFocus();
            return;
        }

        btnSendLink.setEnabled(false);
        showToast("Sending reset link...", true);

        // ⚠️ Thay đổi: Gọi phương thức sendForgotPasswordEmail và sử dụng ApiResponse<String> trong Callback
        authRepository.sendForgotPasswordEmail(email).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                btnSendLink.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> res = response.body();

                    // Giả định API trả về code = 1000 cho thành công
                    if (res.code == 1000) {
                        showToast(res.message, true);

                        // Chuyển về màn hình Login sau khi gửi thành công
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Xử lý lỗi logic từ Server
                        String errorMessage = res.message != null ? res.message : "Failed to send link.";
                        showToast(errorMessage, false);
                    }
                } else {
                    // Xử lý lỗi HTTP (ví dụ: 404, 500) hoặc response body null
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                btnSendLink.setEnabled(true);
                String errorMessage = getErrorMessage(t);
                showToast(errorMessage, false);
            }
        });
    }
    private void handleErrorResponse(Response<ApiResponse<String>> response) {
        try {
            if (response.errorBody() != null) {
                showToast("Server error (" + response.code() + "). Please check email or try again.", false);
            } else {
                showToast("Server error (" + response.code() + "). Please try again.", false);
            }
        } catch (Exception e) {
            showToast("An unexpected error occurred during API communication.", false);
        }
    }

    private String getErrorMessage(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "Cannot connect to server. Please check your connection.";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "Request timeout. Please try again.";
        } else {
            return "An error occurred. Please try again.";
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