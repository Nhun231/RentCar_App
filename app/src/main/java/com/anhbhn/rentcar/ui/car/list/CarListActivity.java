package com.anhbhn.rentcar.ui.car.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.anhbhn.rentcar.ui.car.search.SearchCarActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CarListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private CarListAdapter adapter;
    private TextView tvSearchInfo;
    private CardView cardSearchBox;
    private TextView tvSearchLocation;
    private TextView tvSearchDateTime;
    
    private List<CarThumbnailResponse> carList;
    private String address;
    private String pickUpTime;
    private String dropOffTime;
    private String searchCity;
    private String searchDistrict;
    private String searchWard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);
        
        initializeViews();
        loadIntentData();
        setupRecyclerView();
        displaySearchInfo();
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewCars);
        tvSearchInfo = findViewById(R.id.tvSearchInfo);
        cardSearchBox = findViewById(R.id.cardSearchBox);
        tvSearchLocation = findViewById(R.id.tvSearchLocation);
        tvSearchDateTime = findViewById(R.id.tvSearchDateTime);
        
        // Set click listener for search box to navigate back to search with previous values
        cardSearchBox.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchCarActivity.class);
            // Pass previous search values to pre-populate
            if (searchCity != null) intent.putExtra("previousCity", searchCity);
            if (searchDistrict != null) intent.putExtra("previousDistrict", searchDistrict);
            if (searchWard != null) intent.putExtra("previousWard", searchWard);
            if (pickUpTime != null) intent.putExtra("previousPickUpTime", pickUpTime);
            if (dropOffTime != null) intent.putExtra("previousDropOffTime", dropOffTime);
            startActivity(intent);
        });
    }
    
    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<CarThumbnailResponse> carListArray = (ArrayList<CarThumbnailResponse>) intent.getSerializableExtra("carList");
            if (carListArray != null) {
                carList = new ArrayList<>(carListArray);
            } else {
                carList = new ArrayList<>();
            }
            address = intent.getStringExtra("address");
            pickUpTime = intent.getStringExtra("pickUpTime");
            dropOffTime = intent.getStringExtra("dropOffTime");
            searchCity = intent.getStringExtra("searchCity");
            searchDistrict = intent.getStringExtra("searchDistrict");
            searchWard = intent.getStringExtra("searchWard");
        } else {
            carList = new ArrayList<>();
        }
    }
    
    private void setupRecyclerView() {
        adapter = new CarListAdapter(carList, this::onCarItemClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        updateResultsCount();
    }
    
    private void updateResultsCount() {
        int count = carList != null ? carList.size() : 0;
        String countText = count + " " + (count == 1 ? "car found" : "cars found");
    }
    
    private void displaySearchInfo() {
        // Display location in search box
        if (address != null && !address.isEmpty()) {
            tvSearchLocation.setText(address);
        } else {
            tvSearchLocation.setText("Select location");
        }
        
        // Format and display date/time
        String dateTimeText = formatDateTimeRange();
        tvSearchDateTime.setText(dateTimeText);
        
        // Hide old search info text view
        tvSearchInfo.setVisibility(View.GONE);
    }
    
    private String formatDateTimeRange() {
        if (pickUpTime == null || dropOffTime == null || pickUpTime.isEmpty() || dropOffTime.isEmpty()) {
            return "Select date & time";
        }
        
        try {
            // Parse ISO 8601 format: "yyyy-MM-dd'T'HH:mm:ss"
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            
            Date pickUpDate = apiFormat.parse(pickUpTime);
            Date dropOffDate = apiFormat.parse(dropOffTime);
            
            if (pickUpDate == null || dropOffDate == null) {
                return "Select date & time";
            }
            
            // Format for display: "HH:mm E, dd/MM • HH:mm E, dd/MM" (English locale)
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.ENGLISH);
            SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.ENGLISH);
            
            String pickUpTimeStr = timeFormat.format(pickUpDate);
            String pickUpDateStr = dateFormat.format(pickUpDate);
            String pickUpDayStr = dayFormat.format(pickUpDate).substring(0, 3); // First 3 letters of day (Mon, Tue, etc.)
            
            String dropOffTimeStr = timeFormat.format(dropOffDate);
            String dropOffDateStr = dateFormat.format(dropOffDate);
            String dropOffDayStr = dayFormat.format(dropOffDate).substring(0, 3);
            
            return String.format("%s %s, %s • %s %s, %s", 
                    pickUpTimeStr, pickUpDayStr, pickUpDateStr,
                    dropOffTimeStr, dropOffDayStr, dropOffDateStr);
            
        } catch (ParseException e) {
            return "Select date & time";
        }
    }
    
    private void onCarItemClick(CarThumbnailResponse car) {
        Intent intent = new Intent(this, com.anhbhn.rentcar.ui.car.detail.CarDetailActivity.class);
        intent.putExtra("carId", car.getId());
        intent.putExtra("pickUpTime", pickUpTime);
        intent.putExtra("dropOffTime", dropOffTime);
        startActivity(intent);
    }
}

