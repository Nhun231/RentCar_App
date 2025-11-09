package com.anhbhn.rentcar.ui.booking.rentalDetails;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.databinding.FragmentCarInformationBinding;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CarInformationFragment extends Fragment {

    private FragmentCarInformationBinding binding;
    private RentalDetailsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCarInformationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(RentalDetailsViewModel.class);

        viewModel.getCarDetails().observe(getViewLifecycleOwner(), this::updateCarSpecs);
        viewModel.getBookingDetails().observe(getViewLifecycleOwner(), this::updatePricingAndTerms);
    }

    // --- HÀM HELPER ĐỔ DỮ LIỆU ---

    private void updateCarSpecs(CarResponse.Data carDetails) {
        if (carDetails == null) return;

        // --- 1. Basic Car Specs ---
        binding.tvLicensePlate.setText(carDetails.licensePlate);
        binding.tvCarColor.setText(carDetails.color);
        binding.tvNoOfSeats.setText(String.valueOf(carDetails.numberOfSeats));
        binding.tvBrandName.setText(carDetails.brand);
        binding.tvProductionYear.setText(String.valueOf(carDetails.productionYear));
        binding.tvTransmission.setText(carDetails.isAutomatic ? getString(R.string.radio_automatic) : getString(R.string.radio_manual));
        binding.tvModel.setText(carDetails.model);
        binding.tvFuelType.setText(carDetails.isGasoline ? getString(R.string.radio_gasoline) : getString(R.string.radio_diesel));

        // --- 2. Mileage and Fuel Consumption ---
        binding.tvMileageValue.setText(String.format(Locale.getDefault(), "%.1f km", carDetails.mileage));
        binding.tvFuelConsumptionValue.setText(String.format(Locale.getDefault(), "%.1f L/100KM", carDetails.fuelConsumption));

        // --- 3. Documents (LOGIC NÀY ĐÃ BỊ LOẠI BỎ) ---

        // --- 4. Address & Description ---
        binding.tvCarAddress.setText(carDetails.address);
        binding.tvCarDescription.setText(carDetails.description);

        binding.tvAdditionalFunctions.setText(carDetails.additionalFunction);

        binding.tvTermsOfUse.setText(carDetails.termOfUse);
    }

    private void updatePricingAndTerms(BookingResponse.Data bookingDetails) {
        if (bookingDetails == null) return;

        // --- Pricing ---
        binding.tvBasePriceDetail.setText(getString(R.string.format_price_day, String.valueOf(bookingDetails.getBasePrice())));
        binding.tvDepositDetail.setText(getString(R.string.format_price_day, String.valueOf(bookingDetails.getDeposit())));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}