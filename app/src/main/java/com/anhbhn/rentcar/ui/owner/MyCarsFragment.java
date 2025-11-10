package com.anhbhn.rentcar.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.ui.car.addCar.AddCarActivity;
import com.anhbhn.rentcar.ui.car.myCar.MyCarsAdapter;
import com.anhbhn.rentcar.ui.car.myCar.MyCarsViewModel;
import com.anhbhn.rentcar.ui.car.carDetail.CarDetailActivity;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

/**
 * Fragment Wrapper cho màn hình My Cars, tích hợp logic UI, Filtering, và Pagination
 * vào một tab của OwnerMainActivity.
 */
public class MyCarsFragment extends Fragment implements MyCarsAdapter.OnCarActionListener {

    private MyCarsViewModel viewModel;
    private MyCarsAdapter adapter;

    // --- Views ---
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextInputLayout sortInputLayout;
    private AutoCompleteTextView sortDropdown;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private TextView tvPageIndicator;

    private static final String DEFAULT_SORT = "productionYear,DESC";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_my_cars, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MyCarsViewModel.class);
        viewModel.initializeRepository(requireContext());

        initViews(view);
        setHasOptionsMenu(true);
        setupToolbar(view);
        setupSortDropdown();
        setupPaginationControls();
        setupRecyclerView(); // ⬅️ Gọi hàm đã sửa
        observeViewModel();

        if (viewModel.getCarList().getValue().isEmpty()) {
            viewModel.loadCars(true);
        }
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // ✅ FIX: Xóa menu cũ (nếu có) để tránh lặp lại nút Menu hoặc nút Back
        menu.clear();

        // Khôi phục menu Add Car
        inflater.inflate(R.menu.menu_my_cars, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Xử lý khi nhấn nút "Add Car"
        if (item.getItemId() == R.id.action_add_car) {
            startActivity(new Intent(requireContext(), AddCarActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_my_cars);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        sortInputLayout = view.findViewById(R.id.input_sort_by_container);
        sortDropdown = view.findViewById(R.id.input_sort_by_dropdown);

        btnPrev = view.findViewById(R.id.btn_page_prev);
        btnNext = view.findViewById(R.id.btn_page_next);
        tvPageIndicator = view.findViewById(R.id.tv_page_indicator);
    }

    private void setupToolbar(View view) {
        // ✅ FIX: Xóa logic ẩn Toolbar trong Fragment
        View toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE); // Hoặc không làm gì cả
        }
        // Gán Activity làm Action Bar
        ((AppCompatActivity) requireActivity()).setSupportActionBar((androidx.appcompat.widget.Toolbar) toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.title_my_cars);
        }
    }

    private void setupSortDropdown() {
        String[] sortOptionsDisplay = getResources().getStringArray(R.array.sort_options_display);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, sortOptionsDisplay);

        sortDropdown.setAdapter(arrayAdapter);

        if (sortOptionsDisplay.length > 0) {
            sortDropdown.setText(sortOptionsDisplay[0], false);
        }

        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedApiSort = mapSortPositionToApi(position);
            viewModel.setCurrentSortParameter(selectedApiSort);
        });
    }

    private String mapSortPositionToApi(int position) {
        switch (position) {
            case 0: return "productionYear,DESC";
            case 1: return "productionYear,ASC";
            case 2: return "basePrice,ASC";
            case 3: return "basePrice,DESC";
            default: return DEFAULT_SORT;
        }
    }

    private void setupPaginationControls() {
        btnNext.setOnClickListener(v -> viewModel.goToNextPage());
        btnPrev.setOnClickListener(v -> viewModel.goToPrevPage());
        btnPrev.setEnabled(false);
    }

    private void setupRecyclerView() {
        // ✅ ĐÃ SỬA: Truyền 'this' (Fragment) làm listener cho Adapter
        adapter = new MyCarsAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getCarList().observe(getViewLifecycleOwner(), carList -> {
            adapter.updateData(carList);
            boolean isEmpty = carList.isEmpty();
            boolean isLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());

            if (isEmpty && !isLoading) {
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getPageIndicator().observe(getViewLifecycleOwner(), indicator -> {
            tvPageIndicator.setText(indicator);
            updatePaginationButtonState();
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            updatePaginationButtonState();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    private void updatePaginationButtonState() {
        boolean isLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
        boolean isFirstPage = viewModel.getCurrentPage() == 0;
        boolean isLastPage = viewModel.isLastPage();

        btnPrev.setEnabled(!isLoading && !isFirstPage);
        btnNext.setEnabled(!isLoading && !isLastPage);
    }

    // --- Implementation của Adapter Listener ---
    @Override
    public void onCarAction(String carId, String actionType) {
        if ("VIEW_DETAILS".equals(actionType)) {
            // Logic điều hướng
            Intent intent = new Intent(requireContext(), CarDetailActivity.class);
            intent.putExtra(CarDetailActivity.EXTRA_CAR_ID, carId);
            startActivity(intent);
        } else {
            // Logic cho menu (hoặc các hành động khác)
            Toast.makeText(requireContext(), "Action: " + actionType + " on Car ID: " + carId, Toast.LENGTH_SHORT).show();
        }
    }
}