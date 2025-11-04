package com.anhbhn.rentcar.ui.car.myCar;

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
    private final int pageSize = 10;
    private boolean isLastPage = false;
    private final String defaultSort = "productionYear,DESC";

    public MyCarsViewModel() {
        // Khởi tạo không tham số để sử dụng ViewModelProvider mặc định
    }

    /**
     * Initializes the CarRepository. Must be called from the Activity/Fragment lifecycle.
     * @param context Application/Activity context.
     */
    public void initializeRepository(Context context) {
        if (carRepository == null) {
            carRepository = new CarRepository(context);
        }
        // Load first page if list is empty and not currently loading
        if (_carList.getValue().isEmpty() && !Boolean.TRUE.equals(_isLoading.getValue())) {
            loadCars(true);
        }
    }

    /**
     * Returns the current last page status. Used by the PaginationScrollListener.
     */
    public boolean isLastPage() {
        return isLastPage;
    }

    /**
     * Loads car data from the API, handling pagination and loading state.
     * @param isInitialLoad True if it's the first load or a Refresh operation.
     */
    public void loadCars(boolean isInitialLoad) {
        if (carRepository == null) {
            Log.e(TAG, "CarRepository is not initialized!");
            _errorMessage.setValue("Error initializing data repository.");
            return;
        }

        if (isLastPage && !isInitialLoad) {
            Log.d(TAG, "Reached last page.");
            return;
        }
        if (Boolean.TRUE.equals(_isLoading.getValue())) {
            Log.d(TAG, "Already loading, ignoring request.");
            return;
        }

        _isLoading.setValue(true);
        if (isInitialLoad) {
            currentPage = 0;
            isLastPage = false;
        } else {
            currentPage++;
        }

        Log.d(TAG, "Loading page: " + currentPage);

        carRepository.getMyCars(currentPage, pageSize, defaultSort)
                .enqueue(new Callback<MyCarsPageResponse>() {
                    @Override
                    public void onResponse(Call<MyCarsPageResponse> call, Response<MyCarsPageResponse> response) {
                        _isLoading.setValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            MyCarsPageResponse rootResponse = response.body();

                            if (rootResponse.getCode() == 1000) {

                                PageResponse<CarThumbnailResponse> pageData = rootResponse.getData();
                                List<CarThumbnailResponse> newCars = pageData.getContent();

                                // Update pagination state
                                isLastPage = pageData.isLast();

                                // Update LiveData (Handle Refresh vs Load More)
                                if (isInitialLoad) {
                                    _carList.setValue(newCars);
                                } else {
                                    List<CarThumbnailResponse> currentList = _carList.getValue();
                                    if (currentList == null) currentList = new ArrayList<>();
                                    currentList.addAll(newCars);
                                    _carList.setValue(currentList);
                                }
                            } else {
                                // Business error from server
                                String msg = "Business error: " + rootResponse.getMessage();
                                _errorMessage.setValue(msg);
                                Log.e(TAG, msg);
                            }
                        } else {
                            // HTTP Error
                            String msg = "HTTP Error: " + response.code() + ". Message: " + response.message();
                            _errorMessage.setValue(msg);
                            Log.e(TAG, msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<MyCarsPageResponse> call, Throwable t) {
                        _isLoading.setValue(false);
                        String msg = "Network failure: " + t.getMessage();
                        _errorMessage.setValue(msg);
                        Log.e(TAG, msg, t);
                    }
                });
    }

    /**
     * Trigger a full refresh of the car list (starts from page 0).
     */
    public void refreshCars() {
        loadCars(true);
    }
}