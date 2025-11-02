package com.anhbhn.rentcar.data.dto.request.car;

import android.net.Uri;

import java.io.Serializable;

import okhttp3.MultipartBody;

public class AddCarRequest implements Serializable {
    private String licensePlate;
    private String brand;
    private String model;
    private String color;
    private int numberOfSeats;
    private int productionYear;
    private float mileage;
    private float fuelConsumption;
    private long basePrice;
    private long deposit;
    private String address;
    private String description;
    private String additionalFunction;
    private String termOfUse;
    private boolean isAutomatic;
    private boolean isGasoline;

    private Uri registrationPaper;
    private Uri certificateOfInspection;
    private Uri insurance;
    private Uri carImageFront;
    private Uri carImageBack;
    private Uri carImageLeft;
    private Uri carImageRight;

    public AddCarRequest() {
    }

    public AddCarRequest(String licensePlate, String brand, String model, String color, int numberOfSeats, int productionYear, float mileage, float fuelConsumption, long basePrice, long deposit, String address, String description, String additionalFunction, String termOfUse, boolean isAutomatic, boolean isGasoline, Uri registrationPaper, Uri certificateOfInspection, Uri insurance, Uri carImageFront, Uri carImageBack, Uri carImageLeft, Uri carImageRight) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.numberOfSeats = numberOfSeats;
        this.productionYear = productionYear;
        this.mileage = mileage;
        this.fuelConsumption = fuelConsumption;
        this.basePrice = basePrice;
        this.deposit = deposit;
        this.address = address;
        this.description = description;
        this.additionalFunction = additionalFunction;
        this.termOfUse = termOfUse;
        this.isAutomatic = isAutomatic;
        this.isGasoline = isGasoline;
        this.registrationPaper = registrationPaper;
        this.certificateOfInspection = certificateOfInspection;
        this.insurance = insurance;
        this.carImageFront = carImageFront;
        this.carImageBack = carImageBack;
        this.carImageLeft = carImageLeft;
        this.carImageRight = carImageRight;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public int getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(int productionYear) {
        this.productionYear = productionYear;
    }

    public float getMileage() {
        return mileage;
    }

    public void setMileage(float mileage) {
        this.mileage = mileage;
    }

    public float getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(float fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdditionalFunction() {
        return additionalFunction;
    }

    public void setAdditionalFunction(String additionalFunction) {
        this.additionalFunction = additionalFunction;
    }

    public String getTermOfUse() {
        return termOfUse;
    }

    public void setTermOfUse(String termOfUse) {
        this.termOfUse = termOfUse;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public void setAutomatic(boolean automatic) {
        isAutomatic = automatic;
    }

    public boolean isGasoline() {
        return isGasoline;
    }

    public void setGasoline(boolean gasoline) {
        isGasoline = gasoline;
    }

    public Uri getRegistrationPaper() {
        return registrationPaper;
    }

    public void setRegistrationPaper(Uri registrationPaper) {
        this.registrationPaper = registrationPaper;
    }

    public Uri getCertificateOfInspection() {
        return certificateOfInspection;
    }

    public void setCertificateOfInspection(Uri certificateOfInspection) {
        this.certificateOfInspection = certificateOfInspection;
    }

    public Uri getInsurance() {
        return insurance;
    }

    public void setInsurance(Uri insurance) {
        this.insurance = insurance;
    }

    public Uri getCarImageFront() {
        return carImageFront;
    }

    public void setCarImageFront(Uri carImageFront) {
        this.carImageFront = carImageFront;
    }

    public Uri getCarImageBack() {
        return carImageBack;
    }

    public void setCarImageBack(Uri carImageBack) {
        this.carImageBack = carImageBack;
    }

    public Uri getCarImageLeft() {
        return carImageLeft;
    }

    public void setCarImageLeft(Uri carImageLeft) {
        this.carImageLeft = carImageLeft;
    }

    public Uri getCarImageRight() {
        return carImageRight;
    }

    public void setCarImageRight(Uri carImageRight) {
        this.carImageRight = carImageRight;
    }
}
