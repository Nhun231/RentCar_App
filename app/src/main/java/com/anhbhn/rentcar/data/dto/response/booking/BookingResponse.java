package com.anhbhn.rentcar.data.dto.response.booking;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BookingResponse implements Serializable {

    // --- Metadata API ---
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public static class Data implements Serializable {

        // --- Booking Details ---
        @SerializedName("bookingNumber")
        private String bookingNumber;
        @SerializedName("carId")
        private String carId;
        @SerializedName("status")
        private String status; // EBookingStatus -> String
        @SerializedName("pickUpLocation")
        private String pickUpLocation;
        @SerializedName("pickUpTime")
        private String pickUpTime; // LocalDateTime -> String
        @SerializedName("dropOffTime")
        private String dropOffTime; // LocalDateTime -> String
        @SerializedName("totalPrice")
        private long totalPrice;
        @SerializedName("basePrice")
        private long basePrice;
        @SerializedName("deposit")
        private long deposit;
        @SerializedName("paymentType")
        private String paymentType; // EPaymentType -> String

        // --- Driver Information ---
        @SerializedName("driverFullName")
        private String driverFullName;
        @SerializedName("driverPhoneNumber")
        private String driverPhoneNumber;
        @SerializedName("driverNationalId")
        private String driverNationalId;
        @SerializedName("driverDob")
        private String driverDob; // LocalDate -> String
        @SerializedName("driverEmail")
        private String driverEmail;
        @SerializedName("driverDrivingLicenseUrl")
        private String driverDrivingLicenseUrl;

        // --- Driver Address ---
        @SerializedName("driverCityProvince")
        private String driverCityProvince;
        @SerializedName("driverDistrict")
        private String driverDistrict;
        @SerializedName("driverWard")
        private String driverWard;
        @SerializedName("driverHouseNumberStreet")
        private String driverHouseNumberStreet;
        @SerializedName("isDriver")
        private boolean isDriver;

        // --- Getters & Setters cho Data ---

        public String getBookingNumber() { return bookingNumber; }
        public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

        public String getCarId() { return carId; }
        public void setCarId(String carId) { this.carId = carId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        // ... (Cần thêm getters/setters đầy đủ cho tất cả các trường trong Data) ...

        public String getPickUpTime() { return pickUpTime; }
        public void setPickUpTime(String pickUpTime) { this.pickUpTime = pickUpTime; }

        public long getTotalPrice() { return totalPrice; }
        public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }

        public String getDriverFullName() { return driverFullName; }
        public void setDriverFullName(String driverFullName) { this.driverFullName = driverFullName; }

        public String getDriverDrivingLicenseUrl() { return driverDrivingLicenseUrl; }
        public void setDriverDrivingLicenseUrl(String driverDrivingLicenseUrl) { this.driverDrivingLicenseUrl = driverDrivingLicenseUrl; }

        public String getDriverCityProvince() { return driverCityProvince; }
        public void setDriverCityProvince(String driverCityProvince) { this.driverCityProvince = driverCityProvince; }

        // Cần thêm các getters/setters còn thiếu...

        public String getPickUpLocation() {
            return pickUpLocation;
        }

        public void setPickUpLocation(String pickUpLocation) {
            this.pickUpLocation = pickUpLocation;
        }

        public String getDropOffTime() {
            return dropOffTime;
        }

        public void setDropOffTime(String dropOffTime) {
            this.dropOffTime = dropOffTime;
        }

        public long getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(long basePrice) {
            this.basePrice = basePrice;
        }

        public long getDeposit() {
            return deposit;
        }

        public void setDeposit(long deposit) {
            this.deposit = deposit;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(String paymentType) {
            this.paymentType = paymentType;
        }

        public String getDriverPhoneNumber() {
            return driverPhoneNumber;
        }

        public void setDriverPhoneNumber(String driverPhoneNumber) {
            this.driverPhoneNumber = driverPhoneNumber;
        }

        public String getDriverNationalId() {
            return driverNationalId;
        }

        public void setDriverNationalId(String driverNationalId) {
            this.driverNationalId = driverNationalId;
        }

        public String getDriverDob() {
            return driverDob;
        }

        public void setDriverDob(String driverDob) {
            this.driverDob = driverDob;
        }

        public String getDriverEmail() {
            return driverEmail;
        }

        public void setDriverEmail(String driverEmail) {
            this.driverEmail = driverEmail;
        }

        public String getDriverDistrict() {
            return driverDistrict;
        }

        public void setDriverDistrict(String driverDistrict) {
            this.driverDistrict = driverDistrict;
        }

        public String getDriverWard() {
            return driverWard;
        }

        public void setDriverWard(String driverWard) {
            this.driverWard = driverWard;
        }

        public String getDriverHouseNumberStreet() {
            return driverHouseNumberStreet;
        }

        public void setDriverHouseNumberStreet(String driverHouseNumberStreet) {
            this.driverHouseNumberStreet = driverHouseNumberStreet;
        }

        public boolean isDriver() {
            return isDriver;
        }

        public void setDriver(boolean driver) {
            isDriver = driver;
        }
    }

    // --- Getters & Setters cho BookingResponse ---

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}