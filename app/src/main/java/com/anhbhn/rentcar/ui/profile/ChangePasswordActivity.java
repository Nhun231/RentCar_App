package com.anhbhn.rentcar.ui.profile; // Nên đặt trong package profile hoặc user

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.request.user.EditPasswordRequest;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.repository.user.UserRepository;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText inputOldPassword, inputNewPassword, inputConfirmPassword;
    private Button btnSavePassword;

    // Sử dụng UserRepository bạn vừa tạo
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // Đảm bảo tên layout đúng

        initializeViews();
        userRepository = new UserRepository(this);
        setupListeners();
    }

    // --- 1. Ánh xạ Views ---
    private void initializeViews() {
        inputOldPassword = findViewById(R.id.inputOldPassword);
        inputNewPassword = findViewById(R.id.inputNewPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
    }

    // --- 2. Thiết lập Listener ---
    private void setupListeners() {
        btnSavePassword.setOnClickListener(v -> {
            if (validateInputs()) {
                changePassword();
            }
        });
    }

    // --- 3. Xác thực đầu vào ---
    private boolean validateInputs() {
        String oldPassword = inputOldPassword.getText().toString().trim();
        String newPassword = inputNewPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

        // Kiểm tra trống
        if (TextUtils.isEmpty(oldPassword)) {
            inputOldPassword.setError("Current password is required");
            return false;
        }
        if (TextUtils.isEmpty(newPassword)) {
            inputNewPassword.setError("New password is required");
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            inputConfirmPassword.setError("Confirmation is required");
            return false;
        }

        // Kiểm tra độ dài và định dạng mật khẩu mới
        // Giả định quy tắc: ít nhất 9 ký tự, có chữ và số
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{9,}$")) {
            inputNewPassword.setError("Password must contain at least one letter, one digit, and at least 9 characters");
            return false;
        }

        // Kiểm tra khớp mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            inputConfirmPassword.setError("New password and confirmation do not match");
            return false;
        }

        // Kiểm tra mật khẩu mới khác mật khẩu cũ
        if (oldPassword.equals(newPassword)) {
            inputNewPassword.setError("New password must be different from current password");
            return false;
        }

        return true;
    }

    // --- 4. Gọi API Đổi Mật khẩu ---
    private void changePassword() {
        String oldPassword = inputOldPassword.getText().toString().trim();
        String newPassword = inputNewPassword.getText().toString().trim();

        // Dùng EditPasswordRequest đã tạo
        EditPasswordRequest request = new EditPasswordRequest(oldPassword, newPassword);

        btnSavePassword.setEnabled(false);
        btnSavePassword.setText("SAVING...");

        userRepository.changePassword(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                btnSavePassword.setEnabled(true);
                btnSavePassword.setText(R.string.save_changes);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();

                    if (apiResponse.code == 1000) {
                        // Thành công
                        showToast("Password changed successfully!", true);
                        finish(); // Đóng màn hình
                    } else {
                        // Xử lý lỗi từ Server (ví dụ: Mật khẩu cũ không đúng)
                        String errorMessage = apiResponse.message != null ? apiResponse.message : "Failed to change password.";
                        showToast(errorMessage, false);
                    }
                } else {
                    // Xử lý lỗi HTTP/Server
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                btnSavePassword.setEnabled(true);
                btnSavePassword.setText(R.string.save_changes);
                showToast("Network error: " + t.getMessage(), false);
            }
        });
    }

    // --- Helper Methods ---

    private void handleApiError(Response<ApiResponse<String>> response) {
        // Cần logic để parse JSON từ response.errorBody() để lấy message lỗi
        String errorMessage = "Failed to change password (HTTP " + response.code() + ").";
        try {
            if (response.errorBody() != null) {
                // Ví dụ: sử dụng Gson để parse errorBody().string()
                String errorBody = response.errorBody().string();
                // ... (Logic parsing error message chi tiết hơn)
                showToast(errorMessage, false);
            }
        } catch (java.io.IOException ignored) {
            showToast(errorMessage, false);
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