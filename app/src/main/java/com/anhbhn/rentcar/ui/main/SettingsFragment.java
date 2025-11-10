package com.anhbhn.rentcar.ui.main;

import android.app.AlertDialog;
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
import com.anhbhn.rentcar.ui.auth.LoginActivity;
import com.anhbhn.rentcar.utils.TokenManager;

import es.dmoral.toasty.Toasty;

public class SettingsFragment extends Fragment {
    
    private LinearLayout layoutProfile;
    private LinearLayout layoutWallet;
    private LinearLayout layoutAbout;
    private LinearLayout layoutHelp;
    private Button btnLogout;

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
                // Clear token
                TokenManager.clearToken(requireContext());
                
                // Navigate to login
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
                
                Toasty.success(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("No", null)
            .show();
    }
}

