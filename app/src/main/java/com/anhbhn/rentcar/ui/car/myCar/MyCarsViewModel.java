package com.anhbhn.rentcar.ui.car.myCar;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.anhbhn.rentcar.data.dto.response.car.MyCarsPageResponse;
import com.anhbhn.rentcar.data.dto.helper.PageResponse;
import com.anhbhn.rentcar.data.repository.car.CarRepository;
import android.util.Log;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCarsViewModel extends ViewModel {

    private static final String TAG = "MyCarsViewModel";
    private String currentSortParameter = "productionYear,DESC";
    private final MutableLiveData<String> _pageIndicator = new MutableLiveData<>("0 / 0");
    public LiveData<String> getPageIndicator() { return _pageIndicator; }

    // --- LiveData for UI ---
    private final MutableLiveData<List<CarThumbnailResponse>> _carList = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<CarThumbnailResponse>> getCarList() { return _carList; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    // --- Pagination State & Repository ---
    private CarRepository carRepository;
    private int currentPage = 0;
    private int pageToLoad = 0; // Trang mục tiêu đang cố gắng tải
    private final int pageSize = 5;
    private int totalPages = 0;

    public int getCurrentPage() {
        return currentPage;
    }

    public MyCarsViewModel() {
        // Khởi tạo không tham số
    }

    /**
     * Initializes the CarRepository. Must be called from the Activity/Fragment lifecycle.
     */
    public void initializeRepository(Context context) {
        if (carRepository == null) {
            carRepository = new CarRepository(context);
        }
        // Tải trang đầu tiên
        if (_carList.getValue().isEmpty() && !Boolean.TRUE.equals(_isLoading.getValue())) {
            loadCars(true);
        }
    }

    /**
     * Returns the current last page status.
     */
    public boolean isLastPage() {
        // Trả về true nếu trang hiện tại là trang cuối cùng (totalPages - 1)
        return currentPage >= totalPages - 1 && totalPages > 0;
    }

    /**
     * Loads car data from the API, handling pagination and loading state.
     */
    public void loadCars(boolean isInitialLoad) {
        if (carRepository == null) {
            Log.e(TAG, "CarRepository is not initialized!");
            _errorMessage.setValue("Error initializing data repository.");
            return;
        }
        if (Boolean.TRUE.equals(_isLoading.getValue())) return;

        _isLoading.setValue(true);

        if (isInitialLoad) {
            pageToLoad = 0; // 🌟 RESET TRANG MỤC TIÊU KHI TẢI LẠI 🌟
            currentPage = 0;
            totalPages = 0;
        }

        Log.d(TAG, "Loading page: " + pageToLoad);

        // Gọi API với TRANG MỤC TIÊU
        carRepository.getMyCars(pageToLoad, pageSize, currentSortParameter)
                .enqueue(new Callback<MyCarsPageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MyCarsPageResponse> call, @NonNull Response<MyCarsPageResponse> response) {
                        _isLoading.setValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            MyCarsPageResponse rootResponse = response.body();

                            if (rootResponse.getCode() == 1000 && rootResponse.getData() != null) {

                                PageResponse<CarThumbnailResponse> pageData = rootResponse.getData();
                                List<CarThumbnailResponse> newCars = pageData.getContent();

                                // Cập nhật trạng thái nội bộ sau khi tải thành công
                                currentPage = pageData.getNumber();
                                totalPages = pageData.getTotalPages();
                                // isLastPage được cập nhật thông qua hàm isLastPage()

                                int currentPageDisplay = pageData.getNumber() + 1;
                                _pageIndicator.setValue(String.format("%d / %d", currentPageDisplay, totalPages));

                                // Thay thế dữ liệu hiện tại
                                _carList.setValue(newCars);
                            } else {
                                // Lỗi nghiệp vụ
                                _errorMessage.setValue("Business error: " + rootResponse.getMessage());
                            }
                        } else {
                            // Lỗi HTTP
                            _errorMessage.setValue("HTTP Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MyCarsPageResponse> call, @NonNull Throwable t) {
                        _isLoading.setValue(false);

                        // KHÔNG CẦN ROLLBACK TRANG ở đây vì pageToLoad/currentPage không bị thay đổi trước đó
                        _errorMessage.setValue("Network failure: " + t.getMessage());
                    }
                });
    }

    public void setCurrentSortParameter(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            sortParam = "productionYear,DESC";
        }

        if (!this.currentSortParameter.equals(sortParam)) {
            this.currentSortParameter = sortParam;
            loadCars(true); // Tải lại từ trang 0
        }
    }

    // ------------------------------------------
    // CÁC HÀM ĐIỀU HƯỚNG
    // ------------------------------------------

    public void goToNextPage() {
        if (currentPage < totalPages - 1 && totalPages > 0) {
            pageToLoad = currentPage + 1; // Tính trang tiếp theo
            loadCars(false);
        } else if (totalPages == 0) {
            loadCars(true);
        }
    }

    public void goToPrevPage() {
        if (currentPage > 0) {
            pageToLoad = currentPage - 1; // Tính trang trước đó
            loadCars(false);
        }
    }

    /**
     * Trigger a full refresh of the car list (starts from page 0).
     */
    public void refreshCars() {
        loadCars(true);
    }
}