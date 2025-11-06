package com.anhbhn.rentcar.ui.car.addCar;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.databinding.FragmentAddCarDetailsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Để tải ảnh từ URI
import com.bumptech.glide.Glide;

public class DetailsFragment extends Fragment {
    private FragmentAddCarDetailsBinding binding;
    private AddCarViewModel addCarViewModel;

    // --- URI Ảnh Xe ---
    private String frontUri = "";
    private String backUri = "";
    private String leftUri = "";
    private String rightUri = "";
    private ActivityResultLauncher<String> frontImagePicker;
    private ActivityResultLauncher<String> backImagePicker;
    private ActivityResultLauncher<String> leftImagePicker;
    private ActivityResultLauncher<String> rightImagePicker;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addCarViewModel = new ViewModelProvider(requireActivity()).get(AddCarViewModel.class);

        // Khởi tạo tất cả 4 Launchers
        setupImagePickers();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddCarDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cần khởi tạo dữ liệu Dropdown
        setupAddressDropdowns();
        setImageTitles();
        setupImageUploadListeners(); // Thiết lập listener cho các nút tải/xóa ảnh
        setupActionButtons();

        // Load dữ liệu đã lưu sau khi mọi thứ đã được thiết lập (quan trọng)
        loadSavedData();
    }
    private void setImageTitles() {

        if (binding.imgFront.tvImageTitle != null) {
            binding.imgFront.tvImageTitle.setText(getString(R.string.image_front));
        }
        if (binding.imgBack.tvImageTitle != null) {
            binding.imgBack.tvImageTitle.setText(getString(R.string.image_back));
        }
        if (binding.imgLeft.tvImageTitle != null) {
            binding.imgLeft.tvImageTitle.setText(getString(R.string.image_left));
        }
        if (binding.imgRight.tvImageTitle != null) {
            binding.imgRight.tvImageTitle.setText(getString(R.string.image_right));
        }
    }

    private void setupImagePickers() {
        // Hàm chung để xử lý callback và cập nhật Fragment state/UI
        ActivityResultContracts.GetContent getContent = new ActivityResultContracts.GetContent();

        frontImagePicker = registerForActivityResult(getContent, uri -> handleImageResult(uri, "front"));
        backImagePicker = registerForActivityResult(getContent, uri -> handleImageResult(uri, "back"));
        leftImagePicker = registerForActivityResult(getContent, uri -> handleImageResult(uri, "left"));
        rightImagePicker = registerForActivityResult(getContent, uri -> handleImageResult(uri, "right"));
    }
    private void handleImageResult(Uri uri, String type) {
        if (uri == null) return;

        String uriString = uri.toString();
        ImageView imageView;
        View removeButton; // <-- KHAI BÁO BIẾN CHO NÚT XÓA

        switch (type) {
            case "front":
                this.frontUri = uriString;
                imageView = binding.imgFront.imgPreview;
                removeButton = binding.imgFront.btnRemoveFile; // <-- THÊM NÚT XÓA
                break;
            case "back":
                this.backUri = uriString;
                imageView = binding.imgBack.imgPreview;
                removeButton = binding.imgBack.btnRemoveFile; // <-- THÊM NÚT XÓA
                break;
            case "left":
                this.leftUri = uriString;
                imageView = binding.imgLeft.imgPreview;
                removeButton = binding.imgLeft.btnRemoveFile; // <-- THÊM NÚT XÓA
                break;
            case "right":
            default:
                this.rightUri = uriString;
                imageView = binding.imgRight.imgPreview;
                removeButton = binding.imgRight.btnRemoveFile; // <-- THÊM NÚT XÓA
                break;
        }
        // GỌI HÀM CẬP NHẬT MỚI
        updateImageUI(imageView, removeButton, uri);
        Toast.makeText(getContext(), "Đã chọn ảnh " + type, Toast.LENGTH_SHORT).show();
    }
    private void updateImageUI(ImageView imageView, View removeButton, @Nullable Uri uri) {
        if (uri != null) {
            Glide.with(requireContext()).load(uri).into(imageView);
            removeButton.setVisibility(View.VISIBLE); // Hiển thị nút xóa
        } else {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            removeButton.setVisibility(View.GONE); // Ẩn nút xóa
        }
    }
    /**
     * Thiết lập các ClickListener cho các nút tải lên và xóa ảnh.
     */
    private void setupImageUploadListeners() {
        // Front Image
        binding.imgFront.btnSelectImage.setOnClickListener(v -> frontImagePicker.launch("image/*"));
        binding.imgFront.btnRemoveFile.setOnClickListener(v -> { // <--- SỬ DỤNG ID ĐÚNG
            frontUri = "";
            updateImageUI(binding.imgFront.imgPreview, binding.imgFront.btnRemoveFile, null);
        });

        // Back Image
        binding.imgBack.btnSelectImage.setOnClickListener(v -> backImagePicker.launch("image/*"));
        binding.imgBack.btnRemoveFile.setOnClickListener(v -> { // <--- SỬ DỤNG ID ĐÚNG
            backUri = "";
            updateImageUI(binding.imgBack.imgPreview, binding.imgBack.btnRemoveFile, null);
        });

        // Left Image
        binding.imgLeft.btnSelectImage.setOnClickListener(v -> leftImagePicker.launch("image/*"));
        binding.imgLeft.btnRemoveFile.setOnClickListener(v -> { // <--- SỬ DỤNG ID ĐÚNG
            leftUri = "";
            updateImageUI(binding.imgLeft.imgPreview, binding.imgLeft.btnRemoveFile, null);
        });

        // Right Image
        binding.imgRight.btnSelectImage.setOnClickListener(v -> rightImagePicker.launch("image/*"));
        binding.imgRight.btnRemoveFile.setOnClickListener(v -> { // <--- SỬ DỤNG ID ĐÚNG
            rightUri = "";
            updateImageUI(binding.imgRight.imgPreview, binding.imgRight.btnRemoveFile, null);
        });
    }
    //address
    private void setupAddressDropdowns() {
        // Đảm bảo dữ liệu Address đã được tải
        addCarViewModel.initializeCarOptions(requireContext());

        // 1. Quan sát và thiết lập Dropdown City/Province
        addCarViewModel.getAvailableCities().observe(getViewLifecycleOwner(), this::setupCityDropdown);

        // 2. Xử lý sự kiện chọn City -> District
        binding.selectCityProvince.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);

            // Lấy danh sách District từ ViewModel cache
            List<String> districts = addCarViewModel.getFilteredDistricts(selectedCity);
            setupDistrictDropdown(districts);

            // Reset District và Ward khi City thay đổi
            binding.selectDistrict.setText("");
            binding.selectWard.setText("");
            binding.selectDistrict.clearFocus(); // Ẩn bàn phím ảo
            binding.selectWard.clearFocus();
        });

        // 3. Xử lý sự kiện chọn District -> Ward
        binding.selectDistrict.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = binding.selectCityProvince.getText().toString();
            String selectedDistrict = (String) parent.getItemAtPosition(position);

            // Lấy danh sách Ward từ ViewModel cache
            List<String> wards = addCarViewModel.getFilteredWards(selectedCity, selectedDistrict);
            setupWardDropdown(wards);

            binding.selectWard.setText("");
            binding.selectWard.clearFocus();
        });
    }

    // Các hàm thiết lập Adapter cho từng dropdown (AutoCompleteTextView)
    private void setupCityDropdown(List<String> cities) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, cities);
        binding.selectCityProvince.setAdapter(adapter);
        binding.selectCityProvince.setEnabled(true); // Đảm bảo đã bật sau khi có dữ liệu
    }
    private void setupDistrictDropdown(List<String> districts) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, districts);
        binding.selectDistrict.setAdapter(adapter);
        binding.selectDistrict.setEnabled(true);
    }
    private void setupWardDropdown(List<String> wards) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, wards);
        binding.selectWard.setAdapter(adapter);
        binding.selectWard.setEnabled(true);
    }
    //checkbox
    private List<String> getSelectedFunctions() {
        List<String> functions = new ArrayList<>();

        // Duyệt qua GridLayout/LinearLayout chứa Checkbox (Giả định ID: gridFunctions)
        if (binding.gridFunctions instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) binding.gridFunctions;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox cb = (CheckBox) child;
                    if (cb.isChecked()) {
                        functions.add(cb.getText().toString());
                    }
                }
            }
        }
        return functions;
    }
    private void loadSavedData() {
        addCarViewModel.getRegistrationData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                // Tải Mileage/Consumption
                if (data.mileage != null) binding.inputMileage.setText(String.valueOf(data.mileage));
                if (data.fuelConsumption != null) binding.inputFuelConsumption.setText(String.valueOf(data.fuelConsumption));

                // Tải địa chỉ (AutoCompleteTextViews)
                if (data.addressCityProvince != null) binding.selectCityProvince.setText(data.addressCityProvince, false); // false để tránh kích hoạt listener
                if (data.addressDistrict != null) binding.selectDistrict.setText(data.addressDistrict, false);
                if (data.addressWard != null) binding.selectWard.setText(data.addressWard, false);
                if (data.addressHouseNumberStreet != null) binding.inputFullAddress.setText(data.addressHouseNumberStreet);
                if (data.description != null) binding.inputDescription.setText(data.description);

                // Tải chức năng phụ (Checkbox)
                if (data.selectedFunctions != null && !data.selectedFunctions.isEmpty()) {
                    if (binding.gridFunctions instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) binding.gridFunctions;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            View child = group.getChildAt(i);
                            if (child instanceof CheckBox) {
                                CheckBox cb = (CheckBox) child;
                                if (data.selectedFunctions.contains(cb.getText().toString())) {
                                    cb.setChecked(true);
                                }
                            }
                        }
                    }
                }

                // Cập nhật biến URI ảnh và load ảnh vào ImageView tương ứng
                if (data.carImageFrontUri != null) {
                    frontUri = data.carImageFrontUri;
                    updateImageUI(binding.imgFront.imgPreview, binding.imgFront.btnRemoveFile, Uri.parse(frontUri));
                }
                if (data.carImageBackUri != null) {
                    backUri = data.carImageBackUri;
                    updateImageUI(binding.imgBack.imgPreview, binding.imgBack.btnRemoveFile, Uri.parse(backUri));
                }
                if (data.carImageLeftUri != null) {
                    leftUri = data.carImageLeftUri;
                    updateImageUI(binding.imgLeft.imgPreview, binding.imgLeft.btnRemoveFile, Uri.parse(leftUri));
                }
                if (data.carImageRightUri != null) {
                    rightUri = data.carImageRightUri;
                    updateImageUI(binding.imgRight.imgPreview, binding.imgRight.btnRemoveFile, Uri.parse(rightUri));
                }
            }
        });
    }
    // --- LOGIC NÚT NEXT VÀ XÁC THỰC ---

    private void setupActionButtons() { // Hoặc giữ tên gốc là setupButtons()

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

//                        // Lấy Car ID cần Update
//                        String carId = addCarViewModel.getRegistrationData().getValue().carId;
//
//                        // Gọi hàm Update API cho toàn bộ dữ liệu (cần được định nghĩa trong ViewModel)
//                        addCarViewModel.updateCarDetails(carId, requireContext());

                        Toast.makeText(getContext(), "Đang lưu thay đổi chi tiết xe...", Toast.LENGTH_SHORT).show();
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

                // Thiết lập Listener cho nút NEXT (Chuyển sang Pricing)
                binding.btnNextDetails.setOnClickListener(v -> {
                    if (!validateAndProcessInputs()) {
                        return;
                    }
                    addCarViewModel.setStep(3);
                    Navigation.findNavController(v).navigate(R.id.action_detailsFragment_to_pricingFragment);
                });

                // Thiết lập Listener cho nút BACK (Quay lại Basic)
                binding.btnBackDetails.setOnClickListener(v -> {
                    addCarViewModel.setStep(1);
                    Navigation.findNavController(v).navigate(R.id.action_detailsFragment_to_basicFragment);
                });

                // Thiết lập Listener cho nút CANCEL
                binding.btnCancel.setOnClickListener(v -> {
                    Navigation.findNavController(v).navigate(R.id.action_detailsFragment_to_myCarsActivity);
                });
            }
        });
    }

    private boolean validateAndProcessInputs() {
        try {
            // Lấy dữ liệu từ EditText và AutoCompleteTextView
            String mileageStr = binding.inputMileage.getText().toString().trim();
            String consumptionStr = binding.inputFuelConsumption.getText().toString().trim();

            // Chuyển đổi an toàn sang Float
            // Sử dụng một giá trị mặc định nếu rỗng (hoặc báo lỗi nếu bắt buộc)
            Float mileage = mileageStr.isEmpty() ? null : Float.parseFloat(mileageStr);
            Float consumption = consumptionStr.isEmpty() ? null : Float.parseFloat(consumptionStr);

            String city = binding.selectCityProvince.getText().toString().trim();
            String district = binding.selectDistrict.getText().toString().trim();
            String ward = binding.selectWard.getText().toString().trim();
            String street = binding.inputFullAddress.getText().toString().trim();
            String desc = binding.inputDescription.getText().toString().trim();

            List<String> functions = getSelectedFunctions();

            // Xác thực cơ bản (Kiểm tra các trường bắt buộc)
            if (mileage == null || mileage <= 0) {
                Toast.makeText(getContext(), "Vui lòng nhập số km đã đi hợp lệ.", Toast.LENGTH_LONG).show();
                return false;
            }
            if (city.isEmpty() || district.isEmpty() || ward.isEmpty() || street.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn địa chỉ đầy đủ (Tỉnh/Thành phố, Quận/Huyện, Phường/Xã, Số nhà/Tên đường).", Toast.LENGTH_LONG).show();
                return false;
            }
            if (frontUri.isEmpty() || backUri.isEmpty() || leftUri.isEmpty() || rightUri.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng tải lên cả 4 ảnh xe (Trước, Sau, Trái, Phải).", Toast.LENGTH_LONG).show();
                return false;
            }

            // Cập nhật ViewModel
            addCarViewModel.updateDetailsData(
                    mileage, consumption, desc, city, district, ward, street,
                    functions, frontUri, backUri, leftUri, rightUri
            );
            return true;

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số km đã đi và Mức tiêu thụ nhiên liệu phải là số hợp lệ.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
