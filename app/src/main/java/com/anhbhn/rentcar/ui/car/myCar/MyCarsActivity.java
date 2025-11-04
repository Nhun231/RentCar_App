package com.anhbhn.rentcar.ui.car.myCar;

import android.os.Bundle;
import android.view.View;
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

import java.util.ArrayList;

public class MyCarsActivity extends AppCompatActivity {

    private MyCarsViewModel viewModel;
    private MyCarsAdapter adapter;

    // --- Views ---
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Toolbar toolbar;

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

        setupRecyclerView();
        observeViewModel();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_my_cars);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupRecyclerView() {
        adapter = new MyCarsAdapter(this, new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Thêm Scroll Listener để xử lý Phân trang (Load More)
        recyclerView.addOnScrollListener(new PaginationScrollListener(layoutManager));
    }

    private void observeViewModel() {
        // Quan sát danh sách xe
        viewModel.getCarList().observe(this, carList -> {
            adapter.updateData(carList);
            // Hiển thị Empty State: nếu danh sách rỗng VÀ không đang tải
            if (carList.isEmpty() && !Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) {
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        });

        // Quan sát trạng thái tải
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Quan sát thông báo lỗi
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                // Xóa thông báo lỗi sau khi hiển thị
                viewModel.clearErrorMessage();
            }
        });
    }

    // --- Inner Class: PaginationScrollListener ---

    private class PaginationScrollListener extends androidx.recyclerview.widget.RecyclerView.OnScrollListener {
        private final LinearLayoutManager layoutManager;

        public PaginationScrollListener(LinearLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // Kiểm tra xem đã cuộn đến cuối danh sách chưa
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            // Điều kiện tải: (Không phải trang cuối) VÀ (Không đang tải) VÀ (Gần cuối)
            boolean shouldLoadMore =
                    !viewModel.isLastPage() &&
                            !Boolean.TRUE.equals(viewModel.getIsLoading().getValue()) &&
                            (visibleItemCount + firstVisibleItemPosition) >= (totalItemCount - VISIBLE_THRESHOLD) &&
                            firstVisibleItemPosition >= 0;

            if (shouldLoadMore) {
                // Kích hoạt tải thêm trang
                viewModel.loadCars(false);
            }
        }
    }
}