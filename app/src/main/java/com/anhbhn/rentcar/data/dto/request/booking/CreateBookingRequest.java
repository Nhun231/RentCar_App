package com.anhbhn.rentcar.data.dto.request.booking;

import java.io.Serializable;

public class CreateBookingRequest implements Serializable {
    public String carId;
    public String pickUpLocation;
    public String pickUpTime;
    public String dropOffTime;
    public String paymentType; // WALLET, CASH, BANK_TRANSFER
    
    // Driver information (only required if isDriver = true)
    public String driverFullName;
    public String driverPhoneNumber;
    public String driverNationalId;
    public String driverDob; // Format: "yyyy-MM-dd"
    public String driverEmail;
    public String driverCityProvince;
    public String driverDistrict;
    public String driverWard;
    public String driverHouseNumberStreet;
    
    // true if driver is different from renter, false if renter is the driver
    public boolean isDriver;
}

