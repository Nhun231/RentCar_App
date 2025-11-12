package com.anhbhn.rentcar.ui.profile;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;
import com.anhbhn.rentcar.ui.car.addCar.AddCarViewModel; // Import ViewModel xử lý Address

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    // Khai báo Views
    EditText etFullName, etPhoneNumber, etNationalId, etDob, etEmail, etHouseNumberStreet;
    Spinner spinnerCityProvince, spinnerDistrict, spinnerWard;
    Button btnUploadLicense, btnSaveProfile;
    TextView tvLicenseFileName;

    // Data and State
    private File drivingLicenseFile;
    private EditProfileResponse userProfile;
    private Calendar dobCalendar = Calendar.getInstance();
    private ActivityResultLauncher<Intent> filePickerLauncher;

    // Address data ViewModel
    private AddCarViewModel addressViewModel;

    // --- Lifecycle Methods ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        setupViewModel();
        setupFilePicker();
        loadUserProfile();
        setupDatePicker();
        setupListeners();
        setupAddressSpinners();
    }

    // --- 1. View Initialization ---

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etNationalId = findViewById(R.id.etNationalId);
        etDob = findViewById(R.id.etDob);
        etEmail = findViewById(R.id.etEmail);
        etHouseNumberStreet = findViewById(R.id.etHouseNumberStreet);

        spinnerCityProvince = findViewById(R.id.spinnerCityProvince);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);

        btnUploadLicense = findViewById(R.id.btnUploadLicense);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        tvLicenseFileName = findViewById(R.id.tvLicenseFileName);
    }

    private void setupViewModel() {
        addressViewModel = new ViewModelProvider(this).get(AddCarViewModel.class);
        addressViewModel.initializeCarOptions(this);
    }

    // --- 2. Data Loading & Display ---

    private void loadUserProfile() {
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getUserProfile().enqueue(new Callback<ApiResponse<EditProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<EditProfileResponse>> call, Response<ApiResponse<EditProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<EditProfileResponse> apiResponse = response.body();
                    if (apiResponse.code == 1000 && apiResponse.data != null) {
                        userProfile = apiResponse.data;
                        displayUserProfileInformation();
                        selectAddressFromProfile();
                    } else {
                        Toasty.error(EditProfileActivity.this, apiResponse.message != null ? apiResponse.message : "Failed to load user profile", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toasty.error(EditProfileActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<EditProfileResponse>> call, Throwable t) {
                Toasty.error(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserProfileInformation() {
        if (userProfile == null) return;

        if (userProfile.fullName != null) etFullName.setText(userProfile.fullName);
        if (userProfile.phoneNumber != null) etPhoneNumber.setText(userProfile.phoneNumber);
        if (userProfile.nationalId != null) etNationalId.setText(userProfile.nationalId);
        if (userProfile.email != null) etEmail.setText(userProfile.email); // Read-only

        // Date of Birth
        if (userProfile.dob != null && !userProfile.dob.isEmpty()) {
            try {
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                java.util.Date date = apiFormat.parse(userProfile.dob);
                if (date != null) {
                    etDob.setText(displayFormat.format(date));
                    dobCalendar.setTime(date);
                }
            } catch (Exception ignored) {
                etDob.setText(userProfile.dob);
            }
        }

        // Driving License URL status
        if (userProfile.drivingLicenseUrl != null && !userProfile.drivingLicenseUrl.isEmpty()) {
            tvLicenseFileName.setText("Current file: Available");
            tvLicenseFileName.setVisibility(View.VISIBLE);
        } else {
            tvLicenseFileName.setText("No license uploaded yet.");
            tvLicenseFileName.setVisibility(View.VISIBLE);
        }

        if (userProfile.houseNumberStreet != null) etHouseNumberStreet.setText(userProfile.houseNumberStreet);
    }

    // --- 3. Listeners & Navigation ---

    private void setupListeners() {
        // Upload Giấy phép
        btnUploadLicense.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimeTypes = {"image/*", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select Driving License"));
        });

        // Lưu Hồ sơ
        btnSaveProfile.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserProfile();
            }
        });
    }

    // --- 4. Date Picker Logic ---

    private void setupDatePicker() {
        etDob.setOnClickListener(v -> {
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
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.YEAR, -18);
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });
    }

    private void updateDobField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDob.setText(sdf.format(dobCalendar.getTime()));
    }

    // --- 5. Address Spinners Logic (Heavy lifting from BookingInfo) ---

    private void setupAddressSpinners() {
        addressViewModel.getAvailableCities().observe(this, cities -> {
            if (cities != null && !cities.isEmpty()) {
                List<String> citiesWithPlaceholder = new ArrayList<>();
                citiesWithPlaceholder.add("Select City/Province");
                citiesWithPlaceholder.addAll(cities);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, citiesWithPlaceholder);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCityProvince.setAdapter(adapter);

                setupCitySelectionListener();

                if (userProfile != null) {
                    selectAddressFromProfile();
                }
            }
        });

        setupEmptySpinner(spinnerDistrict, "Select District");
        setupEmptySpinner(spinnerWard, "Select Ward");
        spinnerDistrict.setEnabled(false);
        spinnerWard.setEnabled(false);
    }

    private void setupEmptySpinner(Spinner spinner, String placeholder) {
        List<String> emptyList = new ArrayList<>();
        emptyList.add(placeholder);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, emptyList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupCitySelectionListener() {
        spinnerCityProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedCity = (String) parent.getItemAtPosition(position);
                    List<String> districts = addressViewModel.getFilteredDistricts(selectedCity);
                    setupDistrictDropdown(districts);

                    setupEmptySpinner(spinnerWard, "Select Ward");
                    spinnerWard.setEnabled(false);

                    // Try to select district from user profile
                    if (userProfile != null && userProfile.district != null && userProfile.cityProvince != null && userProfile.cityProvince.equals(selectedCity)) {
                        int districtPosition = districts.indexOf(userProfile.district);
                        if (districtPosition >= 0) {
                            spinnerDistrict.post(() -> spinnerDistrict.setSelection(districtPosition + 1, true));
                        }
                    }
                } else {
                    setupEmptySpinner(spinnerDistrict, "Select District");
                    setupEmptySpinner(spinnerWard, "Select Ward");
                    spinnerDistrict.setEnabled(false);
                    spinnerWard.setEnabled(false);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDistrictDropdown(List<String> districts) {
        if (districts != null && !districts.isEmpty()) {
            List<String> districtsWithPlaceholder = new ArrayList<>();
            districtsWithPlaceholder.add("Select District");
            districtsWithPlaceholder.addAll(districts);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districtsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDistrict.setAdapter(adapter);
            spinnerDistrict.setEnabled(true);

            spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) {
                        String selectedCity = (String) spinnerCityProvince.getSelectedItem();
                        String selectedDistrict = (String) parent.getItemAtPosition(position);
                        List<String> wards = addressViewModel.getFilteredWards(selectedCity, selectedDistrict);
                        setupWardDropdown(wards);

                        // Try to select ward from user profile
                        if (userProfile != null && userProfile.ward != null && userProfile.district != null && userProfile.district.equals(selectedDistrict)) {
                            int wardPosition = wards.indexOf(userProfile.ward);
                            if (wardPosition >= 0) {
                                spinnerWard.post(() -> spinnerWard.setSelection(wardPosition + 1));
                            }
                        }
                    } else {
                        setupEmptySpinner(spinnerWard, "Select Ward");
                        spinnerWard.setEnabled(false);
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        } else {
            spinnerDistrict.setEnabled(false);
        }
    }

    private void setupWardDropdown(List<String> wards) {
        if (wards != null && !wards.isEmpty()) {
            List<String> wardsWithPlaceholder = new ArrayList<>();
            wardsWithPlaceholder.add("Select Ward");
            wardsWithPlaceholder.addAll(wards);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wardsWithPlaceholder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWard.setAdapter(adapter);
            spinnerWard.setEnabled(true);
        } else {
            spinnerWard.setEnabled(false);
        }
    }

    private void selectAddressFromProfile() {
        if (userProfile == null || TextUtils.isEmpty(userProfile.cityProvince)) return;

        List<String> cities = addressViewModel.getAvailableCities().getValue();
        if (cities == null || cities.isEmpty()) return;

        int cityPosition = cities.indexOf(userProfile.cityProvince);
        if (cityPosition >= 0) {
            // +1 for placeholder
            spinnerCityProvince.setSelection(cityPosition + 1, true); // true to trigger listener to load districts
        }
    }

    private String getSpinnerSelectedValue(Spinner spinner) {
        if (spinner.getSelectedItemPosition() > 0) {
            return spinner.getSelectedItem().toString();
        }
        return "";
    }

    // --- 6. Input Validation ---

    private boolean validateInputs() {
        boolean isValid = true;

        if (userProfile == null) {
            Toasty.error(this, "Please wait while loading your profile information", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(etFullName.getText())) { etFullName.setError("Full name is required"); isValid = false; }
        if (TextUtils.isEmpty(etPhoneNumber.getText())) { etPhoneNumber.setError("Phone number is required"); isValid = false; }
        if (TextUtils.isEmpty(etNationalId.getText())) { etNationalId.setError("National ID is required"); isValid = false; }
        if (TextUtils.isEmpty(etDob.getText())) { etDob.setError("Date of birth is required"); isValid = false; }

        // Address spinners
        if (spinnerCityProvince.getSelectedItemPosition() == 0) { Toasty.error(this, "Please select City/Province", Toast.LENGTH_SHORT).show(); isValid = false; }
        if (spinnerDistrict.getSelectedItemPosition() == 0) { Toasty.error(this, "Please select District", Toast.LENGTH_SHORT).show(); isValid = false; }
        if (spinnerWard.getSelectedItemPosition() == 0) { Toasty.error(this, "Please select Ward", Toast.LENGTH_SHORT).show(); isValid = false; }
        if (TextUtils.isEmpty(etHouseNumberStreet.getText())) { etHouseNumberStreet.setError("House number and street is required"); isValid = false; }

        // Driving license check
        boolean licenseInProfile = userProfile.drivingLicenseUrl != null && !userProfile.drivingLicenseUrl.isEmpty();
        boolean licenseUploaded = drivingLicenseFile != null && drivingLicenseFile.exists();

        if (!licenseInProfile && !licenseUploaded) {
            Toasty.warning(this, "Please upload your driving license to complete your profile", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        return isValid;
    }

    // --- 7. Save Profile Logic (API Call) ---

    private void saveUserProfile() {
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving changes...");

        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String nationalId = etNationalId.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String cityProvince = getSpinnerSelectedValue(spinnerCityProvince);
        String district = getSpinnerSelectedValue(spinnerDistrict);
        String ward = getSpinnerSelectedValue(spinnerWard);
        String houseNumberStreet = etHouseNumberStreet.getText().toString().trim();
        String dobStr = getDobForApi();

        // Prepare multipart request fields
        java.util.Map<String, okhttp3.RequestBody> fields = new java.util.HashMap<>();
        fields.put("fullName", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), fullName));
        fields.put("phoneNumber", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), phoneNumber));
        fields.put("nationalId", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), nationalId));
        fields.put("email", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), email));

        if (dobStr != null) { fields.put("dob", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), dobStr)); }
        if (!cityProvince.isEmpty()) { fields.put("cityProvince", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), cityProvince)); }
        if (!district.isEmpty()) { fields.put("district", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), district)); }
        if (!ward.isEmpty()) { fields.put("ward", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), ward)); }
        if (!houseNumberStreet.isEmpty()) { fields.put("houseNumberStreet", okhttp3.RequestBody.create(okhttp3.MediaType.parse("text/plain"), houseNumberStreet)); }

        // Handle driving license file
        okhttp3.MultipartBody.Part licensePart = null;
        if (drivingLicenseFile != null && drivingLicenseFile.exists()) {
            String fileName = drivingLicenseFile.getName().toLowerCase();
            String mediaType = "application/octet-stream";
            // Simple check for media type
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) mediaType = "image/jpeg";
            else if (fileName.endsWith(".png")) mediaType = "image/png";
            else if (fileName.endsWith(".pdf")) mediaType = "application/pdf";

            okhttp3.RequestBody fileRequestBody = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse(mediaType), drivingLicenseFile);
            licensePart = okhttp3.MultipartBody.Part.createFormData(
                    "drivingLicense", drivingLicenseFile.getName(), fileRequestBody);
        }

        // Call API
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.editProfile(fields, licensePart).enqueue(new Callback<ApiResponse<EditProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<EditProfileResponse>> call, Response<ApiResponse<EditProfileResponse>> response) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("SAVE CHANGES");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<EditProfileResponse> apiResponse = response.body();
                    if (apiResponse.code == 1000) {
                        Toasty.success(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        // Tải lại dữ liệu mới để cập nhật UI
                        userProfile = apiResponse.data;
                        displayUserProfileInformation();
                    } else {
                        String errorMessage = apiResponse.message != null ? apiResponse.message : "Failed to update profile";
                        Toasty.error(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    handleApiError(response);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<EditProfileResponse>> call, Throwable t) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("SAVE CHANGES");
                Toasty.error(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- 8. Helper Methods (Date & File Handling) ---

    private String getDobForApi() {
        String convertedDob = safeConvertDateToApiFormat(etDob);

        if (convertedDob != null) { return convertedDob; }
        if (userProfile != null && userProfile.dob != null && !userProfile.dob.isEmpty()) { return userProfile.dob; }

        return null;
    }

    private String safeConvertDateToApiFormat(EditText editText) {
        String dobText = editText.getText().toString().trim();
        if (TextUtils.isEmpty(dobText)) return null;
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            displayFormat.setLenient(false);
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            java.util.Date date = displayFormat.parse(dobText);
            if (date != null) { return apiFormat.format(date); }
        } catch (Exception e) {
            android.util.Log.e("EditProfile", "DOB Parsing Failed for: " + dobText, e);
        }
        return null;
    }

    private void handleApiError(Response<ApiResponse<EditProfileResponse>> response) {
        String errorMessage = "Failed to update profile";
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    com.google.gson.JsonObject jsonError = gson.fromJson(errorBody, com.google.gson.JsonObject.class);
                    if (jsonError.has("message")) { errorMessage = jsonError.get("message").getAsString(); }
                } catch (Exception e) {
                    if (!errorBody.trim().isEmpty() && errorBody.length() < 200) { errorMessage = errorBody; }
                }
            }
        } catch (java.io.IOException ignored) {}
        Toasty.error(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showToast(String message, boolean success) {
        if (success) { Toasty.success(this, message, Toast.LENGTH_SHORT, true).show(); }
        else { Toasty.error(this, message, Toast.LENGTH_SHORT, true).show(); }
    }

    // --- File Handling Logic (Copied from BookingActivity) ---

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) { handleFileSelection(uri); }
                    }
                }
        );
    }

    private void handleFileSelection(Uri uri) {
        try {
            String filePath = getPathFromUri(uri);
            File selectedFile = null;
            if (filePath != null) {
                selectedFile = new File(filePath);
                if (!selectedFile.exists()) { selectedFile = copyUriToFile(uri); }
            } else { selectedFile = copyUriToFile(uri); }

            if (selectedFile != null && selectedFile.exists()) {
                drivingLicenseFile = selectedFile;
                tvLicenseFileName.setText("New file: " + drivingLicenseFile.getName());
                tvLicenseFileName.setVisibility(View.VISIBLE);
            } else { Toasty.error(this, "Failed to get file", Toast.LENGTH_SHORT).show(); }
        } catch (Exception e) {
            Toasty.error(this, "Failed to get file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // NOTE: Các hàm dưới đây cần được cung cấp trong file của bạn (hoặc copy từ BookingInformationActivity)

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
}