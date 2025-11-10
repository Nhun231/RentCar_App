package com.anhbhn.rentcar.data.remote;

import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.request.auth.RegisterRequest;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.dto.response.auth.RefreshTokenResponse;
import com.anhbhn.rentcar.data.dto.response.auth.RegisterResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingListResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.booking.WalletResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarDetailResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.dto.response.car.SearchCarResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    //Auth
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @GET("auth/refresh-token")
    Call<RefreshTokenResponse> refreshToken();
    
    //User
    @POST("user/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    //Car
    @Multipart
    @POST("car/car-owner/add-car")
    Call<CarResponse> addCar(
            @PartMap Map<String, RequestBody> fields,
            @Part List<MultipartBody.Part> files
    );
    
    //Search Car
    @GET("car/customer/search-car")
    Call<SearchCarResponse> searchCars(
            @Query("address") String address,
            @Query("pickUpTime") String pickUpTime,
            @Query("dropOffTime") String dropOffTime,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );
    
    //Get Car Detail
    @GET("car/customer/car-detail")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<CarDetailResponse>> getCarDetail(
            @Query("carId") String carId,
            @Query("pickUpTime") String pickUpTime,
            @Query("dropOffTime") String dropOffTime
    );
    
    //Booking
    @Multipart
    @POST("booking/customer/create-book")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<BookingResponse>> createBooking(
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part driverDrivingLicense
    );
    
    @GET("booking/customer/my-bookings")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<BookingListResponse>> getMyBookings(
            @Query("page") int page,
            @Query("size") int size,
            @Query("status") String status,
            @Query("sort") String sort
    );
    
    @GET("booking/get-wallet")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<WalletResponse>> getWallet();
    
    @GET("booking/customer/{bookingNumber}")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<BookingResponse>> getBookingDetails(
            @Path("bookingNumber") String bookingNumber
    );
    
    @PUT("booking/customer/cancel-booking/{bookingNumber}")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<BookingResponse>> cancelBooking(
            @Path("bookingNumber") String bookingNumber
    );
    
    @PUT("booking/customer/confirm-pick-up/{bookingNumber}")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<BookingResponse>> confirmPickUp(
            @Path("bookingNumber") String bookingNumber
    );
    
    @PUT("booking/customer/return-car/{bookingNumber}")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<BookingResponse>> returnCar(
            @Path("bookingNumber") String bookingNumber
    );
    
    @PUT("booking/customer/pay-deposit-again/{bookingNumber}")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<BookingResponse>> payDepositAgain(
            @Path("bookingNumber") String bookingNumber
    );
    
    @PUT("booking/customer/pay-total-payment-again/{bookingNumber}")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<BookingResponse>> payTotalPaymentAgain(
            @Path("bookingNumber") String bookingNumber
    );
    
    //User Profile
    @GET("user/edit-profile")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> getUserProfile();
    
    @Multipart
    @PUT("user/edit-profile")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> editProfile(
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part drivingLicense
    );
}
