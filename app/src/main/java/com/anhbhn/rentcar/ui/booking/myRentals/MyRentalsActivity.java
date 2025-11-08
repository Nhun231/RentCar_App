package com.anhbhn.rentcar.ui.booking.myRentals;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.booking.BookingThumbnailResponse;
import com.anhbhn.rentcar.databinding.ActivityMyRentalsBinding;

import java.util.ArrayList;

public class MyRentalsActivity extends AppCompatActivity implements BookingThumbnailAdapter.OnItemClickListener {

    private ActivityMyRentalsBinding binding;
    private MyRentalsViewModel viewModel;
    private BookingThumbnailAdapter adapter;

    // Các tham số mặc định
    private static final String DEFAULT_SORT = "updatedAt,DESC";
    private static final String DEFAULT_STATUS = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyRentalsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo ViewModel và Repository
        viewModel = new ViewModelProvider(this).get(MyRentalsViewModel.class);
        viewModel.initializeRepository(this);

        setupToolbar();
        setupRecyclerView();
        setupFiltersAndPagination();
        observeViewModel();

        // 2. Tải dữ liệu ban đầu
        viewModel.loadBookings(this, true);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_my_rentals);
        }
    }

    private void setupRecyclerView() {
        adapter = new BookingThumbnailAdapter(this, new ArrayList<>(), this);

        binding.rvBookingList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBookingList.setAdapter(adapter);
    }

    private void setupFiltersAndPagination() {
        // --- 1. Filters/Sort Spinners ---
        setupSpinner(binding.spinnerStatusFilter, R.array.status_filter_options, true);
        setupSpinner(binding.spinnerSortBy, R.array.sort_by_options, false);

        // --- 2. Pagination Buttons ---
        binding.btnPageNext.setOnClickListener(v -> viewModel.goToNextPage(this));
        binding.btnPagePrev.setOnClickListener(v -> viewModel.goToPreviousPage(this));
    }

    private void setupSpinner(Spinner spinner, int arrayResId, boolean isStatusFilter) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (isStatusFilter) {
                    // Xử lý lọc theo trạng thái
                    viewModel.setStatusFilter(MyRentalsActivity.this, selectedItem);
                } else {
                    // Xử lý sắp xếp
                    String sortValue = mapSortOptionToApi(selectedItem);
                    viewModel.setSort(MyRentalsActivity.this, sortValue);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private String mapSortOptionToApi(String selectedOption) {
        // Ánh xạ chuỗi UI (strings.xml) sang chuỗi API (updatedAt,DESC)
        if (selectedOption.contains("Newest to Oldest")) return "updatedAt,DESC";
        if (selectedOption.contains("Oldest to Newest")) return "updatedAt,ASC";
        if (selectedOption.contains("Price: High to Low")) return "basePrice,DESC";
        if (selectedOption.contains("Price: Low to High")) return "basePrice,ASC";
        return DEFAULT_SORT;
    }

    private void observeViewModel() {

        // --- 1. Quan sát Danh sách Booking ---
        viewModel.getBookingList().observe(this, bookingList -> {
            // Cập nhật Adapter (sử dụng bookingList)
            adapter.updateList(bookingList);

            // Hiển thị/Ẩn Empty State
            boolean isListEmpty = bookingList == null || bookingList.isEmpty();

            // Giả định bạn có tv_empty_state (cần phải ánh xạ view này)
            // Nếu không có view tv_empty_state, bạn cần bỏ qua hoặc thêm nó vào layout.

            // 🚨 LƯU Ý: Phải kiểm tra isLoading để tránh hiển thị "rỗng" khi đang tải
            if (isListEmpty && !Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) {
                // binding.tvEmptyState.setVisibility(View.VISIBLE); // Giả định view này tồn tại
            } else {
                // binding.tvEmptyState.setVisibility(View.GONE);
            }

            // Cần cập nhật trạng thái nút phân trang sau khi danh sách thay đổi
            updatePaginationButtonState();
        });

        // --- 2. Quan sát Chỉ báo Trang ("1 / 10") ---
        viewModel.getPageIndicator().observe(this, indicator -> {
            // binding.tvPageIndicator.setText(indicator); // Giả định view tvPageIndicator tồn tại trong binding

            // Lấy TextView từ binding (đã được ánh xạ trong initViews)
            binding.tvPageIndicator.setText(indicator);

            // Cập nhật trạng thái nút sau khi tải trang mới
            updatePaginationButtonState();
        });

        // --- 3. Quan sát Trạng thái Tải (Loading) ---
        viewModel.getIsLoading().observe(this, isLoading -> {
            // binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            updatePaginationButtonState(); // Cập nhật trạng thái nút (vô hiệu hóa khi isLoading)
        });

        // --- 4. Quan sát Thông báo Lỗi ---
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                // Nếu bạn có hàm clearErrorMessage() trong ViewModel, hãy gọi nó:
                // viewModel.clearErrorMessage();
            }
        });

        // --- 5. Quan sát Waiting Count Summary ---
        viewModel.getWaitingCount().observe(this, count -> {
            binding.tvWaitingCountSummary.setText(getString(R.string.msg_waiting_count_format, count));
        });
    }

    /**
     * Cập nhật trạng thái Enabled/Disabled của các nút phân trang (giống MyCars).
     */
    private void updatePaginationButtonState() {
        // Lấy trạng thái từ ViewModel
        boolean isLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());

        // Kiểm tra trạng thái trang
        boolean isFirstPage = Boolean.TRUE.equals(viewModel.getCurrentPage().getValue() == 0);
        boolean isLastPage = viewModel.isLastPage(); // Hàm kiểm tra từ ViewModel

        // Lấy các ImageButton từ binding
        View btnPrev = binding.btnPagePrev;
        View btnNext = binding.btnPageNext;

        // Vô hiệu hóa khi đang tải
        btnPrev.setEnabled(!isLoading && !isFirstPage);
        btnNext.setEnabled(!isLoading && !isLastPage);
    }

    // --- Interface Implementations (Xử lý hành động từ Adapter) ---

    @Override
    public void onViewDetailsClick(String bookingNumber) {
        // Chuyển sang màn hình chi tiết
//        Intent intent = new Intent(this, RentalDetailsActivity.class);
//        intent.putExtra("BOOKING_NUMBER", bookingNumber);
//        startActivity(intent);
    }

    @Override
    public void onApproveClick(String bookingNumber) {
        // TODO: Hiển thị dialog xác nhận trước (như ảnh web)
        Toast.makeText(this, "Xác nhận Approve cho Booking: " + bookingNumber, Toast.LENGTH_SHORT).show();
        // Sau khi xác nhận, gọi: viewModel.approveBooking(bookingNumber, this);
    }

    @Override
    public void onRejectClick(String bookingNumber) {
        // TODO: Hiển thị dialog xác nhận trước (như ảnh web)
        Toast.makeText(this, "Xác nhận Reject cho Booking: " + bookingNumber, Toast.LENGTH_SHORT).show();
        // Sau khi xác nhận, gọi: viewModel.rejectBooking(bookingNumber, this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}