package com.anhbhn.rentcar.ui.car.addCar;
import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.anhbhn.rentcar.data.dto.request.car.AddCarRequest;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.mapper.CarDetailMapper;
import com.anhbhn.rentcar.data.repository.car.CarRepository;
import com.anhbhn.rentcar.ui.car.CarRegistrationData;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.anhbhn.rentcar.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shared ViewModel quản lý trạng thái dữ liệu (CarRegistrationData)
 * cho toàn bộ luồng đăng ký xe 4 bước.
 */
public class AddCarViewModel extends ViewModel {
    private static final List<String> STATIC_COLORS = Collections.unmodifiableList(Arrays.asList(
            "White", "Black", "Gray", "Silver", "Red", "Blue",
            "Brown", "Green", "Beige", "Gold", "Yellow", "Purple"
    ));

    // 1. LiveData giữ toàn bộ dữ liệu đăng ký xe (THE STATE)
    private final MutableLiveData<CarRegistrationData> registrationData =
            new MutableLiveData<>(new CarRegistrationData());

    // 2. LiveData theo dõi bước hiện tại (Giúp cập nhật UI của Stepper)
    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(1);
    private final MutableLiveData<List<String>> availableBrands = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableColors = new MutableLiveData<>();
    private List<BrandModelData> allBrandModelData = Collections.emptyList();
    private List<AddressItem> allAddressData = Collections.emptyList();
    private final MutableLiveData<List<String>> availableCities = new MutableLiveData<>();
    public final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>();
    public final MutableLiveData<String> submitError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditMode = new MutableLiveData<>(false);

    private CarRepository repository;
    private void ensureRepositoryInitialized(Context context) {
        if (this.repository == null) {
            this.repository = new CarRepository(context);
        }
    }
    public AddCarViewModel() {
        // Khởi tạo LiveData COLORS tĩnh ngay trong constructor
        availableColors.setValue(STATIC_COLORS);
    }
    // --- GETTERS ---

    public LiveData<List<String>> getAvailableBrands() { return availableBrands; }
    public LiveData<List<String>> getAvailableColors() { return availableColors; }
    public MutableLiveData<CarRegistrationData> getRegistrationData() { return registrationData; }
    public LiveData<List<String>> getAvailableCities() { return availableCities; }

    public MutableLiveData<Integer> getCurrentStep() {
        return currentStep;
    }
    public LiveData<Boolean> getIsEditMode() { return isEditMode; }

    public void setEditMode(boolean isEdit) {
        isEditMode.setValue(isEdit);
    }

    public void setStep(int step) {
        currentStep.setValue(step);
    }
    //-------------
    /**
     * Tải dữ liệu Brand/Model từ file database.json (res/raw/) chỉ một lần.
     */
    public void initializeCarOptions(Context context) {
        if (!allBrandModelData.isEmpty() && !allAddressData.isEmpty()) {
            return; // Dữ liệu đã tải
        }

        // Chạy trên background thread
        new Thread(() -> {
            try {
                // 1. Đọc file JSON từ res/raw/database.json
                InputStream inputStream = context.getResources().openRawResource(R.raw.database);
                Reader reader = new InputStreamReader(inputStream);

                // 2. Phân tích cú pháp JSON bằng Gson
                Gson gson = new Gson();
                CarOptions options = gson.fromJson(reader, CarOptions.class);

                // 3. Lưu dữ liệu thô và cập nhật LiveData
                if (options != null) {
                    if(options.getBrandModelDataList() != null){
                        allBrandModelData = options.getBrandModelDataList();

                        // Lọc Brand duy nhất và cập nhật LiveData (cho Dropdown)
                        List<String> uniqueBrands = allBrandModelData.stream()
                                .map(BrandModelData::getBrand)
                                .distinct()
                                .collect(Collectors.toList());

                        availableBrands.postValue(uniqueBrands);
                    }
                    if(options.getAddressList() != null){
                        allAddressData = options.getAddressList();

                        // Lọc Cities duy nhất và cập nhật LiveData
                        List<String> uniqueCities = allAddressData.stream()
                                .map(AddressItem::getCityProvince)
                                .distinct()
                                .collect(Collectors.toList());

                        availableCities.postValue(uniqueCities);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Xử lý lỗi tải dữ liệu
            }
        }).start();
    }

    /**
     * Lấy danh sách Model theo Brand đã chọn (Truy vấn từ dữ liệu đã tải).
     */
    public List<String> getFilteredModels(String selectedBrand) {
        return allBrandModelData.stream()
                .filter(item -> item.getBrand().equals(selectedBrand))
                .map(BrandModelData::getModel)
                .distinct()
                .collect(Collectors.toList());
    }
    public List<String> getFilteredDistricts(String selectedCity) {
        return allAddressData.stream()
                .filter(item -> item.getCityProvince().equals(selectedCity))
                .map(AddressItem::getDistrict)
                .distinct()
                .collect(Collectors.toList());
    }
    public List<String> getFilteredWards(String selectedCity, String selectedDistrict) {
        return allAddressData.stream()
                .filter(item -> item.getCityProvince().equals(selectedCity))
                .filter(item -> item.getDistrict().equals(selectedDistrict))
                .map(AddressItem::getWard)
                .distinct()
                .collect(Collectors.toList());
    }

    // --- HÀM CẬP NHẬT DỮ LIỆU TỪ FRAGMENT ---

    /**
     * Cập nhật dữ liệu từ BasicFragment (Step 1)
     */
    public void updateBasicData(
            String licensePlate, String brand, String model, String color,
            int year, int seats, boolean isAuto, boolean isGas,
            String registrationPaperUri, String certificateOfInspectionUri, String insuranceUri
    ) {
        CarRegistrationData data = registrationData.getValue();
        if (data != null) {
            data.licensePlate = licensePlate;
            data.brand = brand;
            data.model = model;
            data.color = color;
            data.productionYear = year;
            data.numberOfSeats = seats;
            data.isAutomatic = isAuto;
            data.isGasoline = isGas;

            // Files tài liệu
            data.registrationPaperUri = registrationPaperUri;
            data.certificateOfInspectionUri = certificateOfInspectionUri;
            data.insuranceUri = insuranceUri;

            registrationData.setValue(data);
        }
    }

    /**
     * Cập nhật dữ liệu từ DetailsFragment (Step 2).
     * Địa chỉ và Chức năng phụ sẽ được kết hợp chuỗi ở Fragment trước khi lưu vào data.
     */
    public void updateDetailsData(
            Float mileage, Float consumption, String desc,
            String city, String district, String ward, String street,
            List<String> functions,
            // 4 URI ảnh xe riêng biệt
            String frontUri, String backUri, String leftUri, String rightUri
    ) {
        CarRegistrationData data = registrationData.getValue();
        if (data != null) {
            data.mileage = mileage;
            data.fuelConsumption = consumption;
            data.description = desc;

            // Địa chỉ chi tiết
            data.addressCityProvince = city;
            data.addressDistrict = district;
            data.addressWard = ward;
            data.addressHouseNumberStreet = street;
            data.selectedFunctions = functions;

            // Ảnh xe
            data.carImageFrontUri = frontUri;
            data.carImageBackUri = backUri;
            data.carImageLeftUri = leftUri;
            data.carImageRightUri = rightUri;

            registrationData.setValue(data);
        }
    }

    /**
     * Cập nhật dữ liệu từ PricingFragment (Step 3).
     * @param combinedTerms Chuỗi các điều khoản được nối bằng dấu ","
     */
    public void updatePricingData(Long basePrice, Long requiredDeposit, String combinedTerms) {
        CarRegistrationData data = registrationData.getValue();
        if (data != null) {
            data.basePrice = basePrice;
            data.requiredDeposit = requiredDeposit;
            data.termsOfUseCombined = combinedTerms;
            registrationData.setValue(data);
        }
    }

    // --- HÀM XỬ LÝ CUỐI CÙNG ---

    /**
     * Lấy dữ liệu cuối cùng và bắt đầu quá trình gửi lên API (Mapping + Network Call).
     * Sẽ được gọi từ FinishFragment (Step 4).
     */
    public void submitCarData(Context context) {
        CarRegistrationData finalData = registrationData.getValue();
        if (finalData == null) {
            submitError.setValue("Dữ liệu xe không hoàn chỉnh.");
            return;
        }

        AddCarRequest request = mapDataToRequest(finalData);
        ensureRepositoryInitialized(context);

        // Gọi hàm repository và xử lý callback
        // Dùng CarResponse làm kiểu phản hồi vì nó chứa 'code' và 'message'
        repository.addCar(request).enqueue(new Callback<CarResponse>() {
            @Override
            public void onResponse(@NonNull Call<CarResponse> call, @NonNull Response<CarResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    // 1. HTTP 2xx VÀ Body không rỗng
                    CarResponse carResponse = response.body();

                    // 2. Kiểm tra mã nghiệp vụ bên trong Body
                    if (carResponse.code == 1000) {
                        // ✅ THÀNH CÔNG NGHIỆP VỤ (Code: 1000)
                        submitSuccess.setValue(true);
                    } else {
                        // ❌ THẤT BẠI NGHIỆP VỤ (HTTP 200, nhưng Code != 1000)
                        // Lấy thông báo lỗi chi tiết từ message
                        String errorMsg = "Lỗi nghiệp vụ: " + carResponse.message + " (Mã: " + carResponse.code + ")";
                        submitError.setValue(errorMsg);
                    }

                } else {
                    // ❌ THẤT BẠI HTTP (4xx hoặc 5xx) hoặc LỖI BODY RỖNG

                    String errorMsg = "Lỗi kết nối máy chủ (Mã HTTP: " + response.code() + ")";

                    // Thử đọc chi tiết lỗi từ Error Body (áp dụng cho lỗi 4xx/5xx)
                    if (response.errorBody() != null) {
                        try {
                            String detailedError = response.errorBody().string();
                            // Đối với lỗi 4xx/5xx, detailedError thường là JSON chứa code/message
                            errorMsg += ". Chi tiết: " + detailedError;
                        } catch (IOException e) {
                            e.printStackTrace();
                            errorMsg += ". Lỗi đọc phản hồi lỗi.";
                        }
                    }
                    submitError.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CarResponse> call, @NonNull Throwable t) {
                // Lỗi kết nối (Network, Timeout, Deserialization Error)
                String failureMessage = "Lỗi kết nối mạng: " + t.getMessage();

                if (t instanceof IOException) {
                    failureMessage = "Lỗi I/O (Timeout hoặc Mạng): " + t.getMessage();
                } else if (t instanceof com.google.gson.JsonSyntaxException) {
                    // Đây là nơi lỗi Deserialization thường xảy ra khi body không hợp lệ
                    failureMessage = "Lỗi phân tích cú pháp JSON: Server trả về dữ liệu không hợp lệ. " + t.getMessage();
                }

                submitError.setValue(failureMessage);
            }
        });
    }
    private AddCarRequest mapDataToRequest(CarRegistrationData data) {

        // Kết hợp các chuỗi phức tạp
        String finalAddress = combineAddress(data);
        String finalFunctions = combineFunctions(data);

        // CHÚ Ý: Sử dụng Builder Pattern nếu AddCarRequest hỗ trợ,
        // nếu không, phải dùng Constructor hoặc Setter (Tôi dùng Builder giả định)

        return new AddCarRequest(
                // 1. Basic Info
                data.licensePlate,
                data.brand,
                data.model,
                data.color,
                data.numberOfSeats != null ? data.numberOfSeats.intValue() : 0,
                data.productionYear != null ? data.productionYear.intValue() : 0,

                // 2. Mileage & Fuel (Chuyển đổi từ String sang Float/long)
                data.mileage != null ? Float.parseFloat(String.valueOf(data.mileage)) : 0f,
                data.fuelConsumption != null ? Float.parseFloat(String.valueOf(data.fuelConsumption)) : 0f,

                // 3. Pricing
                data.basePrice != null ? data.basePrice : 0L,
                data.requiredDeposit != null ? data.requiredDeposit : 0L,

                // 4. Strings
                finalAddress,
                data.description,
                finalFunctions,
                data.termsOfUseCombined,

                // 5. Booleans
                data.isAutomatic,
                data.isGasoline,

                // 6. FILES (URIs - phải khớp với thứ tự Constructor)
                Uri.parse(data.registrationPaperUri),
                Uri.parse(data.certificateOfInspectionUri),
                Uri.parse(data.insuranceUri),
                Uri.parse(data.carImageFrontUri),
                Uri.parse(data.carImageBackUri),
                Uri.parse(data.carImageLeftUri),
                Uri.parse(data.carImageRightUri)
        );
    }

    /**
     * Kết hợp các thành phần địa chỉ (City, District, Ward, Street) thành một chuỗi duy nhất.
     */
    private String combineAddress(CarRegistrationData data) {
        List<String> parts = Arrays.asList(
                data.addressCityProvince,
                data.addressDistrict,
                data.addressWard,
                data.addressHouseNumberStreet
        );
        // Lọc bỏ null/empty và nối bằng ", "
        return parts.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.joining(", "));
    }

    /**
     * Chuyển đổi List<String> functions thành chuỗi phân tách bằng dấu phẩy.
     */
    private String combineFunctions(CarRegistrationData data) {
        if (data.selectedFunctions == null || data.selectedFunctions.isEmpty()) {
            return "";
        }
        // Nối các chức năng phụ bằng ", "
        return String.join(", ", data.selectedFunctions);
    }

    public void fetchCarDetailsForEdit(String carId, Context context) {
        ensureRepositoryInitialized(context);

        if (repository == null) {
            Log.e(TAG, "CarRepository is not initialized! Cannot fetch details.");
            return;
        }

        repository.getCarDetailsForOwner(carId).enqueue(new Callback<CarResponse>() {
            @Override
            public void onResponse(@NonNull Call<CarResponse> call, @NonNull Response<CarResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    CarResponse apiResponse = response.body();

                    if (apiResponse.code == 1000) {
                        // ... (Logic thành công) ...
                        CarRegistrationData mappedData = CarDetailMapper.mapToRegistrationData(apiResponse.data);
                        registrationData.postValue(mappedData);
                        Log.i(TAG, "Car details loaded successfully.");
                    } else {
                        // ❌ ĐÃ SỬA: Lỗi nghiệp vụ chỉ ghi Log
                        String errorMsg = "Lỗi nghiệp vụ: " + apiResponse.message;
                        Log.e(TAG, errorMsg);
                    }

                } else {
                    // ❌ ĐÃ SỬA: Lỗi HTTP chỉ ghi Log
                    String errorMsg = "Lỗi HTTP: " + response.code();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CarResponse> call, @NonNull Throwable t) {
                // ❌ ĐÃ SỬA: Lỗi kết nối mạng chỉ ghi Log
                String errorMsg = "Lỗi kết nối mạng: " + t.getMessage();
                Log.e(TAG, errorMsg);
            }
        });
    }
}
