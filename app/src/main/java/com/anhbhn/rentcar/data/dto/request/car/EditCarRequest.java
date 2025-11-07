package com.anhbhn.rentcar.data.dto.request.car;

import android.net.Uri;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class EditCarRequest implements Serializable {

    // --- DETAILS ---
    @SerializedName("mileage")
    private float mileage;

    @SerializedName("fuelConsumption")
    private float fuelConsumption;

    @SerializedName("address")
    private String address;

    @SerializedName("description")
    private String description;

    @SerializedName("additionalFunction")
    private String additionalFunction;

    @SerializedName("termOfUse")
    private String termOfUse;

    // --- PRICING ---
    @SerializedName("basePrice")
    private long basePrice;

    @SerializedName("deposit")
    private long deposit;

    // --- STATUS ---
    @SerializedName("status")
    private String status;

    // --- IMAGES (Sử dụng Uri trong Android để tham chiếu file, nhưng tên serialized phải khớp với Backend) ---

    // Backend DTO dùng tên 'carImageFront', ta phải dùng tên này trong @SerializedName
    @SerializedName("carImageFront")
    private Uri carImageFront;

    @SerializedName("carImageBack")
    private Uri carImageBack;

    @SerializedName("carImageLeft")
    private Uri carImageLeft;

    @SerializedName("carImageRight")
    private Uri carImageRight;


    // --- Constructors (Cần thiết) ---

    public EditCarRequest() {
    }

    public EditCarRequest(float mileage, float fuelConsumption, String address, String description, String additionalFunction, String termOfUse, long basePrice, long deposit, String status, Uri carImageFront, Uri carImageBack, Uri carImageLeft, Uri carImageRight) {
        this.mileage = mileage;
        this.fuelConsumption = fuelConsumption;
        this.address = address;
        this.description = description;
        this.additionalFunction = additionalFunction;
        this.termOfUse = termOfUse;
        this.basePrice = basePrice;
        this.deposit = deposit;
        this.status = status;
        this.carImageFront = carImageFront;
        this.carImageBack = carImageBack;
        this.carImageLeft = carImageLeft;
        this.carImageRight = carImageRight;
    }

    // --- Getters & Setters (Bắt buộc cho POJO/DTO) ---

    public float getMileage() { return mileage; }
    public void setMileage(float mileage) { this.mileage = mileage; }

    public float getFuelConsumption() { return fuelConsumption; }
    public void setFuelConsumption(float fuelConsumption) { this.fuelConsumption = fuelConsumption; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdditionalFunction() { return additionalFunction; }
    public void setAdditionalFunction(String additionalFunction) { this.additionalFunction = additionalFunction; }

    public String getTermOfUse() { return termOfUse; }
    public void setTermOfUse(String termOfUse) { this.termOfUse = termOfUse; }

    public long getBasePrice() { return basePrice; }
    public void setBasePrice(long basePrice) { this.basePrice = basePrice; }

    public long getDeposit() { return deposit; }
    public void setDeposit(long deposit) { this.deposit = deposit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Getters/Setters cho Uri ảnh
    public Uri getCarImageFront() { return carImageFront; }
    public void setCarImageFront(Uri carImageFront) { this.carImageFront = carImageFront; }

    public Uri getCarImageBack() { return carImageBack; }
    public void setCarImageBack(Uri carImageBack) { this.carImageBack = carImageBack; }

    public Uri getCarImageLeft() { return carImageLeft; }
    public void setCarImageLeft(Uri carImageLeft) { this.carImageLeft = carImageLeft; }

    public Uri getCarImageRight() { return carImageRight; }
    public void setCarImageRight(Uri carImageRight) { this.carImageRight = carImageRight; }
}