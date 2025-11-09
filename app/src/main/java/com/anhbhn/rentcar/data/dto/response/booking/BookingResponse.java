package com.anhbhn.rentcar.data.dto.response.booking;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BookingResponse implements Serializable {
    @SerializedName("bookingNumber")
    public String bookingNumber;
    
    @SerializedName("carId")
    public String carId;
    
    @SerializedName("status")
    public String status;
    
    @SerializedName("pickUpLocation")
    public String pickUpLocation;
    
    @SerializedName("pickUpTime")
    public String pickUpTime;
    
    @SerializedName("dropOffTime")
    public String dropOffTime;
    
    @SerializedName("totalPrice")
    public long totalPrice;
    
    @SerializedName("basePrice")
    public long basePrice;
    
    @SerializedName("deposit")
    public long deposit;
    
    @SerializedName("paymentType")
    public String paymentType;
    
    // Driver Information
    @SerializedName("driverFullName")
    public String driverFullName;
    
    @SerializedName("driverPhoneNumber")
    public String driverPhoneNumber;
    
    @SerializedName("driverNationalId")
    public String driverNationalId;
    
    @SerializedName("driverDob")
    public String driverDob;
    
    @SerializedName("driverEmail")
    public String driverEmail;
    
    @SerializedName("driverDrivingLicenseUrl")
    public String driverDrivingLicenseUrl;
    
    @SerializedName("driverCityProvince")
    public String driverCityProvince;
    
    @SerializedName("driverDistrict")
    public String driverDistrict;
    
    @SerializedName("driverWard")
    public String driverWard;
    
    @SerializedName("driverHouseNumberStreet")
    public String driverHouseNumberStreet;
    
    @SerializedName("isDriver")
    public boolean isDriver;
}

