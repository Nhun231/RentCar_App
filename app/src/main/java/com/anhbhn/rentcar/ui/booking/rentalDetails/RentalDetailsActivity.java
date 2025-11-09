package com.anhbhn.rentcar.ui.booking.rentalDetails;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View; // Import View cho visibility
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.databinding.ActivityRentalDetailsBinding;
import com.anhbhn.rentcar.ui.booking.ConfirmationDialogFragment;
import com.anhbhn.rentcar.ui.booking.myRentals.BookingThumbnailAdapter;
import com.anhbhn.rentcar.ui.car.myCar.CarImageSliderAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class RentalDetailsActivity extends AppCompatActivity implements ConfirmationDialogFragment.ConfirmationListener {

    public static final String EXTRA_BOOKING_NUMBER = "BOOKING_NUMBER";
    private ActivityRentalDetailsBinding binding;
    private RentalDetailsViewModel viewModel;
    private final String[] tabTitles = {"RENTAL INFORMATION", "CAR INFORMATION"};
    private static final String STATUS_WAITING_CONFIRMED = "WAITING_CONFIRMED";
    private static final String STATUS_WAITING_RETURN = "WAITING_CONFIRMED_RETURN_CAR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRentalDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String bookingNumber = getIntent().getStringExtra(EXTRA_BOOKING_NUMBER);

        viewModel = new ViewModelProvider(this).get(RentalDetailsViewModel.class);
        viewModel.initializeRepository(this);

        setupToolbar();
        // 🛑 BỎ setupViewPager() VÌ NÓ SẼ ĐƯỢC GỌI TRONG OBSERVER ĐỂ NGĂN TRÙNG LẶP

        observeViewModel();

        if (bookingNumber != null) {
            viewModel.fetchBookingDetails(this, bookingNumber);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy số booking.", Toast.LENGTH_LONG).show();
            finish();
        }

        setupActionListeners();
    }

    // ----------------------------------------------------
    // CÁC HÀM SETUP & KẾT NỐI
    // ----------------------------------------------------

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_rental_details);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Gọi onSupportNavigateUp để xử lý Back
            return onSupportNavigateUp();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Đóng Activity hiện tại và quay về Activity trước (MyRentalsActivity)
        return true;
    }

    private void setupActionListeners() {
        // TODO: Cần Dialog Fragment để xử lý Approve/Reject ở màn hình này

        // Ví dụ: Gán Listener cho nút Approve ở Header
        binding.btnApproveRequestHeader.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Approve Detail chưa triển khai.", Toast.LENGTH_SHORT).show();
            // Cần lấy bookingNumber và status từ ViewModel và gọi Dialog
        });

        // Cần thêm logic cho nút Reject header
        binding.btnRejectRequestHeader.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Reject Detail chưa triển khai.", Toast.LENGTH_SHORT).show();
        });
    }

    // ----------------------------------------------------
    // OBSERVERS (Đổ dữ liệu)
    // ----------------------------------------------------

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            // Hiển thị Progress Bar
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });

        // ✅ NEW: Quan sát thông báo thành công sau hành động Approve/Reject
        viewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();

                viewModel.clearSuccessMessage();
            }
        });

        // 1. Quan sát Booking Details (Cập nhật giá, thời gian, trạng thái)
        viewModel.getBookingDetails().observe(this, this::updateHeaderBookingInfo);

        // 2. Quan sát Car Details (Cập nhật tên xe, tải ảnh, khởi tạo tabs)
        viewModel.getCarDetails().observe(this, this::updateHeaderCarInfoAndImages);
    }

    /**
     * ✅ 1. Cập nhật thông tin Booking và giá (từ API Booking).
     */
    private void updateHeaderBookingInfo(BookingResponse.Data details) {
        if (details == null) return;

        String period = String.format(Locale.getDefault(), "%s - %s", details.getPickUpTime(), details.getDropOffTime());
        binding.tvRentalPeriod.setText(period);

        binding.tvTotalPrice.setText(getString(R.string.format_price_day, String.valueOf(details.getTotalPrice())));
        binding.tvDeposit.setText(getString(R.string.format_price_day, String.valueOf(details.getDeposit())));
        binding.tvBookingStatus.setText(String.format("Booking Status: %s", details.getStatus()));

        boolean showActionButtons = STATUS_WAITING_CONFIRMED.equals(details.getStatus()) ||
                STATUS_WAITING_RETURN.equals(details.getStatus());

        int visibility = showActionButtons ? View.VISIBLE : View.GONE;

        binding.btnApproveRequestHeader.setVisibility(visibility);
        binding.btnRejectRequestHeader.setVisibility(visibility);

        // --- GÁN LISTENER (Chỉ gán nếu nút được hiển thị) ---
        if (showActionButtons) {
            String bookingNumber = details.getBookingNumber();

            // Cần triển khai showActionDialog trong Activity để gọi Dialog xác nhận
            binding.btnApproveRequestHeader.setOnClickListener(v ->
                    showBookingActionDialog(bookingNumber, "APPROVE", details.getStatus()));

            binding.btnRejectRequestHeader.setOnClickListener(v ->
                    showBookingActionDialog(bookingNumber, "REJECT", details.getStatus()));
        }
    }
    private void showBookingActionDialog(String bookingNumber, String actionType, String currentItemStatus) {
        String title, message, confirmText;
        int confirmColorResId;
        boolean showIcon = false;

        // 1. Xác định nội dung và màu sắc
        if (actionType.equals("REJECT")) {
            title = getString(R.string.dialog_title_are_you_sure);
            message = getString(R.string.dialog_msg_reject_booking_q);
            confirmText = getString(R.string.btn_yes_reject);
            confirmColorResId = R.color.red_action;
            showIcon = true;
        } else {
            // APPROVE
            title = getString(R.string.dialog_title_confirm_action);
            message = getString(R.string.dialog_msg_confirm_booking_q);
            confirmText = getString(R.string.btn_confirm);
            confirmColorResId = R.color.primary_green;
        }

        // 2. Tạo instance DialogFragment và truyền tham số
        ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(
                bookingNumber,
                actionType,
                title,
                message,
                confirmText,
                confirmColorResId,
                showIcon
        );

        // Truyền trạng thái hiện tại (Giữ lại để sử dụng trong onConfirmAction)
        Bundle args = dialogFragment.getArguments();
        if (args != null) {
            args.putString("CURRENT_ITEM_STATUS", currentItemStatus);
        }

        // 3. Gán Listener và Hiển thị
        dialogFragment.setConfirmationListener(this); // ⬅️ Gán Activity làm Listener
        dialogFragment.show(getSupportFragmentManager(), "BookingActionDialog");
    }


    /**
     * ✅ 2. Cập nhật thông tin chi tiết xe và Slider ảnh (từ API Car Details).
     * Hàm này cũng kích hoạt TabLayout Mediator.
     */
    private void updateHeaderCarInfoAndImages(CarResponse.Data carDetails) {
        if (carDetails == null) return;

        // 1. Cập nhật Tên Xe/Model
        binding.tvCarName.setText(String.format("%s %s", carDetails.brand, carDetails.model));

        // 2. Tái tạo danh sách 4 URL từ CarResponse.Data
        List<String> imageUrls = Arrays.asList(
                carDetails.carImageFrontUrl,
                carDetails.carImageBackUrl,
                carDetails.carImageLeftUrl,
                carDetails.carImageRightUrl
        );

        // 3. Tải ảnh Slider
        CarImageSliderAdapter sliderAdapter = new CarImageSliderAdapter(this, imageUrls);
        binding.vpCarImagesSlider.setAdapter(sliderAdapter);

        // 4. Khởi tạo TabLayout Mediator (Chỉ attach một lần sau khi dữ liệu xe có sẵn)
        if (binding.tabLayout.getTabCount() == 0) {
            RentalDetailsPagerAdapter pagerAdapter = new RentalDetailsPagerAdapter(this);
            binding.viewPager.setAdapter(pagerAdapter);

            new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                    (tab, position) -> tab.setText(tabTitles[position])
            ).attach();
        }
    }
    @Override
    public void onConfirmAction(String bookingNumber, String actionType) {

        ConfirmationDialogFragment dialogFragment = (ConfirmationDialogFragment) getSupportFragmentManager().findFragmentByTag("BookingActionDialog");

        String currentStatus = "WAITING_CONFIRMED";

        if (dialogFragment != null && dialogFragment.getArguments() != null) {
            currentStatus = dialogFragment.getArguments().getString("CURRENT_ITEM_STATUS", "WAITING_CONFIRMED");
        }

        if (actionType.equals("APPROVE")) {
            viewModel.approveBooking(bookingNumber, currentStatus, this);
        } else if (actionType.equals("REJECT")) {
            viewModel.rejectBooking(bookingNumber, currentStatus, this);
        }
        Toast.makeText(this, actionType + " yêu cầu đã được gửi cho booking " + bookingNumber, Toast.LENGTH_SHORT).show();
    }
}