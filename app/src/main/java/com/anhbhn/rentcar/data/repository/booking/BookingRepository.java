package com.anhbhn.rentcar.data.repository.booking;

import android.content.Context;

import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.booking.MyRentalsListResponse;
import com.anhbhn.rentcar.data.dto.response.car.MyCarsPageResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;

import retrofit2.Call;

public class BookingRepository {
    private final ApiService apiService;
    private final Context context;
    public BookingRepository(Context context) {
        this.context = context;
        apiService = ApiClient.getClient(context).create(ApiService.class);
    }
    public Call<MyRentalsListResponse> getOwnerBookings(int page, int size, String status, String sort) {
        if (sort == null || sort.isEmpty()) {
            sort = "updatedAt,DESC";
        }

        // Gọi phương thức API Service tương ứng
        return apiService.getOwnerBookings(page, size, status, sort);
    }
    public Call<BookingResponse> getBookingDetails(String bookingNumber){
        return apiService.getBookingDetails(bookingNumber);
    }
    public Call<BookingResponse> confirmBooking(String bookingNumber){
        return apiService.confirmBooking(bookingNumber);
    }
    public Call<BookingResponse> rejectBooking(String bookingNumber){
        return apiService.rejectBooking(bookingNumber);
    }
    public Call<BookingResponse> confirmEarlyReturnCar(String bookingNumber){
        return apiService.confirmEarlyReturnCar(bookingNumber);
    }
    public Call<BookingResponse> rejectEarlyReturnCar(String bookingNumber){
        return apiService.rejectEarlyReturnCar(bookingNumber);
    }
}
