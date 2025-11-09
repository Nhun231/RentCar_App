package com.anhbhn.rentcar.data.dto.request.auth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @Expose
    @SerializedName("fullName")
    private String fullName;
    
    @Expose
    @SerializedName("email")
    private String email;
    
    @Expose
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @Expose
    @SerializedName("password")
    private String password;
    
    @Expose
    @SerializedName("isCustomer")
    private String isCustomer; // "true" for CUSTOMER, "false" for CAR_OWNER

    public RegisterRequest() {
    }

    public RegisterRequest(String fullName, String email, String phoneNumber, String password, String isCustomer) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.isCustomer = isCustomer;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIsCustomer() {
        return isCustomer;
    }

    public void setIsCustomer(String isCustomer) {
        this.isCustomer = isCustomer;
    }
}
