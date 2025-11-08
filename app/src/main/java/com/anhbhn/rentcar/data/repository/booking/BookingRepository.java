package com.anhbhn.rentcar.data.repository.booking;

import android.content.Context;

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
}
