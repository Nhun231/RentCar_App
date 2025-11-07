package com.anhbhn.rentcar.data.mapper;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.ui.car.CarRegistrationData;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CarDetailMapper {

    /**
     * Chuyển đổi CarResponse.Data (DTO từ API) sang CarRegistrationData (nội bộ).
     */
    public static CarRegistrationData mapToRegistrationData(CarResponse.Data responseData) {

        CarRegistrationData data = new CarRegistrationData();

        // 1. ÁNH XẠ THÔNG TIN CƠ BẢN (BASIC INFORMATION)
        data.licensePlate = responseData.licensePlate;
        data.brand = responseData.brand;
        data.model = responseData.model;
        data.color = responseData.color;
        data.status = responseData.status;

        // Xử lý các kiểu số (đã là kiểu nguyên thủy, không cần kiểm tra null)
        data.productionYear = responseData.productionYear;
        data.numberOfSeats = responseData.numberOfSeats;

        // Ánh xạ Booleans
        data.isAutomatic = responseData.isAutomatic;
        data.isGasoline = responseData.isGasoline;

        // Ánh xạ URIs Tài liệu (Files)
        data.registrationPaperUri = responseData.registrationPaperUrl;
        data.certificateOfInspectionUri = responseData.certificateOfInspectionUrl;
        data.insuranceUri = responseData.insuranceUrl;

        // 2. ÁNH XẠ CHI TIẾT (DETAILS)
        data.mileage = responseData.mileage;
        data.fuelConsumption = responseData.fuelConsumption;
        data.description = responseData.description;

        // Ánh xạ URIs Ảnh xe
        data.carImageFrontUri = responseData.carImageFrontUrl;
        data.carImageBackUri = responseData.carImageBackUrl;
        data.carImageLeftUri = responseData.carImageLeftUrl;
        data.carImageRightUri = responseData.carImageRightUrl;

        // Ánh xạ Chức năng phụ (Chuyển đổi chuỗi thành List nếu cần)
        if (responseData.additionalFunction != null) {
            // Giả sử API trả về chuỗi phân tách bằng dấu phẩy
            data.selectedFunctions = Arrays.asList(responseData.additionalFunction.split(",\\s*"));
        }

        // 3. PHÂN TÍCH VÀ ÁNH XẠ ĐỊA CHỈ (Cần thiết cho các Dropdown)
        mapFullAddress(responseData.address, data);

        // 4. ÁNH XẠ GIÁ (PRICING)
        data.basePrice = responseData.basePrice;
        data.requiredDeposit = responseData.deposit; // Giả định deposit API là requiredDeposit
        data.termsOfUseCombined = responseData.termOfUse;

        return data;
    }

    /**
     * Hàm helper: Tách chuỗi địa chỉ đầy đủ thành các phần tử riêng biệt
     * để đổ vào các trường Dropdown City, District, Ward.
     * Giả định định dạng API: [Số nhà/Đường], [Phường/Xã], [Quận/Huyện], [Thành phố]
     */
    private static void mapFullAddress(String fullAddress, CarRegistrationData data) {
        if (fullAddress == null || fullAddress.isEmpty()) return;

        // Tách chuỗi bằng dấu phẩy và loại bỏ khoảng trắng thừa
        String[] parts = fullAddress.split(",\\s*");

        // Nếu chuỗi API trả về theo thứ tự chuẩn: City, District, Ward, Street
        if (parts.length >= 4) {
            // Ánh xạ theo thứ tự từ đầu chuỗi (index 0)
            data.addressCityProvince = parts[0].trim();     // Phần tử 1 (Index 0): Tỉnh/Thành phố
            data.addressDistrict = parts[1].trim();         // Phần tử 2 (Index 1): Quận/Huyện
            data.addressWard = parts[2].trim();             // Phần tử 3 (Index 2): Phường/Xã
            data.addressHouseNumberStreet = parts[3].trim(); // Phần tử 4 (Index 3): Số nhà/Đường

        } else if (parts.length > 0) {
            // Trường hợp địa chỉ đơn giản, chỉ gán vào trường địa chỉ đầy đủ
            data.addressHouseNumberStreet = fullAddress;
        }
    }
}
