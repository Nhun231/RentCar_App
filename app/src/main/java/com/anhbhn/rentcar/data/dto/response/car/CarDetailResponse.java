package com.anhbhn.rentcar.data.dto.response.car;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CarDetailResponse implements Serializable {
    @SerializedName("licensePlate")
    public String licensePlate;

    @SerializedName("brand")
    public String brand;

    @SerializedName("model")
    public String model;

    @SerializedName("status")
    public String status;

    @SerializedName("color")
    public String color;

    @SerializedName("numberOfSeats")
    public int numberOfSeats;

    @SerializedName("productionYear")
    public int productionYear;

    @SerializedName("mileage")
    public float mileage;

    @SerializedName("fuelConsumption")
    public float fuelConsumption;

    @SerializedName("basePrice")
    public long basePrice;

    @SerializedName("deposit")
    public long deposit;

    @SerializedName("address")
    public String address;

    @SerializedName("description")
    public String description;

    @SerializedName("additionalFunction")
    public String additionalFunction;

    @SerializedName("termOfUse")
    public String termOfUse;

    @SerializedName("isAutomatic")
    public Boolean isAutomatic;

    @SerializedName("isGasoline")
    public Boolean isGasoline;

    @SerializedName("registrationPaperUrl")
    public String registrationPaperUrl;

    @SerializedName("registrationPaperIsVerified")
    public Boolean registrationPaperIsVerified;

    @SerializedName("certificateOfInspectionUrl")
    public String certificateOfInspectionUrl;

    @SerializedName("certificateOfInspectionIsVerified")
    public Boolean certificateOfInspectionIsVerified;

    @SerializedName("insuranceUrl")
    public String insuranceUrl;

    @SerializedName("insuranceIsVerified")
    public Boolean insuranceIsVerified;

    @SerializedName("carImageFront")
    public String carImageFront;

    @SerializedName("carImageBack")
    public String carImageBack;

    @SerializedName("carImageLeft")
    public String carImageLeft;

    @SerializedName("carImageRight")
    public String carImageRight;

    @SerializedName("noOfRides")
    public long noOfRides;

    @SerializedName("booked")
    public Boolean isBooked;

    @SerializedName("available")
    public Boolean isAvailable;

    @SerializedName("averageRatingByCar")
    public double averageRatingByCar;
}

