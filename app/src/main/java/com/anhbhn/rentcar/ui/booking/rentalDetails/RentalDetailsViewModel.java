package com.anhbhn.rentcar.ui.booking.rentalDetails;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.repository.booking.BookingRepository;
import com.anhbhn.rentcar.data.repository.car.CarRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RentalDetailsViewModel extends ViewModel {

    // Lưu trữ Dữ liệu Booking Chi tiết (Lớp Data lồng bên trong)
    private final MutableLiveData<BookingResponse.Data> _bookingDetails = new MutableLiveData<>();
    public LiveData<BookingResponse.Data> getBookingDetails() { return _bookingDetails; }
    private final MutableLiveData<CarResponse.Data> _carDetails = new MutableLiveData<>();

    public LiveData<CarResponse.Data> getCarDetails() {
        return _carDetails;
    }
    private String currentBookingNumber;
    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public LiveData<String> getSuccessMessage() { return _successMessage; }

    // Trạng thái Loading và Error
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    private static final String STATUS_WAITING_CONFIRMED = "WAITING_CONFIRMED";
    private static final String STATUS_WAITING_RETURN = "WAITING_CONFIRMED_RETURN_CAR";

    private BookingRepository repository;
    private CarRepository carRepository;
    public void clearSuccessMessage() {
        _successMessage.setValue(null);
    }
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    public void initializeRepository(Context context) {
        if (repository == null) {
            // Giả định: Khởi tạo BookingRepository ở đây
            this.repository = new BookingRepository(context);
        }
        if (carRepository == null) {
            this.carRepository = new CarRepository(context);
        }
    }

    /**
     * Tải chi tiết booking từ API bằng số booking.
     */
    public void fetchBookingDetails(Context context, String bookingNumber) {
        if (bookingNumber == null || bookingNumber.isEmpty()) return;
        this.currentBookingNumber = bookingNumber;

        initializeRepository(context);
        _isLoading.setValue(true);
        _errorMessage.setValue(null);

        // Gọi API Service
        repository.getBookingDetails(bookingNumber).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    BookingResponse apiResponse = response.body();

                    if (apiResponse.getCode() == 1000 && apiResponse.getData() != null) {
                        // Lưu trữ lớp Data lồng bên trong (chứa chi tiết booking)
                        _bookingDetails.setValue(apiResponse.getData());
                        String carId = apiResponse.getData().getCarId();
                        if (carId != null) {
                            fetchCarDetails(carId);
                        } else {
                            // Nếu thiếu carId, coi như hoàn tất quá trình tải
                            _isLoading.setValue(false);
                        }
                    } else {
                        _errorMessage.setValue(apiResponse.getMessage());
                    }
                } else {
                    _errorMessage.setValue("Lỗi tải chi tiết booking: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }
    public void fetchCarDetails(String carId) {
        if (carRepository == null || carId == null) return;

        carRepository.getCarDetailsForOwner(carId).enqueue(new Callback<CarResponse>() {
            @Override
            public void onResponse(@NonNull Call<CarResponse> call, @NonNull Response<CarResponse> response) {
                _isLoading.setValue(false); // ✅ TẮT LOADING SAU KHI CẢ HAI API HOÀN TẤT

                if (response.isSuccessful() && response.body() != null) {
                    CarResponse apiResponse = response.body();
                    if (apiResponse.code == 1000 && apiResponse.data != null) {
                        _carDetails.setValue(apiResponse.data);
                    } else {
                        _errorMessage.setValue("Lỗi tải chi tiết xe: " + apiResponse.message);
                    }
                } else {
                    _errorMessage.setValue("Lỗi HTTP tải chi tiết xe: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<CarResponse> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi tải chi tiết xe.");
            }
        });
    }


    public void approveBooking(String bookingNumber, String currentStatus, Context context) {
        initializeRepository(context);
        _errorMessage.setValue(null);

        Call<BookingResponse> call;

        if (STATUS_WAITING_RETURN.equals(currentStatus)) {
            // Trường hợp Trả xe sớm
            call = repository.confirmEarlyReturnCar(bookingNumber);
        } else {
            // Mặc định: Xác nhận booking ban đầu (WAITING_CONFIRMED)
            call = repository.confirmBooking(bookingNumber);
        }

        call.enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                if (response.body() != null && response.body().getCode() == 1000) {
                    fetchBookingDetails(context, bookingNumber);
                    _successMessage.setValue("Phê duyệt booking " + bookingNumber + " thành công!");
                } else {
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Lỗi phê duyệt: " + (response.body() != null ? response.body().getMessage() : "HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi phê duyệt: " + t.getMessage());
            }
        });
    }

    /**
     * Từ chối yêu cầu booking (Bình thường hoặc Trả xe sớm).
     * 🚨 PHẢI NHẬN 3 THAM SỐ ĐỂ PHÂN BIỆT API.
     */
    public void rejectBooking(String bookingNumber, String currentStatus, Context context) {
        initializeRepository(context);
        _errorMessage.setValue(null);

        Call<BookingResponse> call;

        if (STATUS_WAITING_RETURN.equals(currentStatus)) {
            // Trường hợp Trả xe sớm
            call = repository.rejectEarlyReturnCar(bookingNumber);
        } else {
            // Mặc định: Từ chối booking ban đầu (WAITING_CONFIRMED)
            call = repository.rejectBooking(bookingNumber);
        }

        call.enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                if (response.body() != null && response.body().getCode() == 1000) {
                    fetchBookingDetails(context, bookingNumber);
                    _successMessage.setValue("Từ chối booking " + bookingNumber + " thành công!");
                } else {
                    _isLoading.setValue(false);
                    _errorMessage.setValue("Lỗi từ chối: " + (response.body() != null ? response.body().getMessage() : "HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng khi từ chối: " + t.getMessage());
            }
        });
    }
}