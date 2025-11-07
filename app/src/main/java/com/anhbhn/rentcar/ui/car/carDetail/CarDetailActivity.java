package com.anhbhn.rentcar.ui.car.carDetail;

import static android.content.ContentValues.TAG;

import static com.anhbhn.rentcar.ui.car.carDetail.CarDetailActivity.ECarStatus.STOPPED;
import static com.anhbhn.rentcar.ui.car.carDetail.CarDetailActivity.ECarStatus.VERIFIED;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.databinding.ActivityCarDetailBinding;
import com.anhbhn.rentcar.databinding.DialogStatusConfirmationBinding;
import com.anhbhn.rentcar.databinding.LayoutCarDetailHeaderBinding;
import com.anhbhn.rentcar.ui.car.CarRegistrationData;
import com.anhbhn.rentcar.ui.car.addCar.AddCarViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CarDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CAR_ID = "car_id";
    private ActivityCarDetailBinding binding;
    private AddCarViewModel detailViewModel;
    private LayoutCarDetailHeaderBinding headerBinding;
    private String carId;

    private final String[] tabTitles = {"BASIC INFORMATION", "DETAILS", "PRICING"};
    private ECarStatus currentCarStatus = ECarStatus.NOT_VERIFIED;

    // --- INNER CLASS: ECarStatus (Giống Backend) ---
    public enum ECarStatus {
        NOT_VERIFIED("NOT_VERIFIED", R.drawable.bg_status_not_verified, R.color.white) {
            @Override
            public List<ECarStatus> getAllowedTransitions() {
                return Arrays.asList(STOPPED);
            }
        },
        VERIFIED("VERIFIED", R.drawable.bg_status_verified, R.color.white) {
            @Override
            public List<ECarStatus> getAllowedTransitions() {
                return Arrays.asList(STOPPED);
            }
        },
        STOPPED("STOPPED", R.drawable.status_stopped_bg, R.color.white) {
            @Override
            public List<ECarStatus> getAllowedTransitions() {
                return Arrays.asList(NOT_VERIFIED);
            }
        };

        public final String statusName;
        public final int bgDrawable;
        public final int textColor;

        ECarStatus(String statusName, int bgDrawable, int textColor) {
            this.statusName = statusName;
            this.bgDrawable = bgDrawable;
            this.textColor = textColor;
        }

        // Cần phương thức trừu tượng để enum constants triển khai
        public abstract List<ECarStatus> getAllowedTransitions();

        public static ECarStatus fromName(String name) {
            if (name == null) return NOT_VERIFIED;
            for (ECarStatus status : values()) {
                if (status.statusName.equalsIgnoreCase(name)) {
                    return status;
                }
            }
            return NOT_VERIFIED;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo Binding và ContentView
        binding = ActivityCarDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        headerBinding = binding.carDetailHeader;

        // 2. Lấy Car ID từ Intent
        carId = getIntent().getStringExtra(EXTRA_CAR_ID);

        // 3. Khởi tạo ViewModel
        // NOTE: Chúng ta sử dụng AddCarViewModel vì nó đã chứa CarRegistrationData
        detailViewModel = new ViewModelProvider(this).get(AddCarViewModel.class);

        boolean isEdit = (carId != null && !carId.isEmpty());
        detailViewModel.setEditMode(isEdit);

        // 4. Thiết lập Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_car_details);
        }

        setupViewPager();

        setupHeader();

        // 5. Tải dữ liệu xe cũ nếu có ID
        if (isEdit) {
            detailViewModel.fetchCarDetailsForEdit(carId, this);
            Log.i(TAG, "Loading details for Car ID: " + carId);
        } else {
            Toast.makeText(this, "Error: No Car ID provided.", Toast.LENGTH_LONG).show();
        }
        detailViewModel.getRegistrationData().observe(this, data -> {
            if (data != null && data.licensePlate != null) {
                // Dữ liệu đã được tải và ánh xạ thành công!
                if (data.carId == null) {
                    data.carId = carId;
                    detailViewModel.getRegistrationData().setValue(data);
                }
                Log.i(TAG, "Data received in Activity. Ready to display: " + data.licensePlate);
                // Sau khi Activity xác nhận, các Fragment sẽ có thể lấy được dữ liệu này.
            }
        });
    }
    private void setupHeader() {
        // 1. Quan sát dữ liệu xe từ ViewModel để cập nhật UI
        detailViewModel.getRegistrationData().observe(this, this::updateHeaderUI);

        // 2. Thiết lập sự kiện click cho dropdown trạng thái
        // Sử dụng rightDetailsContainer để truy cập các view con trong ConstraintLayout của header
        headerBinding.statusDropdownContainer.setOnClickListener(this::showStatusChangePopup);
    }
    private void updateHeaderUI(CarRegistrationData data) {
        if (data == null) return;
        String imageUrl = data.carImageFrontUri;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Tải ảnh vào ImageView duy nhất trong Header
            Glide.with(this)
                    .load(imageUrl)
                    .into(headerBinding.ivLogoOrCar);
        }
        // 1. Cập nhật thông tin cơ bản
        // SỬA: Truy cập qua rightDetailsContainer
        headerBinding.tvCarModel.setText(data.model);

        // 2. Cập nhật Ratings, Rides, Price
        // SỬA: Truy cập qua rightDetailsContainer
        headerBinding.ratingBar.setRating(Float.parseFloat(String.valueOf(data.averageRatingByCar)));
        headerBinding.tvRidesValue.setText(String.valueOf(data.noOfRides));

        String formattedPrice = formatPrice(data.basePrice);
        // SỬA: Truy cập qua rightDetailsContainer
        headerBinding.tvPriceValue.setText(formattedPrice);

        // 3. Cập nhật Location
        String location = String.format("%s, %s, %s, %s",
                data.addressCityProvince, data.addressDistrict, data.addressWard, data.addressHouseNumberStreet);
        // SỬA: Truy cập qua rightDetailsContainer
        headerBinding.tvLocationsValue.setText(location);

        // 4. Cập nhật Status
        ECarStatus status = ECarStatus.fromName(data.status);
        currentCarStatus = status;
        updateStatusChip(status); // updateStatusChip() cũng cần được sửa bên trong

        Log.d(TAG, "Header updated for car: " + data.model + " | Status: " + status.statusName);
    }
    private String formatPrice(Long price) {
        if (price == null) {
            return "N/A";
        }
        long priceInThousands = price;
        return getString(R.string.format_price_day, String.valueOf(priceInThousands));
    }
    private void updateStatusChip(ECarStatus status) {
        headerBinding.tvStatusValue.setText(status.statusName);

        // Đặt background
        headerBinding.statusDropdownContainer.setBackgroundResource(status.bgDrawable);

        // Đặt màu chữ và icon
        int color = ContextCompat.getColor(this, status.textColor);
        headerBinding.tvStatusValue.setTextColor(color);
        headerBinding.ivDropdownArrow.setColorFilter(color);
    }

    // --- PHẦN BỔ SUNG: Hiển thị Popup Menu thay đổi trạng thái ---
    private void showStatusChangePopup(View view) {

        // Lấy danh sách trạng thái cho phép chuyển đổi từ trạng thái hiện tại
        List<ECarStatus> allowedTransitions = currentCarStatus.getAllowedTransitions();

        if (allowedTransitions.isEmpty()) {
            Toast.makeText(this, "No status transitions allowed from current state.", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu popup = new PopupMenu(this, view, Gravity.END);

        // Chỉ lặp qua các trạng thái được phép
        for (ECarStatus status : allowedTransitions) {
            popup.getMenu().add(0, status.ordinal(), 0, status.statusName);
        }

        popup.setOnMenuItemClickListener(item -> {
            ECarStatus newStatus = ECarStatus.values()[item.getItemId()];
            showConfirmationDialog(newStatus);
            return true;
        });

        popup.show();
    }

    // --- PHẦN BỔ SUNG: Hiển thị Dialog Xác nhận ---
    private void showConfirmationDialog(ECarStatus newStatus) {
        final Dialog dialog = new Dialog(this);
        // Sử dụng Binding cho dialog_confirm_status_change.xml
        DialogStatusConfirmationBinding dialogBinding =
                DialogStatusConfirmationBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Sự kiện "Yes, change it!"
        // 🎯 SỬA LỖI: Cập nhật ID nút từ btnYesChange sang btnConfirmYes
        dialogBinding.btnConfirmYes.setOnClickListener(v -> {
            changeCarStatus(newStatus);
            dialog.dismiss();
        });

        // Sự kiện "No, keep it"
        // 🎯 SỬA LỖI: Cập nhật ID nút từ btnNoKeep sang btnConfirmNo
        dialogBinding.btnConfirmNo.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    // --- PHẦN BỔ SUNG: Logic cập nhật trạng thái ---
    private void changeCarStatus(ECarStatus newStatus) {

        // 1. Cập nhật trạng thái tạm thời trên UI (Nếu API gọi thành công, nó sẽ được cập nhật lại)
        currentCarStatus = newStatus;
        updateStatusChip(newStatus);

        // 2. Cập nhật trạng thái mới vào ViewModel (cho EditCarRequest)
        CarRegistrationData data = detailViewModel.getRegistrationData().getValue();
        if (data != null) {
            data.status = newStatus.statusName; // Lưu tên trạng thái mới vào dữ liệu
            // Ghi dữ liệu trở lại ViewModel để Mapper có thể truy cập
            detailViewModel.getRegistrationData().setValue(data);
        }

        // 3. GỌI API CHỈNH SỬA XE
        if (carId != null && detailViewModel.getIsEditMode().getValue() == true) {
            // Gọi phương thức Edit Car trong ViewModel
            detailViewModel.editCarData(carId, this);
            // Toast sẽ được hiển thị bởi Observer trong setupEditObservers
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID xe hoặc không ở chế độ chỉnh sửa.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViewPager() {
        // 1. Khởi tạo Adapter (Cần tạo CarDetailPagerAdapter)
        CarDetailPagerAdapter pagerAdapter = new CarDetailPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        // 2. Liên kết TabLayout và ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // ✅ THAY THẾ: Dùng finish() thay vì onBackPressed()
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Phương thức này là tiêu chuẩn cho hành động "Up" (back trên toolbar).
        // ✅ THAY THẾ: Gọi finish() thay vì onBackPressed()
        finish();
        return true;
    }
}
