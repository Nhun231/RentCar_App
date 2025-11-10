package com.anhbhn.rentcar.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.repository.auth.AuthRepository;
import com.anhbhn.rentcar.ui.auth.LoginActivity;
import com.anhbhn.rentcar.utils.TokenManager;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {
    
    private LinearLayout layoutProfile;
    private LinearLayout layoutWallet;
    private LinearLayout layoutAbout;
    private LinearLayout layoutHelp;
    private Button btnLogout;
    private AuthRepository authRepository;

    public void initializeRepository(Context context) {
        if (authRepository == null) {
            authRepository = new AuthRepository(context);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupClickListeners();
    }
    
    private void initializeViews(View view) {
        layoutProfile = view.findViewById(R.id.layoutProfile);
        layoutWallet = view.findViewById(R.id.layoutWallet);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutHelp = view.findViewById(R.id.layoutHelp);
        btnLogout = view.findViewById(R.id.btnLogout);
    }
    
    private void setupClickListeners() {
        layoutProfile.setOnClickListener(v -> {
            // TODO: Navigate to edit profile
            Toasty.info(requireContext(), "Edit Profile - Coming soon", Toast.LENGTH_SHORT).show();
        });
        
        layoutWallet.setOnClickListener(v -> {
            // TODO: Navigate to wallet
            Toasty.info(requireContext(), "My Wallet - Coming soon", Toast.LENGTH_SHORT).show();
        });
        
        layoutAbout.setOnClickListener(v -> {
            // TODO: Show about dialog
            Toasty.info(requireContext(), "About - Coming soon", Toast.LENGTH_SHORT).show();
        });
        
        layoutHelp.setOnClickListener(v -> {
            // TODO: Navigate to help
            Toasty.info(requireContext(), "Help & Support - Coming soon", Toast.LENGTH_SHORT).show();
        });
        
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // ✅ Gọi hàm logout, truyền Fragment này làm callback
                    logout(requireContext(), new LogoutCallback() {
                        @Override
                        public void onLogoutSuccess() {
                            Toasty.success(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                            // Chuyển hướng
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }

                        @Override
                        public void onLogoutError(String errorMessage) {
                            Toasty.error(requireContext(), errorMessage + ". Please restart the app.", Toast.LENGTH_LONG).show();
                            // Dù có lỗi mạng, chúng ta vẫn đã xóa token local và chuyển hướng
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
    public void logout(Context context, LogoutCallback callback) {
        initializeRepository(context);

        authRepository.logout().enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, @NonNull Response<ApiResponse<String>> response) {

                performLocalLogout(context, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                // Lỗi mạng/Timeout: Vẫn xóa token local để không bị kẹt
                callback.onLogoutError("Network error during logout.");
                performLocalLogout(context, callback);
            }
        });
    }

    private void performLocalLogout(Context context, LogoutCallback callback) {
        TokenManager.clearToken(context);
        callback.onLogoutSuccess();
    }

    public interface LogoutCallback {
        void onLogoutSuccess();
        void onLogoutError(String errorMessage);
    }
}

