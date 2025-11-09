package com.anhbhn.rentcar.ui.booking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.repository.booking.BookingRepository;
import com.anhbhn.rentcar.ui.car.addCar.AddCarViewModel;

import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingInformationActivity extends AppCompatActivity {

    // Renter information EditTexts (editable, pre-filled from account)
    private EditText etRenterFullName;
    private EditText etRenterPhoneNumber;
    private EditText etRenterNationalId;
    private EditText etRenterDob;
    private EditText etRenterEmail;
    private Button btnUploadRenterLicense;
    private TextView tvRenterLicenseFileName;
    private Spinner spinnerRenterCityProvince;
    private Spinner spinnerRenterDistrict;
    private Spinner spinnerRenterWard;
    private EditText etRenterHouseNumberStreet;
    
    private Calendar renterDobCalendar = Calendar.getInstance();

    // Driver information EditTexts (only shown when checkbox is checked)
    private EditText etDriverFullName;
    private EditText etDriverPhoneNumber;
    private EditText etDriverNationalId;
    private EditText etDriverDob;
    private EditText etDriverEmail;
    private Spinner spinnerDriverCityProvince;
    private Spinner spinnerDriverDistrict;
    private Spinner spinnerDriverWard;
    private EditText etDriverHouseNumberStreet;
    private CheckBox cbDifferentDriver;
    private LinearLayout layoutDriverInfo;
    private Button btnUploadLicense;
    private Button btnNext;
    private TextView tvLicenseFileName;
    
    private String carId;
    private String pickUpTime;
    private String dropOffTime;
    private String pickUpLocation;
    private File drivingLicenseFile;
    private File renterDrivingLicenseFile;
    private Calendar dobCalendar = Calendar.getInstance();
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> renterFilePickerLauncher;
    
    // Store user profile data
    private com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse userProfile;
    
    // Address data ViewModel
    private AddCarViewModel addressViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_information);
        
        loadIntentData();
        initializeViews();
        setupViewModel();
        setupFilePicker();
        setupRenterFilePicker();
        loadUserProfile();
        setupDatePicker();
        setupRenterDatePicker();
        setupCheckBox();
        setupButtons();
        setupAddressSpinners();
    }
    
    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            carId = intent.getStringExtra("carId");
            pickUpTime = intent.getStringExtra("pickUpTime");
            dropOffTime = intent.getStringExtra("dropOffTime");
            pickUpLocation = intent.getStringExtra("pickUpLocation");
        }
    }
    
    private void initializeViews() {
        // Initialize renter information EditTexts
        etRenterFullName = findViewById(R.id.etRenterFullName);
        etRenterPhoneNumber = findViewById(R.id.etRenterPhoneNumber);
        etRenterNationalId = findViewById(R.id.etRenterNationalId);
        etRenterDob = findViewById(R.id.etRenterDob);
        etRenterEmail = findViewById(R.id.etRenterEmail);
        btnUploadRenterLicense = findViewById(R.id.btnUploadRenterLicense);
        tvRenterLicenseFileName = findViewById(R.id.tvRenterLicenseFileName);
        spinnerRenterCityProvince = findViewById(R.id.spinnerRenterCityProvince);
        spinnerRenterDistrict = findViewById(R.id.spinnerRenterDistrict);
        spinnerRenterWard = findViewById(R.id.spinnerRenterWard);
        etRenterHouseNumberStreet = findViewById(R.id.etRenterHouseNumberStreet);

        // Initialize driver information EditTexts
        etDriverFullName = findViewById(R.id.etDriverFullName);
        etDriverPhoneNumber = findViewById(R.id.etDriverPhoneNumber);
        etDriverNationalId = findViewById(R.id.etDriverNationalId);
        etDriverDob = findViewById(R.id.etDriverDob);
        etDriverEmail = findViewById(R.id.etDriverEmail);
        spinnerDriverCityProvince = findViewById(R.id.spinnerDriverCityProvince);
        spinnerDriverDistrict = findViewById(R.id.spinnerDriverDistrict);
        spinnerDriverWard = findViewById(R.id.spinnerDriverWard);
        etDriverHouseNumberStreet = findViewById(R.id.etDriverHouseNumberStreet);
        cbDifferentDriver = findViewById(R.id.cbDifferentDriver);
        layoutDriverInfo = findViewById(R.id.layoutDriverInfo);
        btnUploadLicense = findViewById(R.id.btnUploadLicense);
        btnNext = findViewById(R.id.btnNext);
        tvLicenseFileName = findViewById(R.id.tvLicenseFileName);
        
        // Initially hide driver info (driver is same as renter)
        layoutDriverInfo.setVisibility(View.GONE);
    }
    
    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleFileSelection(uri, true);
                    }
                }
            }
        );
    }
    
    private void setupRenterFilePicker() {
        renterFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleFileSelection(uri, false);
                    }
                }
            }
        );
    }
    
    private void handleFileSelection(Uri uri, boolean isDriverLicense) {
                        try {
                            // Get file path from URI
                            String filePath = getPathFromUri(uri);
            File selectedFile = null;
                            if (filePath != null) {
                selectedFile = new File(filePath);
                if (!selectedFile.exists()) {
                                    // If file doesn't exist, copy it to app's cache directory
                    selectedFile = copyUriToFile(uri);
                                }
                            } else {
                                // Copy file from URI to cache directory
                selectedFile = copyUriToFile(uri);
            }
            
            if (selectedFile != null && selectedFile.exists()) {
                if (isDriverLicense) {
                    drivingLicenseFile = selectedFile;
                                    tvLicenseFileName.setText(drivingLicenseFile.getName());
                                    tvLicenseFileName.setVisibility(View.VISIBLE);
                } else {
                    renterDrivingLicenseFile = selectedFile;
                    tvRenterLicenseFileName.setText(renterDrivingLicenseFile.getName());
                    tvRenterLicenseFileName.setVisibility(View.VISIBLE);
                                }
            } else {
                Toasty.error(this, "Failed to get file", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toasty.error(this, "Failed to get file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
    }
    
    private String getPathFromUri(Uri uri) {
        String path = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("primary".equalsIgnoreCase(uri.getAuthority())) {
                path = Environment.getExternalStorageDirectory() + "/" + docId;
            }
        } else {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    path = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        return path;
    }
    
    private File copyUriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            // Extract original file extension from URI
            String fileExtension = getFileExtensionFromUri(uri);
            // If no extension found, default to .jpg for images
            if (fileExtension == null || fileExtension.isEmpty()) {
                fileExtension = "jpg";
            }
            
            File cacheDir = getCacheDir();
            String fileName = "driving_license_" + System.currentTimeMillis() + "." + fileExtension;
            File file = new File(cacheDir, fileName);
            
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            
            return file;
        } catch (Exception e) {
            android.util.Log.e("BookingInformation", "Error copying file from URI", e);
            return null;
        }
    }
    
    private String getFileExtensionFromUri(Uri uri) {
        String extension = null;
        try {
            // Try to get extension from URI path
            String uriString = uri.toString();
            if (uriString != null) {
                int lastDot = uriString.lastIndexOf('.');
                int lastSlash = uriString.lastIndexOf('/');
                if (lastDot > lastSlash && lastDot < uriString.length() - 1) {
                    extension = uriString.substring(lastDot + 1).toLowerCase();
                    // Remove any query parameters
                    int queryIndex = extension.indexOf('?');
                    if (queryIndex > 0) {
                        extension = extension.substring(0, queryIndex);
                    }
                }
            }
            
            // If not found in URI, try to get from ContentResolver
            if (extension == null || extension.isEmpty()) {
                try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        // Try to get display name
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex >= 0) {
                            String displayName = cursor.getString(nameIndex);
                            if (displayName != null) {
                                int lastDot = displayName.lastIndexOf('.');
                                if (lastDot > 0 && lastDot < displayName.length() - 1) {
                                    extension = displayName.substring(lastDot + 1).toLowerCase();
                                }
                            }
                        }
                    }
                }
            }
            
            // Validate extension (only allow formats accepted by backend: .doc, .docx, .pdf, .jpeg, .jpg, .png)
            if (extension != null && !extension.isEmpty()) {
                String[] allowedExtensions = {"doc", "docx", "pdf", "jpeg", "jpg", "png"};
                for (String allowed : allowedExtensions) {
                    if (allowed.equals(extension)) {
                        return extension;
                    }
                }
                // If extension not in allowed list, default to jpg
                return "jpg";
            }
        } catch (Exception e) {
            android.util.Log.e("BookingInformation", "Error getting file extension from URI", e);
        }
        
        // Default to jpg if we can't determine the extension
        return "jpg";
    }
    
    private void loadUserProfile() {
        com.anhbhn.rentcar.data.remote.ApiService apiService = com.anhbhn.rentcar.data.remote.ApiClient.getClient(this).create(com.anhbhn.rentcar.data.remote.ApiService.class);
        apiService.getUserProfile().enqueue(new Callback<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>>() {
            @Override
            public void onResponse(Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> call, Response<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse> apiResponse = response.body();
                    if (apiResponse.code == 1000 && apiResponse.data != null) {
                        userProfile = apiResponse.data;
                        displayRenterInformation();
                        // After displaying information, try to select address in spinners
                        selectRenterAddressFromProfile();
                    } else {
                        Toasty.error(BookingInformationActivity.this, apiResponse.message != null ? apiResponse.message : "Failed to load user profile", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toasty.error(BookingInformationActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> call, Throwable t) {
                Toasty.error(BookingInformationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupViewModel() {
        addressViewModel = new ViewModelProvider(this).get(AddCarViewModel.class);
        // Initialize address data from database.json
        addressViewModel.initializeCarOptions(this);
    }
    
    private void setupAddressSpinners() {
        // Setup renter address spinners
        setupRenterAddressSpinners();
        // Setup driver address spinners
        setupDriverAddressSpinners();
    }
    
    private void setupRenterAddressSpinners() {
        // Observe city list and setup city spinner
        addressViewModel.getAvailableCities().observe(this, cities -> {
            if (cities != null && !cities.isEmpty()) {
                List<String> citiesWithPlaceholder = new ArrayList<>();
                citiesWithPlaceholder.add("Select City/Province");
                citiesWithPlaceholder.addAll(cities);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, citiesWithPlaceholder);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRenterCityProvince.setAdapter(adapter);
                
                // Set up city selection listener
                setupRenterCitySelectionListener();
                
                // If user profile has address, try to select it after adapter is set
                // This will be handled by selectRenterAddressFromProfile() after profile loads
            }
        });
        
        // Initially disable district and ward spinners
        setupEmptySpinner(spinnerRenterDistrict, "Select District");
        setupEmptySpinner(spinnerRenterWard, "Select Ward");
        spinnerRenterDistrict.setEnabled(false);
        spinnerRenterWard.setEnabled(false);
    }
    
    private void setupDriverAddressSpinners() {
        // Observe city list and setup city spinner
        addressViewModel.getAvailableCities().observe(this, cities -> {
            if (cities != null && !cities.isEmpty()) {
                List<String> citiesWithPlaceholder = new ArrayList<>();
                citiesWithPlaceholder.add("Select City/Province");
                citiesWithPlaceholder.addAll(cities);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, citiesWithPlaceholder);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDriverCityProvince.setAdapter(adapter);
                
                // Set up city selection listener
                setupDriverCitySelectionListener();
            }
        });
        
        // Initially disable district and ward spinners
        setupEmptySpinner(spinnerDriverDistrict, "Select District");
        setupEmptySpinner(spinnerDriverWard, "Select Ward");
        spinnerDriverDistrict.setEnabled(false);
        spinnerDriverWard.setEnabled(false);
    }
    
    private void setupEmptySpinner(Spinner spinner, String placeholder) {
        List<String> emptyList = new ArrayList<>();
        emptyList.add(placeholder);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, emptyList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    private void setupRenterCitySelectionListener() {
        spinnerRenterCityProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Not placeholder
                    String selectedCity = (String) parent.getItemAtPosition(position);
                    // Load districts for selected city
                    List<String> districts = addressViewModel.getFilteredDistricts(selectedCity);
                    setupRenterDistrictDropdown(districts);
                    
                    // Reset ward when city changes
                    setupEmptySpinner(spinnerRenterWard, "Select Ward");
                    spinnerRenterWard.setEnabled(false);
                    
                    // Try to select district from user profile if available (only if city matches)
                    if (userProfile != null && userProfile.district != null && !userProfile.district.isEmpty()
                            && userProfile.cityProvince != null && userProfile.cityProvince.equals(selectedCity)) {
                        int districtPosition = districts.indexOf(userProfile.district);
                        if (districtPosition >= 0) {
                            // Post to ensure adapter is set, then select
                            spinnerRenterDistrict.post(() -> {
                                spinnerRenterDistrict.setSelection(districtPosition + 1, true); // true to trigger listener
                            });
                        }
                    }
                } else {
                    // Placeholder selected, disable district and ward
                    setupEmptySpinner(spinnerRenterDistrict, "Select District");
                    setupEmptySpinner(spinnerRenterWard, "Select Ward");
                    spinnerRenterDistrict.setEnabled(false);
                    spinnerRenterWard.setEnabled(false);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void setupRenterDistrictDropdown(List<String> districts) {
        if (districts != null && !districts.isEmpty()) {
            List<String> districtsWithPlaceholder = new ArrayList<>();
            districtsWithPlaceholder.add("Select District");
            districtsWithPlaceholder.addAll(districts);
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, districtsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRenterDistrict.setAdapter(adapter);
            spinnerRenterDistrict.setEnabled(true);
            
            // Set up district selection listener
            spinnerRenterDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) { // Not placeholder
                        String selectedCity = (String) spinnerRenterCityProvince.getSelectedItem();
                        String selectedDistrict = (String) parent.getItemAtPosition(position);
                        // Load wards for selected city and district
                        List<String> wards = addressViewModel.getFilteredWards(selectedCity, selectedDistrict);
                        setupRenterWardDropdown(wards);
                        
                        // Try to select ward from user profile if available (only if district matches)
                        if (userProfile != null && userProfile.ward != null && !userProfile.ward.isEmpty() 
                                && userProfile.district != null && userProfile.district.equals(selectedDistrict)) {
                            int wardPosition = wards.indexOf(userProfile.ward);
                            if (wardPosition >= 0) {
                                // Post to ensure adapter is set
                                spinnerRenterWard.post(() -> {
                                    spinnerRenterWard.setSelection(wardPosition + 1);
                                });
                            }
                        }
                    } else {
                        setupEmptySpinner(spinnerRenterWard, "Select Ward");
                        spinnerRenterWard.setEnabled(false);
                    }
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        } else {
            spinnerRenterDistrict.setEnabled(false);
        }
    }
    
    private void setupRenterWardDropdown(List<String> wards) {
        if (wards != null && !wards.isEmpty()) {
            List<String> wardsWithPlaceholder = new ArrayList<>();
            wardsWithPlaceholder.add("Select Ward");
            wardsWithPlaceholder.addAll(wards);
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, wardsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRenterWard.setAdapter(adapter);
            spinnerRenterWard.setEnabled(true);
        } else {
            spinnerRenterWard.setEnabled(false);
        }
    }
    
    private void setupDriverCitySelectionListener() {
        spinnerDriverCityProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Not placeholder
                    String selectedCity = (String) parent.getItemAtPosition(position);
                    // Load districts for selected city
                    List<String> districts = addressViewModel.getFilteredDistricts(selectedCity);
                    setupDriverDistrictDropdown(selectedCity, districts);
                    
                    // Reset ward when city changes
                    setupEmptySpinner(spinnerDriverWard, "Select Ward");
                    spinnerDriverWard.setEnabled(false);
                } else {
                    // Placeholder selected, disable district and ward
                    setupEmptySpinner(spinnerDriverDistrict, "Select District");
                    setupEmptySpinner(spinnerDriverWard, "Select Ward");
                    spinnerDriverDistrict.setEnabled(false);
                    spinnerDriverWard.setEnabled(false);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void setupDriverDistrictDropdown(String selectedCity, List<String> districts) {
        if (districts != null && !districts.isEmpty()) {
            List<String> districtsWithPlaceholder = new ArrayList<>();
            districtsWithPlaceholder.add("Select District");
            districtsWithPlaceholder.addAll(districts);
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, districtsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDriverDistrict.setAdapter(adapter);
            spinnerDriverDistrict.setEnabled(true);
            
            // Set up district selection listener
            spinnerDriverDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) { // Not placeholder
                        String selectedDistrict = (String) parent.getItemAtPosition(position);
                        // Load wards for selected city and district
                        List<String> wards = addressViewModel.getFilteredWards(selectedCity, selectedDistrict);
                        setupDriverWardDropdown(wards);
                    } else {
                        setupEmptySpinner(spinnerDriverWard, "Select Ward");
                        spinnerDriverWard.setEnabled(false);
                    }
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        } else {
            spinnerDriverDistrict.setEnabled(false);
        }
    }
    
    private void setupDriverWardDropdown(List<String> wards) {
        if (wards != null && !wards.isEmpty()) {
            List<String> wardsWithPlaceholder = new ArrayList<>();
            wardsWithPlaceholder.add("Select Ward");
            wardsWithPlaceholder.addAll(wards);
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, wardsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDriverWard.setAdapter(adapter);
            spinnerDriverWard.setEnabled(true);
        } else {
            spinnerDriverWard.setEnabled(false);
        }
    }
    
    private void displayRenterInformation() {
        if (userProfile == null) return;
        
        // Pre-fill renter information from account (only if blank, allow editing)
        if (userProfile.fullName != null && !userProfile.fullName.isEmpty()) {
            etRenterFullName.setText(userProfile.fullName);
        }
        if (userProfile.phoneNumber != null && !userProfile.phoneNumber.isEmpty()) {
            etRenterPhoneNumber.setText(userProfile.phoneNumber);
        }
        if (userProfile.nationalId != null && !userProfile.nationalId.isEmpty()) {
            etRenterNationalId.setText(userProfile.nationalId);
        }
        if (userProfile.dob != null && !userProfile.dob.isEmpty()) {
            // Convert from "yyyy-MM-dd" to "DD/MM/YYYY" format and set calendar
            try {
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                java.util.Date date = apiFormat.parse(userProfile.dob);
                if (date != null) {
                    etRenterDob.setText(displayFormat.format(date));
                    renterDobCalendar.setTime(date);
                }
            } catch (Exception e) {
                etRenterDob.setText(userProfile.dob);
            }
        }
        if (userProfile.email != null && !userProfile.email.isEmpty()) {
            etRenterEmail.setText(userProfile.email);
        }
        // Note: If user has a driving license in profile, it will be used by backend
        // But user can still upload a new one here if needed
        
        // Address selection in spinners will be handled in setupRenterAddressSpinners()
        // when the city list is loaded, since we need to wait for the data to be available
        
        // Only set house number/street (free text input)
        if (userProfile.houseNumberStreet != null && !userProfile.houseNumberStreet.isEmpty()) {
            etRenterHouseNumberStreet.setText(userProfile.houseNumberStreet);
        }
    }
    
    private void selectRenterAddressFromProfile() {
        if (userProfile == null) return;
        
        // Wait for cities to be loaded
        List<String> cities = addressViewModel.getAvailableCities().getValue();
        if (cities == null || cities.isEmpty()) {
            // Cities not loaded yet, wait for them
            addressViewModel.getAvailableCities().observe(this, citiesList -> {
                if (citiesList != null && !citiesList.isEmpty()) {
                    selectRenterAddressFromProfile();
                }
            });
            return;
        }
        
        // Select city if available
        if (userProfile.cityProvince != null && !userProfile.cityProvince.isEmpty()) {
            int cityPosition = cities.indexOf(userProfile.cityProvince);
            if (cityPosition >= 0) {
                // +1 for placeholder
                spinnerRenterCityProvince.setSelection(cityPosition + 1, true); // true to trigger listener to load districts
                
                // District and ward selection will be handled in the city/district selection listeners
                // They check userProfile for values
            }
        }
    }
    
    private void setupRenterDatePicker() {
        etRenterDob.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    renterDobCalendar.set(Calendar.YEAR, year);
                    renterDobCalendar.set(Calendar.MONTH, month);
                    renterDobCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateRenterDobField();
                },
                renterDobCalendar.get(Calendar.YEAR),
                renterDobCalendar.get(Calendar.MONTH),
                renterDobCalendar.get(Calendar.DAY_OF_MONTH)
            );
            // Set max date to today (must be at least 18 years old)
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, -18);
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });
    }
    
    private void updateRenterDobField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etRenterDob.setText(sdf.format(renterDobCalendar.getTime()));
    }
    
    private void setupDatePicker() {
        etDriverDob.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dobCalendar.set(Calendar.YEAR, year);
                    dobCalendar.set(Calendar.MONTH, month);
                    dobCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDobField();
                },
                dobCalendar.get(Calendar.YEAR),
                dobCalendar.get(Calendar.MONTH),
                dobCalendar.get(Calendar.DAY_OF_MONTH)
            );
            // Set max date to today (must be at least 18 years old)
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, -18);
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });
    }
    
    private void updateDobField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDriverDob.setText(sdf.format(dobCalendar.getTime()));
    }
    
    private void setupCheckBox() {
        cbDifferentDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show driver info fields
                layoutDriverInfo.setVisibility(View.VISIBLE);
                // Clear driver fields
                clearDriverFields();
            } else {
                // Hide driver info fields
                layoutDriverInfo.setVisibility(View.GONE);
                // Clear driver fields
                clearDriverFields();
                drivingLicenseFile = null;
                tvLicenseFileName.setVisibility(View.GONE);
            }
        });
    }
    
    private void clearDriverFields() {
        etDriverFullName.setText("");
        etDriverPhoneNumber.setText("");
        etDriverNationalId.setText("");
        etDriverDob.setText("");
        etDriverEmail.setText("");
        // Reset address spinners to placeholder (index 0)
        spinnerDriverCityProvince.setSelection(0);
        spinnerDriverDistrict.setSelection(0);
        spinnerDriverWard.setSelection(0);
        etDriverHouseNumberStreet.setText("");
    }
    
    private String getSpinnerSelectedValue(Spinner spinner) {
        if (spinner.getSelectedItemPosition() > 0) {
            return spinner.getSelectedItem().toString();
        }
        return "";
    }
    
    private void setupButtons() {
        // Driver license upload
        btnUploadLicense.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimeTypes = {"image/*", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select Driving License"));
        });
        
        // Renter license upload
        btnUploadRenterLicense.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimeTypes = {"image/*", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            renterFilePickerLauncher.launch(Intent.createChooser(intent, "Select Driving License"));
        });
        
        btnNext.setOnClickListener(v -> {
            if (validateInputs()) {
                // Update profile first with renter information, then proceed to payment
                updateProfileAndProceed();
            }
        });
    }
    
    private void updateProfileAndProceed() {
        btnNext.setEnabled(false);
        btnNext.setText("Updating profile...");
        
        // Get renter information from EditTexts and Spinners
        String fullName = etRenterFullName.getText().toString().trim();
        String phoneNumber = etRenterPhoneNumber.getText().toString().trim();
        String nationalId = etRenterNationalId.getText().toString().trim();
        String email = etRenterEmail.getText().toString().trim();
        String cityProvince = getSpinnerSelectedValue(spinnerRenterCityProvince);
        String district = getSpinnerSelectedValue(spinnerRenterDistrict);
        String ward = getSpinnerSelectedValue(spinnerRenterWard);
        String houseNumberStreet = etRenterHouseNumberStreet.getText().toString().trim();
        
        // Convert DOB from DD/MM/YYYY to yyyy-MM-dd format for API
        String dobStr = "";
        if (!TextUtils.isEmpty(etRenterDob.getText())) {
            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date date = displayFormat.parse(etRenterDob.getText().toString().trim());
                if (date != null) {
                    dobStr = apiFormat.format(date);
                }
            } catch (Exception e) {
                dobStr = etRenterDob.getText().toString().trim();
            }
        }
        
        // Prepare multipart request
        java.util.Map<String, okhttp3.RequestBody> fields = new java.util.HashMap<>();
        fields.put("fullName", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), fullName));
        fields.put("phoneNumber", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), phoneNumber));
        fields.put("nationalId", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), nationalId));
        fields.put("email", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), email));
        if (!dobStr.isEmpty()) {
            fields.put("dob", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), dobStr));
        }
        if (!cityProvince.isEmpty()) {
            fields.put("cityProvince", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), cityProvince));
        }
        if (!district.isEmpty()) {
            fields.put("district", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), district));
        }
        if (!ward.isEmpty()) {
            fields.put("ward", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), ward));
        }
        if (!houseNumberStreet.isEmpty()) {
            fields.put("houseNumberStreet", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), houseNumberStreet));
        }
        
        // Handle driving license file
        okhttp3.MultipartBody.Part licensePart = null;
        if (renterDrivingLicenseFile != null && renterDrivingLicenseFile.exists()) {
            String fileName = renterDrivingLicenseFile.getName().toLowerCase();
            String mediaType = "application/octet-stream";
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                mediaType = "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                mediaType = "image/png";
            } else if (fileName.endsWith(".pdf")) {
                mediaType = "application/pdf";
            } else if (fileName.endsWith(".doc")) {
                mediaType = "application/msword";
            } else if (fileName.endsWith(".docx")) {
                mediaType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            
            okhttp3.RequestBody fileRequestBody = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse(mediaType),
                    renterDrivingLicenseFile
            );
            licensePart = okhttp3.MultipartBody.Part.createFormData(
                    "drivingLicense",
                    renterDrivingLicenseFile.getName(),
                    fileRequestBody
            );
        }
        
        // Call API to update profile
        com.anhbhn.rentcar.data.remote.ApiService apiService = com.anhbhn.rentcar.data.remote.ApiClient.getClient(this).create(com.anhbhn.rentcar.data.remote.ApiService.class);
        apiService.editProfile(fields, licensePart).enqueue(new Callback<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>>() {
            @Override
            public void onResponse(Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> call, Response<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> response) {
                btnNext.setEnabled(true);
                btnNext.setText("NEXT");
                
                if (response.isSuccessful() && response.body() != null) {
                    com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse> apiResponse = response.body();
                    if (apiResponse.code == 1000) {
                        // Profile updated successfully, proceed to payment screen
                        proceedToPaymentScreen();
                    } else {
                        // Parse error message
                        String errorMessage = apiResponse.message != null ? apiResponse.message : "Failed to update profile";
                        Toasty.error(BookingInformationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Handle error response
                    String errorMessage = "Failed to update profile";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                com.google.gson.JsonObject jsonError = gson.fromJson(errorBody, com.google.gson.JsonObject.class);
                                if (jsonError.has("message")) {
                                    errorMessage = jsonError.get("message").getAsString();
                                }
                            } catch (Exception e) {
                                if (!errorBody.trim().isEmpty() && errorBody.length() < 200) {
                                    errorMessage = errorBody;
                                }
                            }
                        }
                    } catch (java.io.IOException e) {
                        // Use default error message
                    }
                    Toasty.error(BookingInformationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> call, Throwable t) {
                btnNext.setEnabled(true);
                btnNext.setText("NEXT");
                Toasty.error(BookingInformationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void proceedToPaymentScreen() {
                // Navigate to payment selection screen
                Intent intent = new Intent(this, BookingPaymentActivity.class);
                intent.putExtra("carId", carId);
                intent.putExtra("pickUpTime", pickUpTime);
                intent.putExtra("dropOffTime", dropOffTime);
                intent.putExtra("pickUpLocation", pickUpLocation);
        
        // Pass renter information (updated from EditTexts)
        intent.putExtra("renterFullName", etRenterFullName.getText().toString().trim());
        intent.putExtra("renterPhoneNumber", etRenterPhoneNumber.getText().toString().trim());
        intent.putExtra("renterNationalId", etRenterNationalId.getText().toString().trim());
        // Convert DOB from DD/MM/YYYY to yyyy-MM-dd format for API
        String renterDob = "";
        if (!TextUtils.isEmpty(etRenterDob.getText())) {
            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date date = displayFormat.parse(etRenterDob.getText().toString().trim());
                if (date != null) {
                    renterDob = apiFormat.format(date);
                }
            } catch (Exception e) {
                renterDob = etRenterDob.getText().toString().trim();
            }
        }
        intent.putExtra("renterDob", renterDob);
        intent.putExtra("renterEmail", etRenterEmail.getText().toString().trim());
        intent.putExtra("renterCityProvince", getSpinnerSelectedValue(spinnerRenterCityProvince));
        intent.putExtra("renterDistrict", getSpinnerSelectedValue(spinnerRenterDistrict));
        intent.putExtra("renterWard", getSpinnerSelectedValue(spinnerRenterWard));
        intent.putExtra("renterHouseNumberStreet", etRenterHouseNumberStreet.getText().toString().trim());
        // Pass renter driving license file if uploaded
        if (renterDrivingLicenseFile != null && renterDrivingLicenseFile.exists()) {
            intent.putExtra("renterDrivingLicensePath", renterDrivingLicenseFile.getAbsolutePath());
        }
        
        // isDriver = true if driver is different from renter (checkbox checked)
        // isDriver = false if renter is the driver (checkbox unchecked, use renter info)
        boolean isDriverDifferent = cbDifferentDriver.isChecked();
        intent.putExtra("isDriver", isDriverDifferent);
        
        if (isDriverDifferent) {
            // Driver is different from renter - pass driver information
                intent.putExtra("driverFullName", etDriverFullName.getText().toString().trim());
                intent.putExtra("driverPhoneNumber", etDriverPhoneNumber.getText().toString().trim());
                intent.putExtra("driverNationalId", etDriverNationalId.getText().toString().trim());
            // Convert DOB from DD/MM/YYYY to yyyy-MM-dd format for API
            String driverDob = "";
            if (!TextUtils.isEmpty(etDriverDob.getText())) {
                try {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    java.util.Date date = displayFormat.parse(etDriverDob.getText().toString().trim());
                    if (date != null) {
                        driverDob = apiFormat.format(date);
                    }
                } catch (Exception e) {
                    driverDob = etDriverDob.getText().toString().trim();
                }
            }
            intent.putExtra("driverDob", driverDob);
                intent.putExtra("driverEmail", etDriverEmail.getText().toString().trim());
            intent.putExtra("driverCityProvince", getSpinnerSelectedValue(spinnerDriverCityProvince));
            intent.putExtra("driverDistrict", getSpinnerSelectedValue(spinnerDriverDistrict));
            intent.putExtra("driverWard", getSpinnerSelectedValue(spinnerDriverWard));
                intent.putExtra("driverHouseNumberStreet", etDriverHouseNumberStreet.getText().toString().trim());
                if (drivingLicenseFile != null) {
                    intent.putExtra("drivingLicensePath", drivingLicenseFile.getAbsolutePath());
                }
        }
        
                startActivity(intent);
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Validate that user profile is loaded (renter information)
        if (userProfile == null) {
            Toasty.error(this, "Please wait while loading your profile information", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate required renter information fields (from EditTexts, not userProfile)
        if (TextUtils.isEmpty(etRenterFullName.getText())) {
            etRenterFullName.setError("Full name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etRenterPhoneNumber.getText())) {
            etRenterPhoneNumber.setError("Phone number is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etRenterEmail.getText())) {
            etRenterEmail.setError("Email is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etRenterNationalId.getText())) {
            etRenterNationalId.setError("National ID is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etRenterDob.getText())) {
            etRenterDob.setError("Date of birth is required");
            isValid = false;
        }
        // Validate address spinners
        if (spinnerRenterCityProvince.getSelectedItemPosition() == 0) {
            Toasty.error(this, "Please select City/Province", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (spinnerRenterDistrict.getSelectedItemPosition() == 0) {
            Toasty.error(this, "Please select District", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (spinnerRenterWard.getSelectedItemPosition() == 0) {
            Toasty.error(this, "Please select Ward", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (TextUtils.isEmpty(etRenterHouseNumberStreet.getText())) {
            etRenterHouseNumberStreet.setError("House number and street is required");
            isValid = false;
        }
        
        // Check if driving license is required (if user doesn't have one in profile and hasn't uploaded one)
        if ((userProfile == null || userProfile.drivingLicenseUrl == null || userProfile.drivingLicenseUrl.isEmpty()) 
                && (renterDrivingLicenseFile == null || !renterDrivingLicenseFile.exists())) {
            Toasty.warning(this, "Please upload your driving license to complete your profile", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        
        // Validate driver information if driver is different from renter
        if (cbDifferentDriver.isChecked()) {
            // Validate driver information if different driver
            if (TextUtils.isEmpty(etDriverFullName.getText())) {
                etDriverFullName.setError("Full name is required");
                isValid = false;
            }
            if (TextUtils.isEmpty(etDriverPhoneNumber.getText())) {
                etDriverPhoneNumber.setError("Phone number is required");
                isValid = false;
            }
            if (TextUtils.isEmpty(etDriverNationalId.getText())) {
                etDriverNationalId.setError("National ID is required");
                isValid = false;
            }
            if (TextUtils.isEmpty(etDriverDob.getText())) {
                etDriverDob.setError("Date of birth is required");
                isValid = false;
            }
            if (TextUtils.isEmpty(etDriverEmail.getText())) {
                etDriverEmail.setError("Email is required");
                isValid = false;
            }
            // Validate driver address spinners
            if (spinnerDriverCityProvince.getSelectedItemPosition() == 0) {
                Toasty.error(this, "Please select Driver City/Province", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
            if (spinnerDriverDistrict.getSelectedItemPosition() == 0) {
                Toasty.error(this, "Please select Driver District", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
            if (spinnerDriverWard.getSelectedItemPosition() == 0) {
                Toasty.error(this, "Please select Driver Ward", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
            if (TextUtils.isEmpty(etDriverHouseNumberStreet.getText())) {
                etDriverHouseNumberStreet.setError("House number and street is required");
                isValid = false;
            }
            if (drivingLicenseFile == null || !drivingLicenseFile.exists()) {
                Toasty.warning(this, "Please upload driving license", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        }
        
        return isValid;
    }
}

