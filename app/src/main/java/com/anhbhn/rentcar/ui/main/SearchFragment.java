package com.anhbhn.rentcar.ui.main;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.anhbhn.rentcar.data.dto.response.car.SearchCarResponse;
import com.anhbhn.rentcar.data.repository.car.CarRepository;
import com.anhbhn.rentcar.ui.car.addCar.AddCarViewModel;
import com.anhbhn.rentcar.ui.car.list.CarListActivity;

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

public class SearchFragment extends Fragment {
    
    private Spinner spinnerCityProvince, spinnerDistrict, spinnerWard;
    private Button btnPickUpDate, btnPickUpTime, btnDropOffDate, btnDropOffTime;
    private Button btnSearch;
    
    private AddCarViewModel viewModel;
    private CarRepository carRepository;
    
    private Calendar pickUpCalendar = Calendar.getInstance();
    private Calendar dropOffCalendar = Calendar.getInstance();
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_search_car, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupViewModel();
        setupDateTimePickers();
        setupSearchButton();
    }
    
    private void initializeViews(View view) {
        spinnerCityProvince = view.findViewById(R.id.spinnerCityProvince);
        spinnerDistrict = view.findViewById(R.id.spinnerDistrict);
        spinnerWard = view.findViewById(R.id.spinnerWard);
        btnPickUpDate = view.findViewById(R.id.btnPickUpDate);
        btnPickUpTime = view.findViewById(R.id.btnPickUpTime);
        btnDropOffDate = view.findViewById(R.id.btnDropOffDate);
        btnDropOffTime = view.findViewById(R.id.btnDropOffTime);
        btnSearch = view.findViewById(R.id.btnSearch);
        
        setupEmptySpinner(spinnerDistrict, getString(R.string.select_district));
        setupEmptySpinner(spinnerWard, getString(R.string.select_ward));
        spinnerDistrict.setEnabled(false);
        spinnerWard.setEnabled(false);
        
        Calendar minPickupTime = calculateMinimumPickupTime();
        pickUpCalendar.setTime(minPickupTime.getTime());
        
        dropOffCalendar.setTime(pickUpCalendar.getTime());
        dropOffCalendar.add(Calendar.HOUR_OF_DAY, 2);
        if (dropOffCalendar.get(Calendar.MINUTE) > 0) {
            dropOffCalendar.add(Calendar.HOUR_OF_DAY, 1);
            dropOffCalendar.set(Calendar.MINUTE, 0);
        }
        
        int dropOffHour = dropOffCalendar.get(Calendar.HOUR_OF_DAY);
        if (dropOffHour >= 22) {
            dropOffCalendar.set(Calendar.HOUR_OF_DAY, 22);
            dropOffCalendar.set(Calendar.MINUTE, 0);
        }
        
        updateDateTimeButtons();
    }
    
    private void setupEmptySpinner(Spinner spinner, String placeholder) {
        List<String> emptyList = new ArrayList<>();
        emptyList.add(placeholder);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, emptyList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AddCarViewModel.class);
        viewModel.initializeCarOptions(requireContext());
        setupAddressDropdowns();
    }
    
    private void setupAddressDropdowns() {
        viewModel.getAvailableCities().observe(getViewLifecycleOwner(), cities -> {
            if (cities != null && !cities.isEmpty()) {
                List<String> citiesWithPlaceholder = new ArrayList<>();
                citiesWithPlaceholder.add(getString(R.string.select_city_province));
                citiesWithPlaceholder.addAll(cities);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, citiesWithPlaceholder);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCityProvince.setAdapter(adapter);
                setupCitySelectionListener();
            }
        });
    }
    
    private void setupDistrictDropdown(List<String> districts) {
        if (districts != null && !districts.isEmpty()) {
            List<String> districtsWithPlaceholder = new ArrayList<>();
            districtsWithPlaceholder.add(getString(R.string.select_district));
            districtsWithPlaceholder.addAll(districts);
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, districtsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDistrict.setAdapter(adapter);
            spinnerDistrict.setEnabled(true);
            setupDistrictSelectionListener();
        } else {
            spinnerDistrict.setEnabled(false);
        }
    }
    
    private void setupWardDropdown(List<String> wards) {
        if (wards != null && !wards.isEmpty()) {
            List<String> wardsWithPlaceholder = new ArrayList<>();
            wardsWithPlaceholder.add(getString(R.string.select_ward));
            wardsWithPlaceholder.addAll(wards);
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, wardsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWard.setAdapter(adapter);
            spinnerWard.setEnabled(true);
        } else {
            spinnerWard.setEnabled(false);
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
                } else {
                    setupEmptySpinner(spinnerDistrict, getString(R.string.select_district));
                    setupEmptySpinner(spinnerWard, getString(R.string.select_ward));
                    spinnerDistrict.setEnabled(false);
                    spinnerWard.setEnabled(false);
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
                } else {
                    setupEmptySpinner(spinnerWard, getString(R.string.select_ward));
                    spinnerWard.setEnabled(false);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void setupDateTimePickers() {
        btnPickUpDate.setOnClickListener(v -> showDatePicker(true));
        btnPickUpTime.setOnClickListener(v -> showTimePicker(true));
        btnDropOffDate.setOnClickListener(v -> showDatePicker(false));
        btnDropOffTime.setOnClickListener(v -> showTimePicker(false));
    }
    
    private void showDatePicker(boolean isPickUp) {
        Calendar calendar = isPickUp ? pickUpCalendar : dropOffCalendar;
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    Calendar today = Calendar.getInstance();
                    boolean isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                     selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
                    
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    if (isPickUp) {
                        if (isToday) {
                            Calendar minPickupTime = calculateMinimumPickupTime();
                            if (pickUpCalendar.before(minPickupTime)) {
                                pickUpCalendar.set(Calendar.HOUR_OF_DAY, minPickupTime.get(Calendar.HOUR_OF_DAY));
                                pickUpCalendar.set(Calendar.MINUTE, minPickupTime.get(Calendar.MINUTE));
                            }
                        } else {
                            if (pickUpCalendar.get(Calendar.HOUR_OF_DAY) < 6) {
                                pickUpCalendar.set(Calendar.HOUR_OF_DAY, 6);
                                pickUpCalendar.set(Calendar.MINUTE, 0);
                            }
                        }
                    } else {
                        Calendar minDropoffTime = (Calendar) pickUpCalendar.clone();
                        minDropoffTime.add(Calendar.HOUR_OF_DAY, 2);
                        if (minDropoffTime.get(Calendar.MINUTE) > 0) {
                            minDropoffTime.add(Calendar.HOUR_OF_DAY, 1);
                            minDropoffTime.set(Calendar.MINUTE, 0);
                        }
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
        
        Calendar minTime = null;
        if (isPickUp) {
            if (selectedDate.equals(today)) {
                minTime = calculateMinimumPickupTime();
            } else {
                minTime = (Calendar) selectedDate.clone();
                minTime.set(Calendar.HOUR_OF_DAY, 6);
                minTime.set(Calendar.MINUTE, 0);
            }
        } else {
            minTime = (Calendar) pickUpCalendar.clone();
            minTime.add(Calendar.HOUR_OF_DAY, 2);
            if (minTime.get(Calendar.MINUTE) > 0) {
                minTime.add(Calendar.HOUR_OF_DAY, 1);
                minTime.set(Calendar.MINUTE, 0);
            }
            Calendar minDate = (Calendar) minTime.clone();
            minDate.set(Calendar.HOUR_OF_DAY, 0);
            minDate.set(Calendar.MINUTE, 0);
            if (!minDate.equals(selectedDate)) {
                minTime = (Calendar) selectedDate.clone();
                minTime.set(Calendar.HOUR_OF_DAY, 6);
                minTime.set(Calendar.MINUTE, 0);
            }
        }
        
        final Calendar finalMinTime = minTime;
        int initialHour = calendar.get(Calendar.HOUR_OF_DAY);
        int initialMinute = calendar.get(Calendar.MINUTE);
        
        if (finalMinTime != null && calendar.before(finalMinTime)) {
            initialHour = finalMinTime.get(Calendar.HOUR_OF_DAY);
            initialMinute = finalMinTime.get(Calendar.MINUTE);
        }
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    Calendar selectedTime = (Calendar) calendar.clone();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    
                    if (finalMinTime != null && selectedTime.before(finalMinTime)) {
                        showToast("Selected time must be at least " + 
                                timeFormat.format(finalMinTime.getTime()), false);
                        return;
                    }
                    
                    if (hourOfDay < 6 || hourOfDay > 22 || (hourOfDay == 22 && minute > 0)) {
                        showToast("Time must be between 06:00 and 22:00", false);
                        return;
                    }
                    
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    
                    if (isPickUp) {
                        Calendar newMinDropoff = (Calendar) calendar.clone();
                        newMinDropoff.add(Calendar.HOUR_OF_DAY, 2);
                        if (newMinDropoff.get(Calendar.MINUTE) > 0) {
                            newMinDropoff.add(Calendar.HOUR_OF_DAY, 1);
                            newMinDropoff.set(Calendar.MINUTE, 0);
                        }
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
    
    private Calendar calculateMinimumPickupTime() {
        Calendar now = Calendar.getInstance();
        Calendar minTime = (Calendar) now.clone();
        minTime.add(Calendar.HOUR_OF_DAY, 2);
        if (minTime.get(Calendar.MINUTE) > 0) {
            minTime.add(Calendar.HOUR_OF_DAY, 1);
            minTime.set(Calendar.MINUTE, 0);
        } else {
            minTime.set(Calendar.MINUTE, 0);
        }
        minTime.set(Calendar.SECOND, 0);
        minTime.set(Calendar.MILLISECOND, 0);
        
        int hour = minTime.get(Calendar.HOUR_OF_DAY);
        if (hour >= 22) {
            minTime.add(Calendar.DAY_OF_MONTH, 1);
            minTime.set(Calendar.HOUR_OF_DAY, 8);
            minTime.set(Calendar.MINUTE, 0);
        } else if (hour < 6) {
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
        
        Calendar minPickupTime = calculateMinimumPickupTime();
        if (pickUpCalendar.before(minPickupTime)) {
            showToast("Pick-up time must be at least " + timeFormat.format(minPickupTime.getTime()), false);
            return false;
        }
        
        int pickupHour = pickUpCalendar.get(Calendar.HOUR_OF_DAY);
        if (pickupHour < 6 || pickupHour > 22 || (pickupHour == 22 && pickUpCalendar.get(Calendar.MINUTE) > 0)) {
            showToast("Pick-up time must be between 06:00 and 22:00", false);
            return false;
        }
        
        Calendar minDropoffTime = (Calendar) pickUpCalendar.clone();
        minDropoffTime.add(Calendar.HOUR_OF_DAY, 2);
        if (minDropoffTime.get(Calendar.MINUTE) > 0) {
            minDropoffTime.add(Calendar.HOUR_OF_DAY, 1);
            minDropoffTime.set(Calendar.MINUTE, 0);
        }
        if (dropOffCalendar.before(minDropoffTime)) {
            showToast("Drop-off time must be at least " + timeFormat.format(minDropoffTime.getTime()), false);
            return false;
        }
        
        int dropOffHour = dropOffCalendar.get(Calendar.HOUR_OF_DAY);
        if (dropOffHour < 6 || dropOffHour > 22 || (dropOffHour == 22 && dropOffCalendar.get(Calendar.MINUTE) > 0)) {
            showToast("Drop-off time must be between 06:00 and 22:00", false);
            return false;
        }
        
        return true;
    }
    
    private void performSearch() {
        String city = spinnerCityProvince.getSelectedItemPosition() > 0 
                ? spinnerCityProvince.getSelectedItem().toString() : "";
        String district = spinnerDistrict.getSelectedItemPosition() > 0 
                ? spinnerDistrict.getSelectedItem().toString() : "";
        String ward = spinnerWard.getSelectedItemPosition() > 0 
                ? spinnerWard.getSelectedItem().toString() : "";
        
        StringBuilder addressBuilder = new StringBuilder();
        if (!city.isEmpty()) addressBuilder.append(city);
        if (!district.isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(" ");
            addressBuilder.append(district);
        }
        if (!ward.isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(" ");
            addressBuilder.append(ward);
        }
        String address = addressBuilder.toString();
        
        String pickUpTime = apiDateFormat.format(pickUpCalendar.getTime());
        String dropOffTime = apiDateFormat.format(dropOffCalendar.getTime());
        
        btnSearch.setEnabled(false);
        showToast("Searching...", true);
        
        carRepository = new CarRepository(requireContext());
        carRepository.searchCars(address, pickUpTime, dropOffTime, 0, 10, "productionYear,desc")
                .enqueue(new Callback<SearchCarResponse>() {
                    @Override
                    public void onResponse(Call<SearchCarResponse> call, Response<SearchCarResponse> response) {
                        btnSearch.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            SearchCarResponse searchResponse = response.body();
                            
                            if (searchResponse.code == 1000 && searchResponse.data != null) {
                                List<CarThumbnailResponse> cars = searchResponse.data.content;
                                
                                if (cars != null && !cars.isEmpty()) {
                                    navigateToCarList(cars, address, pickUpTime, dropOffTime);
                                } else {
                                    showToast("No cars found", false);
                                }
                            } else {
                                showToast(searchResponse.message != null ? searchResponse.message : "Search failed", false);
                            }
                        } else {
                            showToast("Search failed", false);
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<SearchCarResponse> call, Throwable t) {
                        btnSearch.setEnabled(true);
                        showToast("Network error: " + t.getMessage(), false);
                    }
                });
    }
    
    private void navigateToCarList(List<CarThumbnailResponse> cars, String address, String pickUpTime, String dropOffTime) {
        Intent intent = new Intent(requireContext(), CarListActivity.class);
        intent.putExtra("carList", new ArrayList<>(cars));
        intent.putExtra("address", address);
        intent.putExtra("pickUpTime", pickUpTime);
        intent.putExtra("dropOffTime", dropOffTime);
        
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
    
    private void showToast(String message, boolean success) {
        if (success) {
            Toasty.success(requireContext(), message, Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.error(requireContext(), message, Toast.LENGTH_SHORT, true).show();
        }
    }
}

