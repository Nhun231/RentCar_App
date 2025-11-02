package com.anhbhn.rentcar.ui.car.addCar;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.anhbhn.rentcar.data.dto.helper.AddCarFormData;
import com.anhbhn.rentcar.data.dto.request.car.AddCarRequest;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.mapper.AddCarMapper;
import com.anhbhn.rentcar.data.repository.car.CarRepository;
import com.google.gson.Gson;

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

        // 1. Ánh xạ State (CarRegistrationData) sang AddCarRequest DTO
        //    (Cần đảm bảo hàm mapDataToRequest đã được tạo)
        AddCarRequest request = mapDataToRequest(finalData);

        // 2. Gọi API thông qua Repository
        CarRepository repository = new CarRepository(context);

        // Gọi hàm repository và xử lý callback
        repository.addCar(request).enqueue(new Callback<CarResponse>() {
            @Override
            public void onResponse(@NonNull Call<CarResponse> call, @NonNull Response<CarResponse> response) {
                if (response.isSuccessful()) {
                    // Thành công: Thông báo cho Fragment
                    submitSuccess.setValue(true);
                } else {
                    // Thất bại: Xử lý lỗi HTTP (Ví dụ: 400 Bad Request)
                    String errorMsg = "Lỗi hệ thống hoặc xác thực: Mã " + response.code();
                    submitError.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CarResponse> call, @NonNull Throwable t) {
                // Lỗi kết nối (Mạng, I/O)
                submitError.setValue("Lỗi kết nối mạng: " + t.getMessage());
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
                data.isAutomatic != null ? data.isAutomatic : false,
                data.isGasoline != null ? data.isGasoline : false,

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
}
