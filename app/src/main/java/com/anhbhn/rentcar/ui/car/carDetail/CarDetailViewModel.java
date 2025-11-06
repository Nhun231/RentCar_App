package com.anhbhn.rentcar.ui.car.carDetail; // Đã đổi package thành detail

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.mapper.CarDetailMapper;
import com.anhbhn.rentcar.data.repository.car.CarRepository;
import com.anhbhn.rentcar.ui.car.CarRegistrationData; // Sửa đường dẫn DTO nội bộ
import android.util.Log;
import android.content.Context;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarDetailViewModel extends ViewModel {

    private static final String TAG = "CarDetailViewModel";

    // Giữ LiveData chính (DTO nội bộ)
    private final MutableLiveData<CarRegistrationData> registrationData =
            new MutableLiveData<>(new CarRegistrationData());

    // Thêm LiveData cho thông báo lỗi trên màn hình (nếu cần)
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    private CarRepository carRepository;

    public MutableLiveData<CarRegistrationData> getRegistrationData() {
        return registrationData;
    }
    public LiveData<String> getErrorMessage() { return _errorMessage; }


    public void initializeRepository(Context context) {
        if (carRepository == null) {
            // Giả định bạn có cách tạo instance của CarRepository
            carRepository = new CarRepository(context);
        }
    }

    /**
     * Tải dữ liệu chi tiết đầy đủ của xe từ API.
     */
    public void fetchCarDetailsForEdit(String carId) {
        if (carRepository == null) {
            Log.e(TAG, "CarRepository is not initialized! Cannot fetch details.");
            _errorMessage.setValue("Lỗi khởi tạo hệ thống.");
            return;
        }

        carRepository.getCarDetailsForOwner(carId).enqueue(new Callback<CarResponse>() {
            @Override
            public void onResponse(@NonNull Call<CarResponse> call, @NonNull Response<CarResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    CarResponse apiResponse = response.body();

                    // 1. KIỂM TRA MÃ NGHIỆP VỤ (Business Logic Code)
                    if (apiResponse.code == 1000) {

                        // 2. LẤY DỮ LIỆU TỪ LỚP CON DATA
                        CarResponse.Data detailData = apiResponse.data;

                        // 3. ÁNH XẠ DTO API SANG DTO NỘI BỘ
                        // CarDetailMapper cần được sửa để chấp nhận CarResponse.Data
                        CarRegistrationData mappedData = CarDetailMapper.mapToRegistrationData(detailData);

                        // 4. CẬP NHẬT LIVE DATA
                        registrationData.postValue(mappedData);

                        Log.i(TAG, "Car details loaded successfully.");
                    } else {
                        // Lỗi nghiệp vụ (ví dụ: Không tìm thấy xe, ID không hợp lệ)
                        String errorMsg = "Lỗi nghiệp vụ: " + apiResponse.message;
                        _errorMessage.setValue(errorMsg);
                        Log.e(TAG, errorMsg);
                    }

                } else {
                    // Lỗi HTTP (4xx hoặc 5xx)
                    String errorMsg = "Lỗi HTTP: " + response.code();
                    _errorMessage.setValue(errorMsg);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CarResponse> call, @NonNull Throwable t) {
                // Lỗi kết nối mạng
                String errorMsg = "Lỗi kết nối mạng: " + t.getMessage();
                _errorMessage.setValue(errorMsg);
                Log.e(TAG, errorMsg);
            }
        });
    }
}