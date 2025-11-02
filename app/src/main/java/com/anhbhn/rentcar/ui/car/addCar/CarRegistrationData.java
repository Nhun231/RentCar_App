package com.anhbhn.rentcar.ui.car.addCar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarRegistrationData {

    // --- STEP 1: Basic ---
    public String licensePlate;
    public String brand;
    public String model;
    public Integer productionYear;
    public Integer numberOfSeats;
    public Boolean isAutomatic;
    public Boolean isGasoline;
    public String color;
    public String registrationPaperUri;        // NEW: Lưu Path/URI file tạm
    public String certificateOfInspectionUri;  // NEW
    public String insuranceUri;                // NEW

    // --- STEP 2: Details ---
    public Float mileage; // (km)
    public Float fuelConsumption; // (L/100km)
    public String addressCityProvince;
    public String addressDistrict;
    public String addressWard;
    public String addressHouseNumberStreet;
    public String description;
    public List<String> selectedFunctions; // Ví dụ: "Bluetooth", "GPS", "Sun roof"
    public String carImageFrontUri; // Tương ứng với carImageFront
    public String carImageBackUri;  // Tương ứng với carImageBack
    public String carImageLeftUri;  // Tương ứng với carImageLeft
    public String carImageRightUri; // Tương ứng với carImageRight

    // --- STEP 3: Pricing ---
    public Long basePrice;
    public Long requiredDeposit;
    public String termsOfUseCombined; // Ví dụ: {"No smoking": true, "No pet": false}

    // Constructor mặc định
    public CarRegistrationData() {
        // Khởi tạo List và Map để tránh NullPointerException khi thêm dữ liệu
        this.selectedFunctions = new ArrayList<>();
    }
}
