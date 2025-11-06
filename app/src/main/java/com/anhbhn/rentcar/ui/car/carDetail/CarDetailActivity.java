package com.anhbhn.rentcar.ui.car.carDetail;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.databinding.ActivityCarDetailBinding;
import com.anhbhn.rentcar.ui.car.addCar.AddCarViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

public class CarDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CAR_ID = "car_id";
    private ActivityCarDetailBinding binding;
    private AddCarViewModel detailViewModel;

    private final String[] tabTitles = {"BASIC INFORMATION", "DETAILS", "PRICING"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo Binding và ContentView
        binding = ActivityCarDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Lấy Car ID từ Intent
        String carId = getIntent().getStringExtra(EXTRA_CAR_ID);

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

        // 5. Tải dữ liệu xe cũ nếu có ID
        if (isEdit) {
            detailViewModel.fetchCarDetailsForEdit(carId, this);
            Toast.makeText(this, "Loading details for Car ID: " + carId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: No Car ID provided.", Toast.LENGTH_LONG).show();
        }
        detailViewModel.getRegistrationData().observe(this, data -> {
            if (data != null && data.licensePlate != null) {
                // Dữ liệu đã được tải và ánh xạ thành công!
                Log.i(TAG, "Data received in Activity. Ready to display: " + data.licensePlate);
                // Sau khi Activity xác nhận, các Fragment sẽ có thể lấy được dữ liệu này.
            }
        });
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
