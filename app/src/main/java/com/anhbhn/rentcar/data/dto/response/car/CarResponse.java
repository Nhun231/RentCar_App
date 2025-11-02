package com.anhbhn.rentcar.data.dto.response.car;

public class CarResponse {
    public int code;
    public String message;
    public Data data;

    public static class Data {
        public String id;
        public String licensePlate;
        public String brand;
        public String model;
        public String status;
        public String color;
        public int numberOfSeats;
        public int productionYear;
        public float mileage;
        public float fuelConsumption;
        public long basePrice;
        public long deposit;
        public String address;
        public String description;
        public String additionalFunction;
        public String termOfUse;
        public boolean isAutomatic;
        public boolean isGasoline;

        // Documents
        public String registrationPaperUrl;
        public boolean registrationPaperUriIsVerified;
        public String certificateOfInspectionUrl;
        public boolean certificateOfInspectionUriIsVerified;
        public String insuranceUrl;
        public boolean insuranceUriIsVerified;

        // Images
        public String carImageFrontUrl;
        public String carImageBackUrl;
        public String carImageLeftUrl;
        public String carImageRightUrl;

        public double averageRatingByCar;
        public long noOfRides;
    }
}
