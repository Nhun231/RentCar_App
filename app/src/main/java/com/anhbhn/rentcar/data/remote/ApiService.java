package com.anhbhn.rentcar.data.remote;

import com.anhbhn.rentcar.data.dto.helper.PageResponse;
import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.request.auth.RegisterRequest;
import com.anhbhn.rentcar.data.dto.request.user.EditPasswordRequest;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.dto.response.auth.RefreshTokenResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.booking.MyRentalsListResponse;
import com.anhbhn.rentcar.data.dto.response.auth.RegisterResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingListResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.booking.WalletResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarDetailResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.anhbhn.rentcar.data.dto.response.car.MyCarsPageResponse;
import com.anhbhn.rentcar.data.dto.response.car.SearchCarResponse;
import com.anhbhn.rentcar.data.dto.response.transaction.ListTransactionResponse;
import com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse;

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
    @GET("auth/logout")
    Call<ApiResponse<String>> logout();
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @GET("auth/refresh-token")
    Call<RefreshTokenResponse> refreshToken();
    @GET("auth/forgot-password/{email}")
    Call<ApiResponse<String>> sendForgotPasswordEmail(@Path("email") String email);
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
    @GET("car/car-owner/my-cars")
    Call<MyCarsPageResponse> getMyCars(@Query("page") int page,
                                       @Query("size") int size,
                                       @Query("sort") String sort);
    @GET("car/car-owner/{carId}")
    Call<CarResponse> getCarDetailsForOwner(@Path("carId") String carId);
    @Multipart
    @PUT("car/car-owner/edit-car/{carId}")
    Call<CarResponse> editCar(
            @Path("carId") String carId,
            @PartMap Map<String, RequestBody> fields,
            @Part List<MultipartBody.Part> files
    );

    //Booking
    @GET("booking/car-owner/rentals")
    Call<MyRentalsListResponse> getOwnerBookings(
            @Query("page") int page,
            @Query("size") int size,
            @Query("status") String status, // EBookingStatus.name() hoặc null
            @Query("sort") String sort // Ví dụ: "updatedAt,DESC"
    );
    @GET("booking/car-owner/{bookingNumber}")
    Call<BookingResponse> getBookingDetailsOwner(@Path("bookingNumber") String bookingNumber);
    @PUT("booking/car-owner/{bookingNumber}/confirm")
    Call<BookingResponse> confirmBooking(@Path("bookingNumber") String bookingNumber);
    @PUT("booking/car-owner/reject-booking/{bookingNumber}")
    Call<BookingResponse> rejectBooking(@Path("bookingNumber") String bookingNumber);
    @PUT("booking/car-owner/confirm-early-return/{bookingNumber}")
    Call<BookingResponse> confirmEarlyReturnCar(@Path("bookingNumber") String bookingNumber);
    @PUT("booking/car-owner/reject-early-return/{bookingNumber}")
    Call<BookingResponse> rejectEarlyReturnCar(@Path("bookingNumber") String bookingNumber);


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
    Call<BookingResponse> createBooking(
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
    Call<BookingResponse> getBookingDetailsCustomer(
            @Path("bookingNumber") String bookingNumber
    );

    @PUT("booking/customer/cancel-booking/{bookingNumber}")
    Call<BookingResponse> cancelBooking(
            @Path("bookingNumber") String bookingNumber
    );

    @PUT("booking/customer/confirm-pick-up/{bookingNumber}")
    Call<BookingResponse> confirmPickUp(
            @Path("bookingNumber") String bookingNumber
    );

    @PUT("booking/customer/return-car/{bookingNumber}")
    Call<BookingResponse> returnCar(
            @Path("bookingNumber") String bookingNumber
    );

    @PUT("booking/customer/pay-deposit-again/{bookingNumber}")
    Call<BookingResponse> payDepositAgain(
            @Path("bookingNumber") String bookingNumber
    );

    @PUT("booking/customer/pay-total-payment-again/{bookingNumber}")
    Call<BookingResponse> payTotalPaymentAgain(
            @Path("bookingNumber") String bookingNumber
    );

    //User Profile
    @GET("user/edit-profile")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> getUserProfile();
    @PUT("user/edit-password")
    Call<ApiResponse<String>> changePassword(@Body EditPasswordRequest request);

    @Multipart
    @PUT("user/edit-profile")
    Call<com.anhbhn.rentcar.data.dto.response.ApiResponse<com.anhbhn.rentcar.data.dto.response.user.EditProfileResponse>> editProfile(
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part drivingLicense
    );
    //Wallet
    @GET("transaction/transaction-list")
    Call<ApiResponse<ListTransactionResponse>> getAllTransactionList(@Query("all") boolean all);
}
