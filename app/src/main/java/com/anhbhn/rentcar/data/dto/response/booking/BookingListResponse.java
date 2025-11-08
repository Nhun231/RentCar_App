package com.anhbhn.rentcar.data.dto.response.booking;

import com.anhbhn.rentcar.data.dto.helper.PageResponse;
import com.google.gson.annotations.SerializedName;


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
}