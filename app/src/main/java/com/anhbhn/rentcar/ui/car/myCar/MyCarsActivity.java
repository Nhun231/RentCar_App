package com.anhbhn.rentcar.ui.car.myCar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.anhbhn.rentcar.ui.car.addCar.AddCarActivity;

import java.util.ArrayList;

public class MyCarsActivity extends AppCompatActivity {

    private MyCarsViewModel viewModel;
    private MyCarsAdapter adapter;

    // --- Views ---
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Toolbar toolbar;
    private AutoCompleteTextView sortDropdown;
    private ImageButton btnPrev; // 🌟 Views mới
    private ImageButton btnNext; // 🌟 Views mới
    private TextView tvPageIndicator;

    // --- Pagination Constants ---
    private static final int VISIBLE_THRESHOLD = 5; // Số item còn lại trước khi tải thêm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activity layout phải là activity_my_cars.xml
        setContentView(R.layout.activity_my_cars);

        // 1. Khởi tạo Views
        initViews();

        // 2. Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_my_cars);
        }

        // 3. Khởi tạo ViewModel (Yêu cầu AppCompatActivity)
        viewModel = new ViewModelProvider(this).get(MyCarsViewModel.class);

        // 4. Khởi tạo Repository và bắt đầu tải dữ liệu
        viewModel.initializeRepository(this); // Gọi hàm này để khởi tạo Repository và tải trang 0
        setupSortDropdown();
        setupPaginationControls();
        setupRecyclerView();
        observeViewModel();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_cars, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_car) {

            // Xử lý khi nhấn nút "Add Car"
            startActivity(new Intent(this, AddCarActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_my_cars);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        toolbar = findViewById(R.id.toolbar);
        sortDropdown = findViewById(R.id.input_sort_by_dropdown);
        btnPrev = findViewById(R.id.btn_page_prev);
        btnNext = findViewById(R.id.btn_page_next);
        tvPageIndicator = findViewById(R.id.tv_page_indicator);
    }
    private void setupSortDropdown() {
        // Giả định bạn đã định nghĩa mảng string-array/sort_options_display
        String[] sortOptionsDisplay = getResources().getStringArray(R.array.sort_options_display);

        // Tạo Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, // Dùng layout mặc định của Android
                sortOptionsDisplay);

        sortDropdown.setAdapter(adapter);

        // Thiết lập giá trị mặc định ("Production year: Newest first")
        if (sortOptionsDisplay.length > 0) {
            sortDropdown.setText(sortOptionsDisplay[0], false);
        }

        // Lắng nghe sự kiện chọn item
        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            // Ánh xạ vị trí được chọn sang tham số API (Ví dụ: productionYear,ASC)
            String selectedApiSort = mapSortPositionToApi(position);

            // Gọi ViewModel để cập nhật tham số và tải lại dữ liệu từ trang 0
            viewModel.setCurrentSortParameter(selectedApiSort);
        });
    }

    /**
     * Ánh xạ vị trí được chọn trong Dropdown sang tham số API.
     */
    private String mapSortPositionToApi(int position) {
        // Phải khớp với thứ tự trong arrays.xml và yêu cầu của API backend
        switch (position) {
            case 0: return "productionYear,DESC"; // Production year: Newest first
            case 1: return "productionYear,ASC";  // Production year: Oldest first
            case 2: return "basePrice,ASC";       // Price: Low to High
            case 3: return "basePrice,DESC";      // Price: High to Low
            default: return "productionYear,DESC";
        }
    }
    private void setupPaginationControls() {
        // Gán listener cho nút PREV và NEXT
        btnNext.setOnClickListener(v -> viewModel.goToNextPage());
        btnPrev.setOnClickListener(v -> viewModel.goToPrevPage());

        // Mặc định vô hiệu hóa nút PREV ở trang 1
        btnPrev.setEnabled(false);
    }

    private void setupRecyclerView() {
        adapter = new MyCarsAdapter(this, new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Thêm Scroll Listener để xử lý Phân trang (Load More)
//        recyclerView.addOnScrollListener(new PaginationScrollListener(layoutManager));
    }

    private void observeViewModel() {
        // Quan sát danh sách xe
        viewModel.getCarList().observe(this, carList -> {
            adapter.updateData(carList);
            // Hiển thị Empty State
            if (carList.isEmpty() && !Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) {
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        });

        // Quan sát chỉ báo trang (ví dụ: "1 / 10")
        viewModel.getPageIndicator().observe(this, indicator -> {
            tvPageIndicator.setText(indicator);
            // Cập nhật trạng thái nút sau khi tải trang mới
            updatePaginationButtonState();
        });

        // Quan sát trạng thái tải (dùng để vô hiệu hóa nút điều hướng)
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            updatePaginationButtonState(); // Cập nhật trạng thái nút khi loading
        });

        // Quan sát thông báo lỗi (giữ nguyên)
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
    }
    private void updatePaginationButtonState() {
        boolean isLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());

        boolean isFirstPage = viewModel.getCurrentPage() == 0;

        boolean isLastPage = viewModel.isLastPage();

        // Vô hiệu hóa khi đang tải
        btnPrev.setEnabled(!isLoading && !isFirstPage);
        btnNext.setEnabled(!isLoading && !isLastPage);
    }
}