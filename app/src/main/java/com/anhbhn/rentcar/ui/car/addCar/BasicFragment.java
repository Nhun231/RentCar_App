package com.anhbhn.rentcar.ui.car.addCar;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.databinding.FragmentAddCarBasicBinding;

import java.util.List;
import java.util.Objects;

public class BasicFragment extends Fragment {

    private FragmentAddCarBasicBinding binding;
    private AddCarViewModel addCarViewModel;

    // Giữ các URI đã chọn trong Fragment (URI file sẽ được cập nhật bởi ActivityResultLauncher)
    private String registrationUri = "";
    private String inspectionUri = "";
    private String insuranceUri = "";
    private ActivityResultLauncher<String> registrationPaperPicker;
    private ActivityResultLauncher<String> inspectionCertificatePicker;
    private ActivityResultLauncher<String> insurancePicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addCarViewModel = new ViewModelProvider(requireActivity()).get(AddCarViewModel.class);
        setupFilePickers();
    }

    // <--- PHƯƠNG THỨC ONCREATEVIEW ĐÃ THÊM VÀ HOÀN THIỆN --->
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Khởi tạo View Binding
        binding = FragmentAddCarBasicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    // <--- END ONCREATEVIEW --->

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo và tải dữ liệu Dropdown từ ViewModel
        setupDropdownsAndObservers();
        setDocumentTitles();
        loadSavedData();
        setupButtons();
    }
    private void setupFilePickers() {
        // MIME types cho tài liệu
        final String fileMimeTypes = "*/*"; // Cho phép tất cả các loại file để chọn dễ dàng

        // 1. Giấy đăng ký
        registrationPaperPicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        setRegistrationUri(uri.toString());
                    }
                });

        // 2. Giấy kiểm định
        inspectionCertificatePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        setInspectionUri(uri.toString());
                    }
                });

        // 3. Bảo hiểm
        insurancePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        setInsuranceUri(uri.toString());
                    }
                });
    }

    // --- LOGIC DROPDOWN VÀ CACHING ---

    private void setupDropdownsAndObservers() {
        // BƯỚC 1: Khởi tạo dữ liệu từ JSON (Chỉ chạy nếu dữ liệu chưa có)
        addCarViewModel.initializeCarOptions(requireContext());

        // BƯỚC 2: Quan sát và thiết lập Dropdown (Brands, Colors)
        addCarViewModel.getAvailableBrands().observe(getViewLifecycleOwner(), this::setupBrandDropdown);
        addCarViewModel.getAvailableColors().observe(getViewLifecycleOwner(), this::setupColorDropdown);

        // (Model sẽ được thiết lập thông qua Listener của Brand)
    }

    private void setupBrandDropdown(List<String> brands) {
        // Thiết lập Adapter cho Brand
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, brands);
        binding.inputBrand.setAdapter(adapter);

        // Logic phụ thuộc: Lọc Model theo Brand
        binding.inputBrand.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBrand = (String) parent.getItemAtPosition(position);

            // Cập nhật lại Model (Lấy từ cache của ViewModel)
            List<String> models = addCarViewModel.getFilteredModels(selectedBrand);
            setupModelDropdown(models);

            // Reset Model sau khi chọn Brand mới
            binding.inputModel.setText("");
        });
    }

    private void setupModelDropdown(List<String> models) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, models);
        binding.inputModel.setAdapter(adapter);
        // Bật hoặc tắt Model dựa trên việc có Models hay không
        binding.inputModel.setEnabled(!models.isEmpty());
    }

    private void setupColorDropdown(List<String> colors) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, colors);
        binding.inputColor.setAdapter(adapter);
    }

    // --- CÁC HÀM CẬP NHẬT URI FILE (ĐƯỢC GỌI TỪ BÊN NGOÀI) ---
    private void setDocumentTitles() {
        // Cần các string resource như label_registration_paper
        binding.docRegistration.tvDocumentTitle.setText(getString(R.string.label_registration_paper));
        binding.docInspection.tvDocumentTitle.setText(getString(R.string.label_certificate_of_inspection));
        binding.docInsurance.tvDocumentTitle.setText(getString(R.string.label_insurance));
    }

    public void setRegistrationUri(String uriString) {
        this.registrationUri = uriString;
        if (binding != null && uriString != null && !uriString.isEmpty()) {
            // Cập nhật UI hiển thị tên file
            binding.docRegistration.tvSelectedFileName.setText(getString(R.string.file_selected_placeholder, getFileName(Uri.parse(uriString))));
        } else if (binding != null) {
            binding.docRegistration.tvSelectedFileName.setText(getString(R.string.file_selected_placeholder, ""));
        }
    }

    public void setInspectionUri(String uriString) {
        this.inspectionUri = uriString;
        if (binding != null && uriString != null && !uriString.isEmpty()) {
            binding.docInspection.tvSelectedFileName.setText(getString(R.string.file_selected_placeholder, getFileName(Uri.parse(uriString))));
        } else if (binding != null) {
            binding.docInspection.tvSelectedFileName.setText(getString(R.string.file_selected_placeholder, ""));
        }
    }

    public void setInsuranceUri(String uriString) {
        this.insuranceUri = uriString;
        if (binding != null && uriString != null && !uriString.isEmpty()) {
            binding.docInsurance.tvSelectedFileName.setText(getString(R.string.file_selected_placeholder, getFileName(Uri.parse(uriString))));
        } else if (binding != null) {
            binding.docInsurance.tvSelectedFileName.setText(getString(R.string.file_selected_placeholder, ""));
        }
    }

    // --- LOGIC TẢI DỮ LIỆU ĐÃ LƯU (Đã có sẵn) ---

    private void loadSavedData() {
        addCarViewModel.getRegistrationData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                // ... (Logic tải dữ liệu đã có) ...
                if (data.licensePlate != null) binding.inputLicensePlate.setText(data.licensePlate);
                if (data.brand != null) binding.inputBrand.setText(data.brand);
                if (data.model != null) binding.inputModel.setText(data.model);
                if (data.color != null) binding.inputColor.setText(data.color);

                if (data.productionYear != null) binding.inputProductionYear.setText(String.valueOf(data.productionYear));
                if (data.numberOfSeats != null) binding.inputNumberOfSeats.setText(String.valueOf(data.numberOfSeats));

                if (data.isAutomatic != null && data.isAutomatic) binding.radioAutomatic.setChecked(true);
                if (data.isGasoline != null && data.isGasoline) binding.radioGasoline.setChecked(true);

                if (data.registrationPaperUri != null) this.registrationUri = data.registrationPaperUri;
                if (data.certificateOfInspectionUri != null) this.inspectionUri = data.certificateOfInspectionUri;
                if (data.insuranceUri != null) this.insuranceUri = data.insuranceUri;
                // ... (Logic hiển thị tên file UI khi có URI)
            }
        });
    }

    // --- LOGIC NÚT BẤM VÀ XÁC THỰC (Đã có sẵn) ---

    private void setupButtons() {
        // Nút NEXT và CANCEL
        binding.btnNextBasic.setOnClickListener(v -> {
            if (!validateAndProcessInputs()) {
                return;
            }
            addCarViewModel.setStep(2);
            Navigation.findNavController(v).navigate(R.id.action_basicFragment_to_detailsFragment);
        });

        binding.btnCancel.setOnClickListener(v -> {
            // Hành động CANCEL
            // Navigation.findNavController(v).navigate(R.id.action_global_to_myCarsScreen);
        });

        // GÁN LISTENER CHO NÚT CHỌN FILE (ĐÃ SỬA LỖI LOGIC)
        binding.docRegistration.btnSelectFile.setOnClickListener(v -> {
            registrationPaperPicker.launch("*/*");
        });

        binding.docInspection.btnSelectFile.setOnClickListener(v -> {
            inspectionCertificatePicker.launch("*/*");
        });

        binding.docInsurance.btnSelectFile.setOnClickListener(v -> {
            insurancePicker.launch("*/*");
        });
    }

    /**
     * Lấy dữ liệu từ UI, xác thực và cập nhật vào ViewModel.
     * Cần đảm bảo các trường RadioButton được nhóm trong RadioGroup để việc kiểm tra logic hơn.
     */
    private boolean validateAndProcessInputs() {
        try {
            // A. Lấy dữ liệu từ UI
            String lp = binding.inputLicensePlate.getText().toString().trim();
            String brand = binding.inputBrand.getText().toString().trim();
            String model = binding.inputModel.getText().toString().trim();
            String color = binding.inputColor.getText().toString().trim();

            int year = Integer.parseInt(binding.inputProductionYear.getText().toString());
            int seats = Integer.parseInt(binding.inputNumberOfSeats.getText().toString());

            // Xử lý Radio Button: Kiểm tra RadioGroup
            boolean isAutomatic = binding.rgTransmission.getCheckedRadioButtonId() == binding.radioAutomatic.getId();
            boolean isManual = binding.rgTransmission.getCheckedRadioButtonId() == binding.radioManual.getId();

            boolean isGasoline = binding.rgFuel.getCheckedRadioButtonId() == binding.radioGasoline.getId();
            boolean isDiesel = binding.rgFuel.getCheckedRadioButtonId() == binding.radioDiesel.getId();


            // B. Xác thực cơ bản
            if (lp.isEmpty() || brand.isEmpty() || model.isEmpty() || color.isEmpty() ||
                    (!isAutomatic && !isManual) || (!isGasoline && !isDiesel) || // Kiểm tra phải chọn hộp số và nhiên liệu
                    registrationUri.isEmpty() || inspectionUri.isEmpty() || insuranceUri.isEmpty()) {

                Toast.makeText(getContext(), "Vui lòng điền và chọn đủ thông tin bắt buộc, bao gồm cả tài liệu.", Toast.LENGTH_LONG).show();
                return false;
            }

            // C. Cập nhật ViewModel
            addCarViewModel.updateBasicData(lp, brand, model, color, year, seats, isAutomatic, isGasoline,
                    registrationUri, inspectionUri, insuranceUri);
            return true;

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Năm sản xuất và Số chỗ ngồi phải là số nguyên.", Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception e) {
            Toast.makeText(getContext(), "Có lỗi xảy ra khi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            // Lấy tên file từ ContentResolver
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "File đã chọn";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}