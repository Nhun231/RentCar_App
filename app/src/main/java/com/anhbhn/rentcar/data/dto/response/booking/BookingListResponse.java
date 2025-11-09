package com.anhbhn.rentcar.data.dto.response.booking;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class BookingListResponse implements Serializable {
    @SerializedName("totalOnGoingBookings")
    public int totalOnGoingBookings;
    
    @SerializedName("totalWaitingConfirmBooking")
    public int totalWaitingConfirmBooking;
    
    @SerializedName("bookings")
    public Data data;
    
    public static class Data implements Serializable {
        @SerializedName("content")
        public List<BookingThumbnailResponse> content;
        
        @SerializedName("totalElements")
        public int totalElements;
        
        @SerializedName("totalPages")
        public int totalPages;
        
        @SerializedName("size")
        public int size;
        
        @SerializedName("number")
        public int number;
        
        @SerializedName("first")
        public boolean first;
        
        @SerializedName("last")
        public boolean last;
    }
}

