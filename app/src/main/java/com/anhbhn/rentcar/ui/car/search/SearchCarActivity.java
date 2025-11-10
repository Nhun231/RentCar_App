package com.anhbhn.rentcar.ui.car.search;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.anhbhn.rentcar.data.dto.response.car.SearchCarResponse;
import com.anhbhn.rentcar.data.repository.car.CarRepository;
import com.anhbhn.rentcar.ui.car.addCar.AddCarViewModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchCarActivity extends AppCompatActivity {
    
    private Spinner spinnerCityProvince, spinnerDistrict, spinnerWard;
    private Button btnPickUpDate, btnPickUpTime, btnDropOffDate, btnDropOffTime;
    private Button btnSearch;
    
    private AddCarViewModel viewModel;
    private CarRepository carRepository;
    
    private Calendar pickUpCalendar = Calendar.getInstance();
    private Calendar dropOffCalendar = Calendar.getInstance();
    
    // Previous search values
    private String previousCity;
    private String previousDistrict;
    private String previousWard;
    private String previousPickUpTime;
    private String previousDropOffTime;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    // ISO 8601 format for API: "yyyy-MM-ddTHH:mm:ss" (without quotes around T)
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_car);
        
        initializeViews();
        setupViewModel();
        setupDateTimePickers();
        setupSearchButton();
        loadPreviousSearchValues();
    }
    
    private void initializeViews() {
        spinnerCityProvince = findViewById(R.id.spinnerCityProvince);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        btnPickUpDate = findViewById(R.id.btnPickUpDate);
        btnPickUpTime = findViewById(R.id.btnPickUpTime);
        btnDropOffDate = findViewById(R.id.btnDropOffDate);
        btnDropOffTime = findViewById(R.id.btnDropOffTime);
        btnSearch = findViewById(R.id.btnSearch);
        
        // Initially disable district and ward until city is selected
        // Set empty adapters with placeholder so hints are visible
        setupEmptySpinner(spinnerDistrict, getString(R.string.select_district));
        setupEmptySpinner(spinnerWard, getString(R.string.select_ward));
        spinnerDistrict.setEnabled(false);
        spinnerWard.setEnabled(false);
        
        // Set default times (2 hours from now for pick-up, rounded up to next hour)
        // Ensure pickup time is at least 2 hours from now and within valid time window (06:00-22:00)
        Calendar minPickupTime = calculateMinimumPickupTime();
        pickUpCalendar.setTime(minPickupTime.getTime());
        
        // Set drop-off time to 2 hours after pickup (minimum duration), rounded up to next hour
        dropOffCalendar.setTime(pickUpCalendar.getTime());
        dropOffCalendar.add(Calendar.HOUR_OF_DAY, 2);
        // Round up drop-off to next hour if it has minutes
        if (dropOffCalendar.get(Calendar.MINUTE) > 0) {
            dropOffCalendar.add(Calendar.HOUR_OF_DAY, 1);
            dropOffCalendar.set(Calendar.MINUTE, 0);
        }
        
        // Ensure drop-off time is within valid time window (06:00-22:00)
        int dropOffHour = dropOffCalendar.get(Calendar.HOUR_OF_DAY);
        if (dropOffHour >= 22) {
            // Cap at 22:00 (maximum allowed time)
            dropOffCalendar.set(Calendar.HOUR_OF_DAY, 22);
            dropOffCalendar.set(Calendar.MINUTE, 0);
        }
        
        updateDateTimeButtons();
    }
    
    private void setupEmptySpinner(Spinner spinner, String placeholder) {
        List<String> emptyList = new ArrayList<>();
        emptyList.add(placeholder);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, emptyList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AddCarViewModel.class);
        // Initialize address data from database.json (same as add car)
        viewModel.initializeCarOptions(this);
        setupAddressDropdowns();
    }
    
    private void setupAddressDropdowns() {
        // Observe city list and setup city spinner
        viewModel.getAvailableCities().observe(this, cities -> {
            if (cities != null && !cities.isEmpty()) {
                // Add placeholder at the beginning
                List<String> citiesWithPlaceholder = new ArrayList<>();
                citiesWithPlaceholder.add(getString(R.string.select_city_province));
                citiesWithPlaceholder.addAll(cities);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, citiesWithPlaceholder);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCityProvince.setAdapter(adapter);
                
                // Restore previous city selection if available, otherwise set up normal listeners
                if (previousCity != null && !previousCity.isEmpty()) {
                    restorePreviousAddress();
                } else {
                    // Set up normal listeners if no previous values
                    setupCitySelectionListener();
                    setupDistrictSelectionListener();
                }
            }
        });
    }
    
    private void setupDistrictDropdown(List<String> districts) {
        if (districts != null && !districts.isEmpty()) {
            // Add placeholder at the beginning
            List<String> districtsWithPlaceholder = new ArrayList<>();
            districtsWithPlaceholder.add(getString(R.string.select_district));
            districtsWithPlaceholder.addAll(districts);
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, districtsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDistrict.setAdapter(adapter);
            spinnerDistrict.setEnabled(true);
        } else {
            spinnerDistrict.setEnabled(false);
        }
    }
    
    private void setupWardDropdown(List<String> wards) {
        if (wards != null && !wards.isEmpty()) {
            // Add placeholder at the beginning
            List<String> wardsWithPlaceholder = new ArrayList<>();
            wardsWithPlaceholder.add(getString(R.string.select_ward));
            wardsWithPlaceholder.addAll(wards);
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, wardsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWard.setAdapter(adapter);
            spinnerWard.setEnabled(true);
        } else {
            spinnerWard.setEnabled(false);
        }
    }
    
    private void setupDateTimePickers() {
        btnPickUpDate.setOnClickListener(v -> showDatePicker(true));
        btnPickUpTime.setOnClickListener(v -> showTimePicker(true));
        btnDropOffDate.setOnClickListener(v -> showDatePicker(false));
        btnDropOffTime.setOnClickListener(v -> showTimePicker(false));
    }
    
    private void showDatePicker(boolean isPickUp) {
        Calendar calendar = isPickUp ? pickUpCalendar : dropOffCalendar;
        Calendar now = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // Check if selected date is today
                    Calendar today = Calendar.getInstance();
                    boolean isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                     selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
                    
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    if (isPickUp) {
                        // If pickup date is today, adjust time to minimum pickup time if current time is too early
                        if (isToday) {
                            Calendar minPickupTime = calculateMinimumPickupTime();
                            // Only update if current pickup time is before minimum
                            if (pickUpCalendar.before(minPickupTime)) {
                                pickUpCalendar.set(Calendar.HOUR_OF_DAY, minPickupTime.get(Calendar.HOUR_OF_DAY));
                                pickUpCalendar.set(Calendar.MINUTE, minPickupTime.get(Calendar.MINUTE));
                            }
                        } else {
                            // For future dates, ensure time is at least 06:00
                            if (pickUpCalendar.get(Calendar.HOUR_OF_DAY) < 6) {
                                pickUpCalendar.set(Calendar.HOUR_OF_DAY, 6);
                                pickUpCalendar.set(Calendar.MINUTE, 0);
                            }
                        }
                    } else {
                        // For drop-off, ensure it's at least 2 hours after pickup (rounded up)
                        Calendar minDropoffTime = (Calendar) pickUpCalendar.clone();
                        minDropoffTime.add(Calendar.HOUR_OF_DAY, 2);
                        // Round up to next hour
                        if (minDropoffTime.get(Calendar.MINUTE) > 0) {
                            minDropoffTime.add(Calendar.HOUR_OF_DAY, 1);
                            minDropoffTime.set(Calendar.MINUTE, 0);
                        }
                        // If drop-off is before minimum, update it
                        if (dropOffCalendar.before(minDropoffTime)) {
                            dropOffCalendar.setTime(minDropoffTime.getTime());
                        }
                    }
                    
                    updateDateTimeButtons();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void showTimePicker(boolean isPickUp) {
        Calendar calendar = isPickUp ? pickUpCalendar : dropOffCalendar;
        
        // Calculate minimum allowed time based on selected date
        Calendar minTime = null;
        Calendar now = Calendar.getInstance();
        Calendar selectedDate = (Calendar) calendar.clone();
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);
        
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        if (isPickUp) {
            // For pickup: if selected date is today, use minimum pickup time (2 hours from now, rounded up)
            // If future date, allow from 06:00
            if (selectedDate.equals(today)) {
                minTime = calculateMinimumPickupTime();
            } else {
                // Future date: minimum is 06:00
                minTime = (Calendar) selectedDate.clone();
                minTime.set(Calendar.HOUR_OF_DAY, 6);
                minTime.set(Calendar.MINUTE, 0);
            }
        } else {
            // For drop-off: minimum is 2 hours after pickup (rounded up to next hour)
            minTime = (Calendar) pickUpCalendar.clone();
            minTime.add(Calendar.HOUR_OF_DAY, 2);
            // Round up to next hour
            if (minTime.get(Calendar.MINUTE) > 0) {
                minTime.add(Calendar.HOUR_OF_DAY, 1);
                minTime.set(Calendar.MINUTE, 0);
            }
            
            // If minTime is on a different date than selected, use 06:00 on selected date
            Calendar minDate = (Calendar) minTime.clone();
            minDate.set(Calendar.HOUR_OF_DAY, 0);
            minDate.set(Calendar.MINUTE, 0);
            if (!minDate.equals(selectedDate)) {
                minTime = (Calendar) selectedDate.clone();
                minTime.set(Calendar.HOUR_OF_DAY, 6);
                minTime.set(Calendar.MINUTE, 0);
            }
        }
        
        final Calendar finalMinTime = minTime != null ? (Calendar) minTime.clone() : null;
        
        // Determine initial hour and minute for the time picker
        // Use minimum time if current time is before minimum, otherwise use current time
        int initialHour = calendar.get(Calendar.HOUR_OF_DAY);
        int initialMinute = calendar.get(Calendar.MINUTE);
        
        if (finalMinTime != null && calendar.before(finalMinTime)) {
            // Start picker at minimum valid time to guide user
            initialHour = finalMinTime.get(Calendar.HOUR_OF_DAY);
            initialMinute = finalMinTime.get(Calendar.MINUTE);
        }
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar selectedTime = (Calendar) calendar.clone();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    
                    // Validate selected time meets minimum requirements
                    if (finalMinTime != null && selectedTime.before(finalMinTime)) {
                        showToast("Selected time must be at least " + 
                                timeFormat.format(finalMinTime.getTime()), false);
                        // Don't update the calendar, keep the previous valid time
                        return;
                    }
                    
                    // Validate time is within valid window (06:00-22:00)
                    if (hourOfDay < 6 || hourOfDay > 22 || (hourOfDay == 22 && minute > 0)) {
                        showToast("Time must be between 06:00 and 22:00", false);
                        return;
                    }
                    
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    
                    // If pickup time changed, update drop-off time to maintain minimum duration
                    if (isPickUp) {
                        Calendar newMinDropoff = (Calendar) calendar.clone();
                        newMinDropoff.add(Calendar.HOUR_OF_DAY, 2);
                        // Round up to next hour
                        if (newMinDropoff.get(Calendar.MINUTE) > 0) {
                            newMinDropoff.add(Calendar.HOUR_OF_DAY, 1);
                            newMinDropoff.set(Calendar.MINUTE, 0);
                        }
                        // If current drop-off is before new minimum, update it
                        if (dropOffCalendar.before(newMinDropoff)) {
                            dropOffCalendar.setTime(newMinDropoff.getTime());
                        }
                    }
                    
                    updateDateTimeButtons();
                },
                initialHour,
                initialMinute,
                true
        );
        timePickerDialog.show();
    }
    
    /**
     * Calculates the minimum pickup time: 2 hours from now, rounded up to the next hour,
     * ensuring it's within the valid time window (06:00-22:00).
     */
    private Calendar calculateMinimumPickupTime() {
        Calendar now = Calendar.getInstance();
        Calendar minTime = (Calendar) now.clone();
        
        // Add 2 hours
        minTime.add(Calendar.HOUR_OF_DAY, 2);
        
        // Round up to next hour (if minutes > 0, add 1 hour and set minutes to 0)
        if (minTime.get(Calendar.MINUTE) > 0) {
            minTime.add(Calendar.HOUR_OF_DAY, 1);
            minTime.set(Calendar.MINUTE, 0);
        } else {
            minTime.set(Calendar.MINUTE, 0);
        }
        minTime.set(Calendar.SECOND, 0);
        minTime.set(Calendar.MILLISECOND, 0);
        
        int hour = minTime.get(Calendar.HOUR_OF_DAY);
        
        // If after 22:00, move to next day at 08:00 (earliest valid time that's at least 2 hours after 06:00)
        if (hour >= 22) {
            minTime.add(Calendar.DAY_OF_MONTH, 1);
            minTime.set(Calendar.HOUR_OF_DAY, 8);
            minTime.set(Calendar.MINUTE, 0);
        } else if (hour < 6) {
            // If before 06:00 (edge case: current time is before 04:00), set to 08:00 same day
            minTime.set(Calendar.HOUR_OF_DAY, 8);
            minTime.set(Calendar.MINUTE, 0);
        }
        
        return minTime;
    }
    
    private void updateDateTimeButtons() {
        btnPickUpDate.setText(dateFormat.format(pickUpCalendar.getTime()));
        btnPickUpTime.setText(timeFormat.format(pickUpCalendar.getTime()));
        btnDropOffDate.setText(dateFormat.format(dropOffCalendar.getTime()));
        btnDropOffTime.setText(timeFormat.format(dropOffCalendar.getTime()));
    }
    
    private void setupSearchButton() {
        btnSearch.setOnClickListener(v -> {
            if (validateInputs()) {
                performSearch();
            }
        });
    }
    
    private boolean validateInputs() {
        int cityPosition = spinnerCityProvince.getSelectedItemPosition();
        
        if (cityPosition == 0 || cityPosition == AdapterView.INVALID_POSITION) {
            showToast("Please select City/Province", false);
            return false;
        }
        
        // Validate pickup time is at least 2 hours from now (using rounded up minimum)
        Calendar minPickupTime = calculateMinimumPickupTime();
        if (pickUpCalendar.before(minPickupTime)) {
            showToast("Pick-up time must be at least " + timeFormat.format(minPickupTime.getTime()), false);
            return false;
        }
        
        // Validate pickup time is within valid hours (06:00-22:00)
        int pickupHour = pickUpCalendar.get(Calendar.HOUR_OF_DAY);
        if (pickupHour < 6 || pickupHour > 22 || (pickupHour == 22 && pickUpCalendar.get(Calendar.MINUTE) > 0)) {
            showToast("Pick-up time must be between 06:00 and 22:00", false);
            return false;
        }
        
        // Validate drop-off time is at least 2 hours after pickup (rounded up to next hour)
        Calendar minDropoffTime = (Calendar) pickUpCalendar.clone();
        minDropoffTime.add(Calendar.HOUR_OF_DAY, 2);
        // Round up to next hour
        if (minDropoffTime.get(Calendar.MINUTE) > 0) {
            minDropoffTime.add(Calendar.HOUR_OF_DAY, 1);
            minDropoffTime.set(Calendar.MINUTE, 0);
        }
        if (dropOffCalendar.before(minDropoffTime)) {
            showToast("Drop-off time must be at least " + timeFormat.format(minDropoffTime.getTime()), false);
            return false;
        }
        
        // Validate drop-off time is within valid hours (06:00-22:00)
        int dropOffHour = dropOffCalendar.get(Calendar.HOUR_OF_DAY);
        if (dropOffHour < 6 || dropOffHour > 22 || (dropOffHour == 22 && dropOffCalendar.get(Calendar.MINUTE) > 0)) {
            showToast("Drop-off time must be between 06:00 and 22:00", false);
            return false;
        }
        
        return true;
    }
    
    private void performSearch() {
        // Get selected items (skip placeholder at position 0)
        String city = spinnerCityProvince.getSelectedItemPosition() > 0 
                ? spinnerCityProvince.getSelectedItem().toString() : "";
        String district = spinnerDistrict.getSelectedItemPosition() > 0 
                ? spinnerDistrict.getSelectedItem().toString() : "";
        String ward = spinnerWard.getSelectedItemPosition() > 0 
                ? spinnerWard.getSelectedItem().toString() : "";
        
        // Combine address: "City District Ward" (space-separated, matching backend CONCAT format)
        // Backend removes commas anyway, but we'll use space format to match CONCAT
        StringBuilder addressBuilder = new StringBuilder();
        if (!city.isEmpty()) {
            addressBuilder.append(city);
        }
        if (!district.isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(" ");
            addressBuilder.append(district);
        }
        if (!ward.isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(" ");
            addressBuilder.append(ward);
        }
        String address = addressBuilder.toString();
        
        // Format dates for API (ISO 8601 format)
        String pickUpTime = apiDateFormat.format(pickUpCalendar.getTime());
        String dropOffTime = apiDateFormat.format(dropOffCalendar.getTime());
        
        btnSearch.setEnabled(false);
        showToast("Searching...", true);
        
        carRepository = new CarRepository(this);
        carRepository.searchCars(address, pickUpTime, dropOffTime, 0, 10, "productionYear,desc")
                .enqueue(new Callback<SearchCarResponse>() {
                    @Override
                    public void onResponse(Call<SearchCarResponse> call, Response<SearchCarResponse> response) {
                        btnSearch.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            SearchCarResponse searchResponse = response.body();
                            
                            if (searchResponse.code == 1000 && searchResponse.data != null) {
                                List<CarThumbnailResponse> cars = searchResponse.data.content;
                                
                                // Debug: Log first car's automatic and gasoline values
                                if (cars != null && !cars.isEmpty()) {
                                    CarThumbnailResponse firstCar = cars.get(0);
//                                    android.util.Log.d("SearchCar", "First car - isAutomatic: " + firstCar.isAutomatic + ", isGasoline: " + firstCar.isGasoline);
                                    navigateToCarList(cars, address, pickUpTime, dropOffTime);
                                } else {
                                    showToast("No cars found", false);
                                }
                            } else {
                                showToast(searchResponse.message != null ? searchResponse.message : "Search failed", false);
                            }
                        } else {
                            handleSearchError(response);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<SearchCarResponse> call, Throwable t) {
                        btnSearch.setEnabled(true);
                        showToast(getErrorMessage(t), false);
                    }
                });
    }
    
    private void navigateToCarList(List<CarThumbnailResponse> cars, String address, String pickUpTime, String dropOffTime) {
        Intent intent = new Intent(this, com.anhbhn.rentcar.ui.car.list.CarListActivity.class);
        intent.putExtra("carList", new ArrayList<>(cars));
        intent.putExtra("address", address);
        intent.putExtra("pickUpTime", pickUpTime);
        intent.putExtra("dropOffTime", dropOffTime);
        
        // Pass individual address components for search box
        String city = spinnerCityProvince.getSelectedItemPosition() > 0 
                ? spinnerCityProvince.getSelectedItem().toString() : "";
        String district = spinnerDistrict.getSelectedItemPosition() > 0 
                ? spinnerDistrict.getSelectedItem().toString() : "";
        String ward = spinnerWard.getSelectedItemPosition() > 0 
                ? spinnerWard.getSelectedItem().toString() : "";
        intent.putExtra("searchCity", city);
        intent.putExtra("searchDistrict", district);
        intent.putExtra("searchWard", ward);
        
        startActivity(intent);
    }
    
    private void handleSearchError(Response<SearchCarResponse> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    com.google.gson.JsonObject jsonError = gson.fromJson(errorBody, com.google.gson.JsonObject.class);
                    String errorMessage = jsonError.has("message") ? jsonError.get("message").getAsString() : errorBody;
                    showToast("Search failed: " + errorMessage, false);
                } catch (Exception e) {
                    showToast("Search failed", false);
                }
            } else {
                showToast("Search failed (HTTP " + response.code() + ")", false);
            }
        } catch (Exception e) {
            showToast("Search failed", false);
        }
    }
    
    private String getErrorMessage(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "Cannot connect to server. Please check your connection.";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "Request timeout. Please try again.";
        } else if (t instanceof java.io.IOException) {
            return "Network error. Please try again.";
        } else {
            return "An error occurred. Please try again.";
        }
    }
    
    private void loadPreviousSearchValues() {
        Intent intent = getIntent();
        if (intent != null) {
            previousCity = intent.getStringExtra("previousCity");
            previousDistrict = intent.getStringExtra("previousDistrict");
            previousWard = intent.getStringExtra("previousWard");
            previousPickUpTime = intent.getStringExtra("previousPickUpTime");
            previousDropOffTime = intent.getStringExtra("previousDropOffTime");
            
            // Restore date/time if available
            if (previousPickUpTime != null && previousDropOffTime != null) {
                restorePreviousDateTime();
            }
        }
    }
    
    private void restorePreviousAddress() {
        if (previousCity == null || previousCity.isEmpty()) {
            return;
        }
        
        // Find city in spinner
        ArrayAdapter adapter = (ArrayAdapter) spinnerCityProvince.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                String item = (String) adapter.getItem(i);
                if (previousCity.equals(item)) {
                    spinnerCityProvince.setSelection(i);
                    // Temporarily disable listener to avoid triggering district load twice
                    spinnerCityProvince.setOnItemSelectedListener(null);
                    
                    // Load districts for the selected city
                    List<String> districts = viewModel.getFilteredDistricts(previousCity);
                    if (districts != null && !districts.isEmpty()) {
                        setupDistrictDropdown(districts);
                        
                        // Restore district selection
                        if (previousDistrict != null && !previousDistrict.isEmpty()) {
                            restoreDistrictSelection(districts);
                        }
                    }
                    
                    // Re-enable listener
                    setupCitySelectionListener();
                    break;
                }
            }
        }
    }
    
    private void restoreDistrictSelection(List<String> districts) {
        ArrayAdapter districtAdapter = (ArrayAdapter) spinnerDistrict.getAdapter();
        if (districtAdapter != null) {
            for (int i = 0; i < districtAdapter.getCount(); i++) {
                String item = (String) districtAdapter.getItem(i);
                if (previousDistrict.equals(item)) {
                    spinnerDistrict.setSelection(i);
                    // Temporarily disable listener
                    spinnerDistrict.setOnItemSelectedListener(null);
                    
                    // Load wards for the selected city and district
                    List<String> wards = viewModel.getFilteredWards(previousCity, previousDistrict);
                    if (wards != null && !wards.isEmpty()) {
                        setupWardDropdown(wards);
                        
                        // Restore ward selection
                        if (previousWard != null && !previousWard.isEmpty()) {
                            restoreWardSelection(wards);
                        }
                    }
                    
                    // Re-enable listener
                    setupDistrictSelectionListener();
                    break;
                }
            }
        }
    }
    
    private void restoreWardSelection(List<String> wards) {
        ArrayAdapter wardAdapter = (ArrayAdapter) spinnerWard.getAdapter();
        if (wardAdapter != null) {
            for (int i = 0; i < wardAdapter.getCount(); i++) {
                String item = (String) wardAdapter.getItem(i);
                if (previousWard.equals(item)) {
                    spinnerWard.setSelection(i);
                    break;
                }
            }
        }
    }
    
    private void restorePreviousDateTime() {
        try {
            Date pickUpDate = apiDateFormat.parse(previousPickUpTime);
            Date dropOffDate = apiDateFormat.parse(previousDropOffTime);
            
            if (pickUpDate != null && dropOffDate != null) {
                pickUpCalendar.setTime(pickUpDate);
                dropOffCalendar.setTime(dropOffDate);
                updateDateTimeButtons();
            }
        } catch (ParseException e) {
            // Ignore parsing errors, use default dates
        }
    }
    
    private void setupCitySelectionListener() {
        spinnerCityProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedCity = (String) parent.getItemAtPosition(position);
                    List<String> districts = viewModel.getFilteredDistricts(selectedCity);
                    setupDistrictDropdown(districts);
                    spinnerDistrict.setSelection(0);
                    setupEmptySpinner(spinnerWard, getString(R.string.select_ward));
                    spinnerWard.setEnabled(false);
                    spinnerWard.setSelection(0);
                } else {
                    setupEmptySpinner(spinnerDistrict, getString(R.string.select_district));
                    setupEmptySpinner(spinnerWard, getString(R.string.select_ward));
                    spinnerDistrict.setEnabled(false);
                    spinnerWard.setEnabled(false);
                    spinnerDistrict.setSelection(0);
                    spinnerWard.setSelection(0);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void setupDistrictSelectionListener() {
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && spinnerCityProvince.getSelectedItemPosition() > 0) {
                    String selectedCity = spinnerCityProvince.getSelectedItem().toString();
                    String selectedDistrict = (String) parent.getItemAtPosition(position);
                    List<String> wards = viewModel.getFilteredWards(selectedCity, selectedDistrict);
                    setupWardDropdown(wards);
                    spinnerWard.setEnabled(true);
                    spinnerWard.setSelection(0);
                } else {
                    setupEmptySpinner(spinnerWard, getString(R.string.select_ward));
                    spinnerWard.setEnabled(false);
                    spinnerWard.setSelection(0);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void showToast(String message, boolean success) {
        if (success) {
            Toasty.success(this, message, Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.error(this, message, Toast.LENGTH_SHORT, true).show();
        }
    }
}

