package com.anhbhn.rentcar.data.dto.response.car;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CarThumbnailResponse implements Serializable {
    @SerializedName("id")
    public String id;

    @SerializedName("brand")
    public String brand;

    @SerializedName("model")
    public String model;

    @SerializedName("productionYear")
    public int productionYear;

    @SerializedName("status")
    public String status;

    @SerializedName("mileage")
    public float mileage;

    @SerializedName("basePrice")
    public long basePrice;

    @SerializedName("address")
    public String address;

    @SerializedName("carImageFront")
    public String carImageFront;

    @SerializedName("carImageRight")
    public String carImageRight;

    @SerializedName("carImageLeft")
    public String carImageLeft;

    @SerializedName("carImageBack")
    public String carImageBack;

    @SerializedName("noOfRides")
    public long noOfRides;

    @SerializedName("averageRatingByCar")
    public double averageRatingByCar;

    @SerializedName("updatedAt")
    public String updatedAt;

    @SerializedName("numberOfSeats")
    public Integer numberOfSeats;

    @SerializedName("automatic")
    public Boolean isAutomatic;

    @SerializedName("gasoline")
    public Boolean isGasoline;
}

