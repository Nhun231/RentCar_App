package com.anhbhn.rentcar.ui.booking.rentalDetails;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.databinding.FragmentRentalInformationBinding;
import com.bumptech.glide.Glide;

public class RentalInformationFragment extends Fragment {

    private FragmentRentalInformationBinding binding;
    private RentalDetailsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRentalInformationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(RentalDetailsViewModel.class);

        viewModel.getBookingDetails().observe(getViewLifecycleOwner(), this::updateUI);
    }

    /**
     * Cập nhật giao diện với dữ liệu chi tiết booking.
     */
    private void updateUI(BookingResponse.Data details) {
        if (details == null) return;

        // --- 1. Driver Info ---
        binding.tvDriverName.setText(details.getDriverFullName());
        binding.tvDriverDob.setText(details.getDriverDob());
        binding.tvDriverPhone.setText(details.getDriverPhoneNumber());
        binding.tvDriverEmail.setText(details.getDriverEmail());
        binding.tvDriverNationalId.setText(details.getDriverNationalId());

        // --- 2. Address ---
        // Gộp địa chỉ theo thứ tự chuẩn (City, District, Ward, Street)
        String fullAddress = String.format("%s, %s, %s, %s",
                details.getDriverCityProvince(),
                details.getDriverDistrict(),
                details.getDriverWard(),
                details.getDriverHouseNumberStreet());
        binding.tvDriverAddress.setText(fullAddress);

        // --- 3. Driving License Image ---
        if (details.getDriverDrivingLicenseUrl() != null) {
            Glide.with(this)
                    .load(details.getDriverDrivingLicenseUrl())
                    .placeholder(R.drawable.ic_license_placeholder) // Cần có placeholder
                    .into(binding.ivDrivingLicense);
        } else {
            binding.ivDrivingLicense.setImageResource(R.drawable.ic_license_placeholder);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}