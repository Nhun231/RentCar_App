package com.anhbhn.rentcar.ui.car.addCar;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Cần thiết để hiển thị thông tin bổ sung
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.databinding.FragmentAddCarFinishBinding;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class FinishFragment extends Fragment {

    private FragmentAddCarFinishBinding binding;
    private AddCarViewModel addCarViewModel;

    private static final Locale LOCALE_VIETNAM = new Locale("vi", "VN");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addCarViewModel = new ViewModelProvider(requireActivity()).get(AddCarViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddCarFinishBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Quan sát ViewModel để nạp dữ liệu
        addCarViewModel.getRegistrationData().observe(getViewLifecycleOwner(), this::displayCarDetails);

        // 2. Thiết lập Listener cho nút SUBMIT và BACK
        setupActionButtons();
        setupSubmissionObservers();
    }
    private void displayCarDetails(CarRegistrationData data) {
        if (data == null) return;

        // --- 1. Basic Info (Brand + Model) ---
        String carName = (data.brand != null ? data.brand : "") + " " +
                (data.model != null ? data.model : "");
        binding.textCarName.setText(carName.trim());

        // --- 2. Price ---
        String priceText = formatPrice(data.basePrice);
        binding.textPrice.setText(priceText);

        // --- 3. Location ---
        String locationText = combineAddress(data);
        binding.textLocations.setText(locationText);

        // --- 4. Status và Rides (Giữ nguyên tĩnh cho preview) ---
        binding.textStatus.setText("Not Verified");
        binding.textRidesCount.setText("0");
        binding.textRatings.setText("☆☆☆☆☆");

        // --- 5. Image Preview (Front) ---
        if (data.carImageFrontUri != null && !data.carImageFrontUri.isEmpty()) {
            Glide.with(requireContext())
                    .load(Uri.parse(data.carImageFrontUri))
                    .into(binding.imageCarPreview);
        } else {
            binding.imageCarPreview.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    private String combineAddress(CarRegistrationData data) {
        // Thứ tự: City, District, Ward, Street (phải khớp với yêu cầu backend)
        List<String> parts = Arrays.asList(
                data.addressCityProvince,
                data.addressDistrict,
                data.addressWard,
                data.addressHouseNumberStreet
        );
        return parts.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.joining(", "));
    }

    private String formatPrice(Long price) {
        if (price == null || price == 0) return "N/A";
        NumberFormat formatter = NumberFormat.getInstance(LOCALE_VIETNAM);
        String formattedPrice = formatter.format(price);
        return formattedPrice + "/ngày";
    }

    // --- XỬ LÝ NÚT BẤM (Giữ nguyên) ---
    private void setupActionButtons() {
        // Nút BACK: Quay lại Pricing (Step 3)
        binding.btnBackFinish.setOnClickListener(v -> {
            addCarViewModel.setStep(3);
            // Điều hướng về Step 3
            Navigation.findNavController(v).navigate(R.id.action_finishFragment_to_pricingFragment);
        });

        // Nút SUBMIT: Gửi dữ liệu
        binding.btnSubmitFinish.setOnClickListener(v -> {
            // Vô hiệu hóa nút và thông báo đang gửi
            v.setEnabled(false);
            Toast.makeText(getContext(), "Đang gửi dữ liệu xe...", Toast.LENGTH_SHORT).show();

            // Gọi hàm submit API trong ViewModel
            addCarViewModel.submitCarData(requireContext());
        });
    }
    private void setupSubmissionObservers() {
        // Quan sát trạng thái thành công
        addCarViewModel.submitSuccess.observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null && isSuccess) {
                // 1. THÀNH CÔNG: Hiển thị Toast và điều hướng
                Toast.makeText(getContext(), "🎉 Thêm xe thành công! Xe đang chờ phê duyệt.", Toast.LENGTH_LONG).show();

                // 2. Điều hướng về màn hình chính (hoặc màn hình quản lý xe)
                // LƯU Ý: Đây là nơi bạn sẽ finish() AddCarActivity nếu dùng Activity riêng.
                // Nếu đang test trong MainActivity, bạn cần pop Fragment này khỏi stack.
                // Ví dụ: Đóng AddCar flow và về màn hình Home
                // Navigation.findNavController(requireView()).navigate(R.id.action_global_homeScreen);
            }
            // Xóa giá trị LiveData để không kích hoạt lại khi thay đổi cấu hình
            addCarViewModel.submitSuccess.setValue(null);
            binding.btnSubmitFinish.setEnabled(true); // Kích hoạt lại nút (trừ khi bạn đã điều hướng)
        });

        // Quan sát trạng thái thất bại
        addCarViewModel.submitError.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // 1. THẤT BẠI: Hiển thị Toast lỗi
                String displayMessage = "❌ Lỗi: " + errorMessage;
                Toast.makeText(getContext(), displayMessage, Toast.LENGTH_LONG).show();

                // 2. Kích hoạt lại nút SUBMIT
                binding.btnSubmitFinish.setEnabled(true);
            }
            // Xóa giá trị LiveData để không kích hoạt lại
            addCarViewModel.submitError.setValue(null);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}