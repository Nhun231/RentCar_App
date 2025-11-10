package com.anhbhn.rentcar.data.dto.response.booking;

import com.anhbhn.rentcar.data.dto.helper.PageResponse;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


public class BookingListResponse {

    @SerializedName("totalOnGoingBookings")
    private int totalOnGoingBookings;

    @SerializedName("totalWaitingConfirmBooking")
    private int totalWaitingConfirmBooking;

    // ✅ SỬ DỤNG PAGE RESPONSE CHUNG
    @SerializedName("bookings")
    private PageResponse<BookingThumbnailResponse> bookings;

    // --- Getters & Setters ---

    public int getTotalOnGoingBookings() {
        return totalOnGoingBookings;
    }

    public void setTotalOnGoingBookings(int totalOnGoingBookings) {
        this.totalOnGoingBookings = totalOnGoingBookings;
    }

    public int getTotalWaitingConfirmBooking() {
        return totalWaitingConfirmBooking;
    }

    public void setTotalWaitingConfirmBooking(int totalWaitingConfirmBooking) {
        this.totalWaitingConfirmBooking = totalWaitingConfirmBooking;
    }

    public PageResponse<BookingThumbnailResponse> getBookings() {
        return bookings;
    }

    public void setBookings(PageResponse<BookingThumbnailResponse> bookings) {
        this.bookings = bookings;
    }
//    public class Data implements Serializable {
//        @SerializedName("content")
//        public List<BookingThumbnailResponse> content;
//
//        @SerializedName("totalElements")
//        public int totalElements;
//
//        @SerializedName("totalPages")
//        public int totalPages;
//
//        @SerializedName("size")
//        public int size;
//
//        @SerializedName("number")
//        public int number;
//
//        @SerializedName("first")
//        public boolean first;
//
//        @SerializedName("last")
//        public boolean last;
//    }
}