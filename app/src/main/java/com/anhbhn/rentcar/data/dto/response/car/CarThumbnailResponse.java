package com.anhbhn.rentcar.data.dto.response.car;


import com.google.gson.annotations.SerializedName;

import org.threeten.bp.LocalDateTime;

import java.io.Serializable;

public class CarThumbnailResponse implements Serializable {
        private String id;
        private String brand;
        private String model;
        private int productionYear;
        private String status;
        private float mileage;
        private long basePrice;
        private String address;

        // Images
        private String carImageFront;
        private String carImageRight;
        private String carImageLeft;
        private String carImageBack;

        // Stats
        private long noOfRides;
        private double averageRatingByCar;
        private LocalDateTime updatedAt;
    @SerializedName("numberOfSeats")
    public Integer numberOfSeats;

    @SerializedName("automatic")
    public Boolean isAutomatic;

    @SerializedName("gasoline")
    public Boolean isGasoline;

    public CarThumbnailResponse() {
    }

    public CarThumbnailResponse(String id, String brand, String model, int productionYear, String status, float mileage, long basePrice, String address, String carImageFront, String carImageRight, String carImageLeft, String carImageBack, long noOfRides, double averageRatingByCar, LocalDateTime updatedAt) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.productionYear = productionYear;
        this.status = status;
        this.mileage = mileage;
        this.basePrice = basePrice;
        this.address = address;
        this.carImageFront = carImageFront;
        this.carImageRight = carImageRight;
        this.carImageLeft = carImageLeft;
        this.carImageBack = carImageBack;
        this.noOfRides = noOfRides;
        this.averageRatingByCar = averageRatingByCar;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getMileage() {
        return mileage;
    }

    public void setMileage(float mileage) {
        this.mileage = mileage;
    }

    public long getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(long basePrice) {
        this.basePrice = basePrice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCarImageFront() {
        return carImageFront;
    }

    public void setCarImageFront(String carImageFront) {
        this.carImageFront = carImageFront;
    }

    public String getCarImageRight() {
        return carImageRight;
    }

    public void setCarImageRight(String carImageRight) {
        this.carImageRight = carImageRight;
    }

    public String getCarImageLeft() {
        return carImageLeft;
    }

    public void setCarImageLeft(String carImageLeft) {
        this.carImageLeft = carImageLeft;
    }

    public String getCarImageBack() {
        return carImageBack;
    }

    public void setCarImageBack(String carImageBack) {
        this.carImageBack = carImageBack;
    }

    public long getNoOfRides() {
        return noOfRides;
    }

    public void setNoOfRides(long noOfRides) {
        this.noOfRides = noOfRides;
    }

    public double getAverageRatingByCar() {
        return averageRatingByCar;
    }

    public void setAverageRatingByCar(double averageRatingByCar) {
        this.averageRatingByCar = averageRatingByCar;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}