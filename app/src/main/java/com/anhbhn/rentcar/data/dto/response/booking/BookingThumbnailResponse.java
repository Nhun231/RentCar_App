package com.anhbhn.rentcar.data.dto.response.booking;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BookingThumbnailResponse implements Serializable {

    // --- Booking Details ---
    @SerializedName("bookingNumber")
    private String bookingNumber;
    @SerializedName("status")
    private String status; // EBookingStatus (WAITTING_CONFIRMED, STOPPED, etc.)

    @SerializedName("pickUpTime")
    private String pickUpTime; // LocalDateTime -> String

    @SerializedName("dropOffTime")
    private String dropOffTime; // LocalDateTime -> String

    @SerializedName("numberOfDay")
    private int numberOfDay;

    @SerializedName("basePrice")
    private long basePrice;

    @SerializedName("totalPrice")
    private long totalPrice;

    @SerializedName("deposit")
    private long deposit;

    // --- Car Information ---
    @SerializedName("brand")
    private String brand;
    @SerializedName("model")
    private String model;
    @SerializedName("productionYear")
    private int productionYear;

    // --- Image URL ---
    @SerializedName("carImageFrontUrl")
    private String carImageFrontUrl;

    @SerializedName("carImageBackUrl")
    private String carImageBackUrl;

    @SerializedName("carImageLeftUrl")
    private String carImageLeftUrl;

    @SerializedName("carImageRightUrl")
    private String carImageRightUrl;

    // --- Customer/Contact Info ---
    @SerializedName("customerPhoneNumber")
    private String customerPhoneNumber;

    @SerializedName("customerEmail")
    private String customerEmail;

    @SerializedName("paymentType")
    private String paymentType; // EPaymentType -> String

    public BookingThumbnailResponse() {
    }

    public BookingThumbnailResponse(String bookingNumber, String status, String pickUpTime, String dropOffTime, int numberOfDay, long basePrice, long totalPrice, long deposit, String brand, String model, int productionYear, String carImageFrontUrl, String carImageBackUrl, String carImageLeftUrl, String carImageRightUrl, String customerPhoneNumber, String customerEmail, String paymentType) {
        this.bookingNumber = bookingNumber;
        this.status = status;
        this.pickUpTime = pickUpTime;
        this.dropOffTime = dropOffTime;
        this.numberOfDay = numberOfDay;
        this.basePrice = basePrice;
        this.totalPrice = totalPrice;
        this.deposit = deposit;
        this.brand = brand;
        this.model = model;
        this.productionYear = productionYear;
        this.carImageFrontUrl = carImageFrontUrl;
        this.carImageBackUrl = carImageBackUrl;
        this.carImageLeftUrl = carImageLeftUrl;
        this.carImageRightUrl = carImageRightUrl;
        this.customerPhoneNumber = customerPhoneNumber;
        this.customerEmail = customerEmail;
        this.paymentType = paymentType;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(String pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public String getDropOffTime() {
        return dropOffTime;
    }

    public void setDropOffTime(String dropOffTime) {
        this.dropOffTime = dropOffTime;
    }

    public int getNumberOfDay() {
        return numberOfDay;
    }

    public void setNumberOfDay(int numberOfDay) {
        this.numberOfDay = numberOfDay;
    }

    public long getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(long basePrice) {
        this.basePrice = basePrice;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getDeposit() {
        return deposit;
    }

    public void setDeposit(long deposit) {
        this.deposit = deposit;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(int productionYear) {
        this.productionYear = productionYear;
    }

    public String getCarImageFrontUrl() {
        return carImageFrontUrl;
    }

    public void setCarImageFrontUrl(String carImageFrontUrl) {
        this.carImageFrontUrl = carImageFrontUrl;
    }

    public String getCarImageBackUrl() {
        return carImageBackUrl;
    }

    public void setCarImageBackUrl(String carImageBackUrl) {
        this.carImageBackUrl = carImageBackUrl;
    }

    public String getCarImageLeftUrl() {
        return carImageLeftUrl;
    }

    public void setCarImageLeftUrl(String carImageLeftUrl) {
        this.carImageLeftUrl = carImageLeftUrl;
    }

    public String getCarImageRightUrl() {
        return carImageRightUrl;
    }

    public void setCarImageRightUrl(String carImageRightUrl) {
        this.carImageRightUrl = carImageRightUrl;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}