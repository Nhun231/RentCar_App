package com.anhbhn.rentcar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.request.auth.RegisterRequest;
import com.anhbhn.rentcar.data.dto.response.auth.RegisterResponse;
import com.anhbhn.rentcar.data.repository.auth.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    EditText etEmail, etPhoneNumber, etFullName, etPassword, etConfirmPassword;
    ImageButton btnTogglePassword, btnToggleConfirmPassword;
    RadioGroup rgRole;
    RadioButton rbCustomer, rbCarOwner;
    CheckBox cbTerms;
    Button btnRegister;
    TextView linkLogin;
    AuthRepository authRepository;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupPasswordToggle();
        setupRegisterButton();
        setupLoginLink();

        authRepository = new AuthRepository(this);
    }

    private void setupLoginLink() {
        linkLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etFullName = findViewById(R.id.etFullName);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        rbCustomer = findViewById(R.id.rbCustomer);
        rbCarOwner = findViewById(R.id.rbCarOwner);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        linkLogin = findViewById(R.id.linkLogin);
    }

    private void setupPasswordToggle() {
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                etConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility);
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });
    }

    private void setupRegisterButton() {
        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });
    }

    private boolean validateInputs() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Full Name validation
        if (fullName.isEmpty()) {
            showToast("Please enter your full name", false);
            etFullName.requestFocus();
            return false;
        }
        if (!fullName.matches("^[\\p{L}\\s-]+$")) {
            showToast("Full name can only contain letters, spaces, and hyphens", false);
            etFullName.requestFocus();
            return false;
        }

        // Email validation
        if (email.isEmpty()) {
            showToast("Please enter your email", false);
            etEmail.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address", false);
            etEmail.requestFocus();
            return false;
        }

        // Phone number validation
        if (phoneNumber.isEmpty()) {
            showToast("Please enter your phone number", false);
            etPhoneNumber.requestFocus();
            return false;
        }
        if (!phoneNumber.matches("^0[\\d]{9}$")) {
            showToast("Phone number must be 10 digits starting with 0", false);
            etPhoneNumber.requestFocus();
            return false;
        }

        // Password validation
        if (password.isEmpty()) {
            showToast("Please enter your password", false);
            etPassword.requestFocus();
            return false;
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{9,}$")) {
            showToast("Password must contain at least one letter, one digit, and at least 9 characters", false);
            etPassword.requestFocus();
            return false;
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            showToast("Please confirm your password", false);
            etConfirmPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Password and confirm password don't match", false);
            etConfirmPassword.requestFocus();
            return false;
        }

        // Terms validation
        if (!cbTerms.isChecked()) {
            showToast("Please accept the terms and conditions", false);
            return false;
        }

        return true;
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String isCustomer = rbCustomer.isChecked() ? "true" : "false";

        RegisterRequest request = new RegisterRequest(fullName, email, phoneNumber, password, isCustomer);

        btnRegister.setEnabled(false);
        showToast("Registering...", true);

        authRepository.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse res = response.body();

                    if (res.code == 1000 && res.data != null) {
                        showToast("Registration successful! Please check your email to verify your account.", true);
                        navigateToLogin(email);
                    } else {
                        showToast(res.message != null ? res.message : "Registration failed", false);
                    }
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                String errorMessage = getErrorMessage(t);
                showToast(errorMessage, false);
            }
        });
    }

    private void navigateToLogin(String email) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    private void handleErrorResponse(Response<RegisterResponse> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    com.google.gson.JsonObject jsonError = gson.fromJson(errorBody, com.google.gson.JsonObject.class);
                    String errorMessage = jsonError.has("message") ? jsonError.get("message").getAsString() : errorBody;
                    showToast("Registration failed: " + errorMessage, false);
                } catch (Exception e) {
                    showToast("Registration failed", false);
                }
            } else {
                showToast("Registration failed (HTTP " + response.code() + ")", false);
            }
        } catch (Exception e) {
            showToast("Registration failed", false);
        }
    }

    private String getErrorMessage(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "Cannot connect to server. Please check your connection.";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "Request timeout. Please try again.";
        } else if (t instanceof java.io.IOException) {
            return "Network error. Please try again.";
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
