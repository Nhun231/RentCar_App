package com.anhbhn.rentcar.ui.booking.myRentals;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.booking.MyRentalsListResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingListResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingThumbnailResponse;
import com.anhbhn.rentcar.data.repository.booking.BookingRepository; // Cần tạo Repository
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Collections;
import java.util.List;

public class MyRentalsViewModel extends ViewModel {

    private static final String TAG = "MyRentalsViewModel";
    private final int PAGE_SIZE = 5;

    // --- Dữ liệu danh sách ---
    private final MutableLiveData<List<BookingThumbnailResponse>> _bookingList = new MutableLiveData<>(Collections.emptyList());
    public LiveData<List<BookingThumbnailResponse>> getBookingList() { return _bookingList; }

    // --- Trạng thái tổng quan/Tải ---
    private final MutableLiveData<Integer> _totalWaitingCount = new MutableLiveData<>(0);
    public LiveData<Integer> getWaitingCount() { return _totalWaitingCount; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public LiveData<String> getSuccessMessage() { return _successMessage; }

    // --- Trạng thái Phân trang & Sort/Filter ---
    private final MutableLiveData<Integer> _currentPage = new MutableLiveData<>(0); // Index trang hiện tại (0-based)
    public LiveData<Integer> getCurrentPage() { return _currentPage; }

    private final MutableLiveData<Integer> _totalPages = new MutableLiveData<>(0);
    public LiveData<Integer> getTotalPages() { return _totalPages; }

    private final MutableLiveData<String> _pageIndicator = new MutableLiveData<>("0 / 0");
    public LiveData<String> getPageIndicator() { return _pageIndicator; }

    // Biến nội bộ quản lý trạng thái phân trang
    private int currentPageInternal = 0; // Trang hiện tại đã hiển thị (0-based)
    private int pageToLoad = 0; // Trang mục tiêu đang cố gắng tải
    private int totalPagesInternal = 0;

    private String currentStatusFilter = "ALL";
    private String currentSortBy = "updatedAt,DESC";

    private BookingRepository repository;
    private static final String STATUS_WAITING_CONFIRMED = "WAITING_CONFIRMED";
    private static final String STATUS_WAITING_RETURN = "WAITING_CONFIRMED_RETURN_CAR";

    public void initializeRepository(Context context) {
        if (this.repository == null) {
            this.repository = new BookingRepository(context);
        }
        // Tải trang đầu tiên khi ViewModel được khởi tạo
        if (_bookingList.getValue().isEmpty() && !Boolean.TRUE.equals(_isLoading.getValue())) {
            loadBookings(context, true);
        }
    }

    // --- HÀM KIỂM TRA TRẠNG THÁI ---

    public int getCurrentPageInternal() {
        return currentPageInternal;
    }

    /**
     * Trả về true nếu trang hiện tại là trang cuối cùng.
     */
    public boolean isLastPage() {
        return currentPageInternal >= totalPagesInternal - 1 && totalPagesInternal > 0;
    }
    public void clearSuccessMessage() {
        _successMessage.setValue(null);
    }
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
    // --- Logic Tải dữ liệu ---

    /**
     * Tải danh sách booking từ API.
     * @param isInitialLoad True nếu là tải lần đầu hoặc do thay đổi filter/sort.
     */
    public void loadBookings(Context context, boolean isInitialLoad) {
        if (repository == null) {
            initializeRepository(context);
            if (repository == null) return;
        }
        if (Boolean.TRUE.equals(_isLoading.getValue())) return;

        _isLoading.setValue(true);

        if (isInitialLoad) {
            pageToLoad = 0; // Reset trang mục tiêu về 0
        }

        final int finalPageToLoad = pageToLoad; // Bản sao effectively final

        repository.getOwnerBookings(
                finalPageToLoad,
                PAGE_SIZE,
                getApiStatusFilter(),
                currentSortBy
        ).enqueue(new Callback<MyRentalsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyRentalsListResponse> call, @NonNull Response<MyRentalsListResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    MyRentalsListResponse apiResponse = response.body();

                    if (apiResponse.getCode() == 1000) {
                        BookingListResponse data = apiResponse.getData();

                        // Cập nhật trạng thái phân trang nội bộ và LiveData
                        totalPagesInternal = data.getBookings().getTotalPages();
                        currentPageInternal = data.getBookings().getNumber(); // Lấy số trang từ API

                        // Cập nhật LiveData
                        _totalWaitingCount.setValue(data.getTotalWaitingConfirmBooking());
                        _bookingList.setValue(data.getBookings().getContent());
                        _currentPage.setValue(currentPageInternal);
                        _totalPages.setValue(totalPagesInternal);

                        updatePageIndicator(); // Cập nhật chỉ số phân trang

                    } else {
                        _errorMessage.setValue(apiResponse.getMessage());
                    }
                } else {
                    _errorMessage.setValue("Lỗi tải dữ liệu: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyRentalsListResponse> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // --- CÁC HÀM ĐIỀU HƯỚNG PHÂN TRANG ---

    public void goToNextPage(Context context) {
        if (currentPageInternal < totalPagesInternal - 1 && totalPagesInternal > 0) {
            pageToLoad = currentPageInternal + 1; // Tính trang mục tiêu
            loadBookings(context, false);
        } else if (totalPagesInternal == 0) {
            loadBookings(context, true); // Tải lại nếu không có trang nào
        }
    }

    public void goToPreviousPage(Context context) {
        if (currentPageInternal > 0) {
            pageToLoad = currentPageInternal - 1; // Tính trang mục tiêu
            loadBookings(context, false);
        }
    }

    // --- Logic Cập nhật Filter/Sort (Giúp tải lại trang 0) ---

    public void setStatusFilter(Context context, String newStatus) {
        if (!currentStatusFilter.equals(newStatus)) {
            currentStatusFilter = newStatus;
            loadBookings(context, true); // Luôn tải lại từ trang 0 (isInitialLoad = true)
        }
    }

    public void setSort(Context context, String newSort) {
        if (!currentSortBy.equals(newSort)) {
            currentSortBy = newSort;
            loadBookings(context, true); // Luôn tải lại từ trang 0 (isInitialLoad = true)
        }
    }

    /**
     * Cập nhật LiveData PageIndicator sau khi tải thành công.
     */
    private void updatePageIndicator() {
        if (totalPagesInternal > 0) {
            // Định dạng: Trang hiện tại (1-based) / Tổng số trang
            String indicator = (currentPageInternal + 1) + " / " + totalPagesInternal;
            _pageIndicator.setValue(indicator);
        } else {
            _pageIndicator.setValue("0 / 0");
        }
    }
    private String getApiStatusFilter() {
        // Nếu là "ALL", trả về null (hoặc "" tùy theo API Gateway/Controller xử lý)
        return currentStatusFilter.equals("ALL") ? null : currentStatusFilter;
    }

    // --- Logic Thao tác (Approve/Reject) ---
    private void reloadCurrentPage(Context context) {
        Integer currentPage = _currentPage.getValue();

        int pageToLoadSafely = (currentPage != null && currentPage >= 0) ? currentPage : 0;

        this.pageToLoad = pageToLoadSafely;

        loadBookings(context, false);
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
                    _successMessage.setValue("Phê duyệt booking " + bookingNumber + " thành công!");
                    reloadCurrentPage(context);
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
                    _successMessage.setValue("Từ chối booking " + bookingNumber + " thành công!");
                    reloadCurrentPage(context);
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