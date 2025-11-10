package com.anhbhn.rentcar.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.booking.WalletResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarDetailResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;
import com.anhbhn.rentcar.data.repository.booking.BookingRepository;
import com.anhbhn.rentcar.data.repository.car.CarRepository;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingPaymentActivity extends AppCompatActivity {

    private RadioGroup rgPaymentType;
    private RadioButton rbWallet;
    private RadioButton rbCash;
    private RadioButton rbBankTransfer;
    private TextView tvWalletBalance;
    private TextView tvDeposit;
    private Button btnConfirm;
    private Button btnBack;
    
    private String carId;
    private String pickUpTime;
    private String dropOffTime;
    private String pickUpLocation;
    private boolean isDriver;
    private String driverFullName;
    private String driverPhoneNumber;
    private String driverNationalId;
    private String driverDob;
    private String driverEmail;
    private String driverCityProvince;
    private String driverDistrict;
    private String driverWard;
    private String driverHouseNumberStreet;
    private String drivingLicensePath;
    private String renterDrivingLicensePath;
    private String renterDob;
    
    private long walletBalance = 0;
    private long deposit = 0;
    private String selectedPaymentType = "";
    private BookingRepository bookingRepository;
    private CarRepository carRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_payment);
        
        loadIntentData();
        initializeViews();
        loadWalletAndCarDetail();
        setupPaymentSelection();
        setupButtons();
    }
    
    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            carId = intent.getStringExtra("carId");
            pickUpTime = intent.getStringExtra("pickUpTime");
            dropOffTime = intent.getStringExtra("dropOffTime");
            pickUpLocation = intent.getStringExtra("pickUpLocation");
            isDriver = intent.getBooleanExtra("isDriver", false);
            driverFullName = intent.getStringExtra("driverFullName");
            driverPhoneNumber = intent.getStringExtra("driverPhoneNumber");
            driverNationalId = intent.getStringExtra("driverNationalId");
            driverDob = intent.getStringExtra("driverDob");
            driverEmail = intent.getStringExtra("driverEmail");
            driverCityProvince = intent.getStringExtra("driverCityProvince");
            driverDistrict = intent.getStringExtra("driverDistrict");
            driverWard = intent.getStringExtra("driverWard");
            driverHouseNumberStreet = intent.getStringExtra("driverHouseNumberStreet");
            drivingLicensePath = intent.getStringExtra("drivingLicensePath");
            renterDrivingLicensePath = intent.getStringExtra("renterDrivingLicensePath");
            renterDob = intent.getStringExtra("renterDob");
        }
    }
    
    private void initializeViews() {
        rgPaymentType = findViewById(R.id.rgPaymentType);
        rbWallet = findViewById(R.id.rbWallet);
        rbCash = findViewById(R.id.rbCash);
        rbBankTransfer = findViewById(R.id.rbBankTransfer);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvDeposit = findViewById(R.id.tvDeposit);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);
        
        bookingRepository = new BookingRepository(this);
        carRepository = new CarRepository(this);
    }
    
    private void loadWalletAndCarDetail() {
        // Load wallet balance
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getWallet().enqueue(new Callback<ApiResponse<WalletResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<WalletResponse>> call, Response<ApiResponse<WalletResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    walletBalance = response.body().data.balance;
                    updateWalletBalance();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<WalletResponse>> call, Throwable t) {
                // Silently fail
            }
        });
        
        // Load car detail to get deposit
        carRepository.getCarDetail(carId, pickUpTime, dropOffTime)
            .enqueue(new Callback<ApiResponse<CarDetailResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<CarDetailResponse>> call, Response<ApiResponse<CarDetailResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                        deposit = response.body().data.deposit;
                        updateDeposit();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<CarDetailResponse>> call, Throwable t) {
                    // Silently fail
                }
            });
    }
    
    private void updateWalletBalance() {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        String balanceText = "Current balance: " + formatter.format(walletBalance) + " VND";
        tvWalletBalance.setText(balanceText);
        
        // Change color based on balance
        if (walletBalance >= deposit) {
            tvWalletBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else {
            tvWalletBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        }
    }
    
    private void updateDeposit() {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        String depositText = "Deposit: " + formatter.format(deposit) + " VND";
        tvDeposit.setText(depositText);
    }
    
    private void setupPaymentSelection() {
        rgPaymentType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbWallet) {
                selectedPaymentType = "WALLET";
            } else if (checkedId == R.id.rbCash) {
                selectedPaymentType = "CASH";
            } else if (checkedId == R.id.rbBankTransfer) {
                selectedPaymentType = "BANK_TRANSFER";
            }
        });
    }
    
    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        
        btnConfirm.setOnClickListener(v -> {
            if (selectedPaymentType.isEmpty()) {
                Toasty.warning(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }
            
            createBooking();
        });
    }
    
    private void createBooking() {
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Creating booking...");
        
        File drivingLicenseFile = null;
        // If driver is different from renter, use driver's license
        // If renter is the driver, use renter's uploaded license if available
        if (isDriver) {
            // Driver is different from renter - use driver's license
            if (drivingLicensePath != null && !drivingLicensePath.isEmpty()) {
                drivingLicenseFile = new File(drivingLicensePath);
                if (!drivingLicenseFile.exists()) {
                    Toasty.error(this, "Driver driving license file not found", Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("CONFIRM PAYMENT");
                    return;
                }
            }
        } else {
            // Renter is the driver - use renter's uploaded license if available
            // Otherwise backend will use account's driving license
            if (renterDrivingLicensePath != null && !renterDrivingLicensePath.isEmpty()) {
                drivingLicenseFile = new File(renterDrivingLicensePath);
                if (!drivingLicenseFile.exists()) {
                    // If file doesn't exist, just continue - backend will use account's license
                    drivingLicenseFile = null;
                }
            }
        }
        
        // Note: Backend constructs pickUpLocation from car entity, so we can pass empty string
        // Backend will override this with the full address from the car
        bookingRepository.createBooking(
            carId,
            "", // Backend will construct pickUpLocation from car entity
            pickUpTime,
            dropOffTime,
            selectedPaymentType,
            isDriver,
            isDriver ? driverFullName : null,
            isDriver ? driverPhoneNumber : null,
            isDriver ? driverNationalId : null,
            // When isDriver = false, use renter's DOB (backend requires driverDob even when renter is driver)
            isDriver ? driverDob : renterDob,
            isDriver ? driverEmail : null,
            isDriver ? driverCityProvince : null,
            isDriver ? driverDistrict : null,
            isDriver ? driverWard : null,
            isDriver ? driverHouseNumberStreet : null,
            drivingLicenseFile
        ).enqueue(new Callback<ApiResponse<BookingResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingResponse>> call, Response<ApiResponse<BookingResponse>> response) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("CONFIRM PAYMENT");
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<BookingResponse> apiResponse = response.body();
                    if (apiResponse.code == 1000 && apiResponse.data != null) {
                        // Navigate to booking finish screen
                        Intent intent = new Intent(BookingPaymentActivity.this, BookingFinishActivity.class);
                        intent.putExtra("bookingNumber", apiResponse.data.bookingNumber);
                        intent.putExtra("status", apiResponse.data.status);
                        intent.putExtra("paymentType", apiResponse.data.paymentType);
                        intent.putExtra("pickUpTime", apiResponse.data.pickUpTime);
                        intent.putExtra("dropOffTime", apiResponse.data.dropOffTime);
                        intent.putExtra("carId", carId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toasty.error(BookingPaymentActivity.this, 
                            apiResponse.message != null ? apiResponse.message : "Failed to create booking", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response - parse JSON error body
                    String errorMessage = "Failed to create booking";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            try {
                                // Try to parse JSON error response
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                com.google.gson.JsonObject jsonError = gson.fromJson(errorBody, com.google.gson.JsonObject.class);
                                if (jsonError.has("message")) {
                                    errorMessage = jsonError.get("message").getAsString();
                                } else if (jsonError.has("code")) {
                                    int errorCode = jsonError.get("code").getAsInt();
                                    if (errorCode == 4013) {
                                        errorMessage = "Please complete your profile before booking. Go to Profile to update your information.";
                                    } else {
                                        errorMessage = jsonError.has("message") ? jsonError.get("message").getAsString() : "Failed to create booking";
                                    }
                                }
                            } catch (Exception e) {
                                // If JSON parsing fails, use error body as is if it's not empty
                                if (!errorBody.trim().isEmpty() && errorBody.length() < 200) {
                                    errorMessage = errorBody;
                                }
                            }
                        }
                    } catch (java.io.IOException e) {
                        // Use default error message if we can't read error body
                    }
                    Toasty.error(BookingPaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<BookingResponse>> call, Throwable t) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("CONFIRM PAYMENT");
                Toasty.error(BookingPaymentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

