package com.anhbhn.rentcar.ui.car.addCar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.databinding.FragmentAddCarPricingBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PricingFragment extends Fragment {
    private FragmentAddCarPricingBinding binding;
    private AddCarViewModel addCarViewModel;
    private List<CheckBox> allCheckboxes;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addCarViewModel = new ViewModelProvider(requireActivity()).get(AddCarViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tên Binding phải khớp với layout XML của Step 3
        binding = FragmentAddCarPricingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeCheckboxes();
        loadSavedData();
        setupActionButtons();
    }
    private void initializeCheckboxes() {
        allCheckboxes = new ArrayList<>();
        if (binding.gridTerms != null) {
            ViewGroup group = (ViewGroup) binding.gridTerms;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof CheckBox) {
                    allCheckboxes.add((CheckBox) child);
                }
            }
        }
    }
    /**
     * Lấy và kết hợp các điều khoản đã chọn từ Checkbox và văn bản tùy chỉnh thành một chuỗi duy nhất,
     * ngăn cách bởi dấu phẩy và khoảng trắng (", ").
     * @return Chuỗi các điều khoản kết hợp.
     */
    private String getCombinedTerms() {
        List<String> selectedTerms = new ArrayList<>();

        if (binding.gridTerms instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) binding.gridTerms;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox cb = (CheckBox) child;
                    if (cb.isChecked()) {
                        selectedTerms.add(cb.getText().toString());
                    }
                }
            }
        }

        String combinedTerms = String.join(", ", selectedTerms);

        // 2. Thêm văn bản tùy chỉnh
        String specifiedText = binding.inputTermsSpecify.getText().toString().trim();

        if (!specifiedText.isEmpty()) {
            if (!combinedTerms.isEmpty()) {
                combinedTerms += ", " + specifiedText;
            } else {
                combinedTerms = specifiedText;
            }
        }

        return combinedTerms;
    }
    private void loadSavedData() {
        addCarViewModel.getRegistrationData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                // Tải giá
                if (data.basePrice != null) binding.inputBasePrice.setText(String.valueOf(data.basePrice));
                if (data.requiredDeposit != null) binding.inputDeposit.setText(String.valueOf(data.requiredDeposit));

                // Khôi phục điều khoản sử dụng
                if (data.termsOfUseCombined != null) {
                    restoreTermsState(data.termsOfUseCombined); // <--- SỬ DỤNG HÀM MỚI
                }
            }
        });
    }
    private void restoreTermsState(String combinedTerms) {
        if (combinedTerms == null || combinedTerms.isEmpty()) return;

        // 1. Chuẩn hóa chuỗi và tách thành List các điều khoản
        // Sử dụng regex để xử lý khoảng trắng thừa xung quanh dấu phẩy: (No smoking,No pet)
        List<String> savedTerms = Arrays.asList(combinedTerms.split("\\s*,\\s*"));

        // 2. Lấy danh sách văn bản của các checkbox tiêu chuẩn
        List<String> standardTermLabels = new ArrayList<>();
        for (CheckBox cb : allCheckboxes) {
            standardTermLabels.add(cb.getText().toString());
            cb.setChecked(false); // Reset trước khi khôi phục
        }

        StringBuilder remainingText = new StringBuilder();

        // 3. Khôi phục Checkbox và xác định văn bản tùy chỉnh
        for (String term : savedTerms) {
            if (standardTermLabels.contains(term)) {
                // Đây là một điều khoản tiêu chuẩn -> Khôi phục Checkbox
                for (CheckBox cb : allCheckboxes) {
                    if (cb.getText().toString().equals(term)) {
                        cb.setChecked(true);
                        break;
                    }
                }
            } else {
                // Đây là văn bản tùy chỉnh (hoặc một phần của nó)
                if (remainingText.length() > 0) {
                    remainingText.append(", ");
                }
                remainingText.append(term);
            }
        }

        // 4. Khôi phục trường nhập liệu tùy chỉnh
        binding.inputTermsSpecify.setText(remainingText.toString());
    }
    private void setupActionButtons() {

        // --- LOGIC CHUYỂN ĐỔI MODE SỬ DỤNG LIVEDATA ---

        // Giả định addCarViewModel đã được khởi tạo
        addCarViewModel.getIsEditMode().observe(getViewLifecycleOwner(), isEditMode -> {

            if (isEditMode) {
                // 1. CHẾ ĐỘ DETAIL/EDIT (Nút SAVE)

                // Ẩn Container chứa BACK/NEXT/CANCEL
                // (Bạn cần thêm ID cho container này trong XML)
                if (binding.addCarActions != null) {
                    binding.addCarActions.setVisibility(View.GONE);
                }

                // Hiển thị nút SAVE
                if (binding.btnSaveDetail != null) {
                    binding.btnSaveDetail.setVisibility(View.VISIBLE);

                    binding.btnSaveDetail.setOnClickListener(v -> {
                        if (!validateAndProcessInputs()) {
                            return;
                        }

//                        // Lấy Car ID cần Update (cần lưu trong CarRegistrationData)
//                        String carId = addCarViewModel.getRegistrationData().getValue().carId;
//
//                        // Gọi hàm Update API cho toàn bộ dữ liệu
//                        addCarViewModel.updateCarDetails(carId, requireContext());

                        Toast.makeText(getContext(), "Đang lưu thay đổi giá và điều khoản...", Toast.LENGTH_SHORT).show();
                    });
                }

            } else {
                // 2. CHẾ ĐỘ ADD CAR (Nút BACK/NEXT/CANCEL)

                // Hiển thị Container chứa BACK/NEXT/CANCEL
                if (binding.addCarActions != null) {
                    binding.addCarActions.setVisibility(View.VISIBLE);
                }

                // Ẩn nút SAVE
                if (binding.btnSaveDetail != null) {
                    binding.btnSaveDetail.setVisibility(View.GONE);
                }

                // Thiết lập Listener cho nút NEXT (Chuyển sang Finish - Step 4)
                binding.btnNextPricing.setOnClickListener(v -> {
                    if (!validateAndProcessInputs()) {
                        return;
                    }
                    addCarViewModel.setStep(4);
                    Navigation.findNavController(v).navigate(R.id.action_pricingFragment_to_finishFragment);
                });

                // Thiết lập Listener cho nút BACK (Quay lại Details - Step 2)
                binding.btnBackPricing.setOnClickListener(v -> {
                    addCarViewModel.setStep(2);
                    Navigation.findNavController(v).navigate(R.id.action_pricingFragment_to_detailsFragment);
                });

                // Thiết lập Listener cho nút CANCEL
                binding.btnCancel.setOnClickListener(v -> {
                    Navigation.findNavController(v).navigate(R.id.action_pricingFragment_to_myCarsActivity);
                });
            }
        });
    }

    private boolean validateAndProcessInputs() {
        try {
            // Lấy dữ liệu
            String basePriceStr = binding.inputBasePrice.getText().toString().trim();
            String depositStr = binding.inputDeposit.getText().toString().trim();

            // Xác thực và chuyển đổi sang Long
            if (basePriceStr.isEmpty() || depositStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập giá thuê và giá đặt cọc.", Toast.LENGTH_LONG).show();
                return false;
            }

            Long basePrice = Long.parseLong(basePriceStr);
            Long deposit = Long.parseLong(depositStr);

            if (basePrice <= 0 || deposit <= 0) {
                Toast.makeText(getContext(), "Giá thuê và đặt cọc phải lớn hơn 0.", Toast.LENGTH_LONG).show();
                return false;
            }

            String combinedTerms = getCombinedTerms();

            // Cập nhật ViewModel
            addCarViewModel.updatePricingData(basePrice, deposit, combinedTerms);
            return true;

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Giá trị phải là số nguyên hợp lệ.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
