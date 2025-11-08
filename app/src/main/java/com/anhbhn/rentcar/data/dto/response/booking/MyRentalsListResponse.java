package com.anhbhn.rentcar.data.dto.response.booking;

import com.google.gson.annotations.SerializedName;

public class MyRentalsListResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private BookingListResponse data;

    public MyRentalsListResponse() {}

    public MyRentalsListResponse(int code, String message, BookingListResponse data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // --- Getters & Setters ---

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BookingListResponse getData() {
        return data;
    }

    public void setData(BookingListResponse data) {
        this.data = data;
    }
}