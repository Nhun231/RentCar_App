package com.anhbhn.rentcar.data.dto.response.user;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class EditProfileResponse implements Serializable {
    @SerializedName("fullName")
    public String fullName;
    
    @SerializedName("email")
    public String email;
    
    @SerializedName("phoneNumber")
    public String phoneNumber;
    
    @SerializedName("nationalId")
    public String nationalId;
    
    @SerializedName("dob")
    public String dob; // Format: "yyyy-MM-dd"
    
    @SerializedName("drivingLicenseUrl")
    public String drivingLicenseUrl;
    
    @SerializedName("cityProvince")
    public String cityProvince;
    
    @SerializedName("district")
    public String district;
    
    @SerializedName("ward")
    public String ward;
    
    @SerializedName("houseNumberStreet")
    public String houseNumberStreet;
}

