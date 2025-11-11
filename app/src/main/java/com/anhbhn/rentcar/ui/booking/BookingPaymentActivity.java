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

    // --- DỮ LIỆU ĐƯỢC LOAD TỪ INTENT ---
    private String carId;
    private String pickUpTime;
    private String dropOffTime;
    private String pickUpLocation;

    // 🟢 ĐỔI TÊN: Biến này xác định TÀI XẾ KHÁC người thuê
    private boolean isDriverDifferent;

    // --- THÔNG TIN TÀI XẾ (Nếu là tài xế khác) ---
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

    // --- THÔNG TIN NGƯỜI THUÊ ---
    private String renterFullName;
    private String renterPhoneNumber;
    private String renterNationalId;
    private String renterDob;
    private String renterEmail;
    private String renterCityProvince;
    private String renterDistrict;
    private String renterWard;
    private String renterHouseNumberStreet;
    private String renterDrivingLicensePath;

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

            // 🟢 LẤY GIÁ TRỊ VÀO BIẾN ĐÃ ĐỔI TÊN
            int isDriverFlagInt = intent.getIntExtra("isDriverFlag", 0);
            isDriverDifferent = (isDriverFlagInt == 1);

            // Lấy thông tin Tài xế (Nếu là tài xế khác)
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

            // LOAD THÔNG TIN NGƯỜI THUÊ
            renterFullName = intent.getStringExtra("renterFullName");
            renterPhoneNumber = intent.getStringExtra("renterPhoneNumber");
            renterNationalId = intent.getStringExtra("renterNationalId");
            renterDob = intent.getStringExtra("renterDob");
            renterEmail = intent.getStringExtra("renterEmail");
            renterCityProvince = intent.getStringExtra("renterCityProvince");
            renterDistrict = intent.getStringExtra("renterDistrict");
            renterWard = intent.getStringExtra("renterWard");
            renterHouseNumberStreet = intent.getStringExtra("renterHouseNumberStreet");
            renterDrivingLicensePath = intent.getStringExtra("renterDrivingLicensePath");
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
        // 🟢 SỬ DỤNG BIẾN ĐÃ ĐỔI TÊN
        String licensePath = isDriverDifferent ? drivingLicensePath : renterDrivingLicensePath;

        if (licensePath != null && !licensePath.isEmpty()) {
            drivingLicenseFile = new File(licensePath);
            if (!drivingLicenseFile.exists()) {
                // 🟢 SỬ DỤNG BIẾN ĐÃ ĐỔI TÊN
                if (isDriverDifferent) {
                    Toasty.error(this, "Driver driving license file not found", Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("CONFIRM PAYMENT");
                    return;
                }
                // Nếu người thuê là tài xế, ta không cần file nếu nó đã có trên Profile
                drivingLicenseFile = null;
            }
        }

        // 🟢 XỬ LÝ DỮ LIỆU TÀI XẾ DỰA TRÊN BIẾN MỚI
        // isDriverDifferent = true -> dùng driverFullName, driverDob, etc.
        // isDriverDifferent = false -> dùng renterFullName, renterDob, etc.
        boolean driverIsRenter = !isDriverDifferent;

        String finalDriverFullName = driverIsRenter ? renterFullName : driverFullName;
        String finalDriverPhoneNumber = driverIsRenter ? renterPhoneNumber : driverPhoneNumber;
        String finalDriverNationalId = driverIsRenter ? renterNationalId : driverNationalId;
        String finalDriverDob = driverIsRenter ? renterDob : driverDob;
        String finalDriverEmail = driverIsRenter ? renterEmail : driverEmail;
        String finalDriverCity = driverIsRenter ? renterCityProvince : driverCityProvince;
        String finalDriverDistrict = driverIsRenter ? renterDistrict : driverDistrict;
        String finalDriverWard = driverIsRenter ? renterWard : driverWard;
        String finalDriverHouseNumberStreet = driverIsRenter ? renterHouseNumberStreet : driverHouseNumberStreet;

        // 🟢 Đảm bảo DOB không phải NULL (nếu nó là NULL do parse lỗi)
        // Nếu DOB bị null, ta chuyển nó thành chuỗi rỗng "" để gửi đi (dựa trên logic đã sửa trước đó)
        String dobForApi = (finalDriverDob != null) ? finalDriverDob : "";


        // 🟢 SỬ DỤNG isDriverDifferent (dạng boolean) CHO THAM SỐ THỨ 6
        bookingRepository.createBooking(
                carId,
                pickUpLocation,
                pickUpTime,
                dropOffTime,
                selectedPaymentType,
                isDriverDifferent, // 🟢 ĐỔI TÊN BIẾN
                finalDriverFullName,
                finalDriverPhoneNumber,
                finalDriverNationalId,
                dobForApi, // 🟢 SỬ DỤNG GIÁ TRỊ ĐÃ XỬ LÝ (Không null)
                finalDriverEmail,
                finalDriverCity,
                finalDriverDistrict,
                finalDriverWard,
                finalDriverHouseNumberStreet,
                drivingLicenseFile
        ).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("CONFIRM PAYMENT");

                if (response.isSuccessful() && response.body() != null) {
                    BookingResponse bookingResponse = response.body();
                    if (bookingResponse.getCode() == 1000 && bookingResponse.getData() != null) {
                        // Navigate to booking finish screen
                        Intent intent = new Intent(BookingPaymentActivity.this, BookingFinishActivity.class);
                        intent.putExtra("bookingNumber", bookingResponse.getData().getBookingNumber());
                        intent.putExtra("status", bookingResponse.getData().getStatus());
                        intent.putExtra("paymentType", bookingResponse.getData().getPaymentType());
                        intent.putExtra("pickUpTime", bookingResponse.getData().getPickUpTime());
                        intent.putExtra("dropOffTime", bookingResponse.getData().getDropOffTime());
                        intent.putExtra("carId", carId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toasty.error(BookingPaymentActivity.this,
                                bookingResponse.getMessage() != null ? bookingResponse.getMessage() : "Failed to create booking",
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
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("CONFIRM PAYMENT");
                Toasty.error(BookingPaymentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}