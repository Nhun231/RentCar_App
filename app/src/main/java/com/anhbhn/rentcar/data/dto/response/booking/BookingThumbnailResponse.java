package com.anhbhn.rentcar.data.dto.response.booking;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BookingThumbnailResponse implements Serializable {
    @SerializedName("bookingNumber")
    public String bookingNumber;
    
    @SerializedName("status")
    public String status;
    
    @SerializedName("pickUpTime")
    public String pickUpTime;
    
    @SerializedName("dropOffTime")
    public String dropOffTime;
    
    @SerializedName("numberOfDay")
    public int numberOfDay;
    
    @SerializedName("basePrice")
    public long basePrice;
    
    @SerializedName("totalPrice")
    public long totalPrice;
    
    @SerializedName("deposit")
    public long deposit;
    
    @SerializedName("brand")
    public String brand;
    
    @SerializedName("model")
    public String model;
    
    @SerializedName("productionYear")
    public int productionYear;
    
    @SerializedName("carImageFrontUrl")
    public String carImageFrontUrl;
    
    @SerializedName("carImageBackUrl")
    public String carImageBackUrl;
    
    @SerializedName("carImageLeftUrl")
    public String carImageLeftUrl;
    
    @SerializedName("carImageRightUrl")
    public String carImageRightUrl;
    
    @SerializedName("customerPhoneNumber")
    public String customerPhoneNumber;
    
    @SerializedName("customerEmail")
    public String customerEmail;
    
    @SerializedName("paymentType")
    public String paymentType;
}

