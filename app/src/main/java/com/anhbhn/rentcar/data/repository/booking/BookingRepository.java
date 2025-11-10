package com.anhbhn.rentcar.data.repository.booking;

import android.content.Context;

import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingListResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingResponse;
import com.anhbhn.rentcar.data.dto.response.booking.WalletResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class BookingRepository {
    private ApiService apiService;
    private Context context;

    public BookingRepository(Context context) {
        this.context = context;
        this.apiService = ApiClient.getClient(context).create(ApiService.class);
    }

    public Call<ApiResponse<BookingResponse>> createBooking(
            String carId,
            String pickUpLocation,
            String pickUpTime,
            String dropOffTime,
            String paymentType,
            boolean isDriver,
            String driverFullName,
            String driverPhoneNumber,
            String driverNationalId,
            String driverDob,
            String driverEmail,
            String driverCityProvince,
            String driverDistrict,
            String driverWard,
            String driverHouseNumberStreet,
            File drivingLicenseFile
    ) {
        Map<String, RequestBody> fields = new HashMap<>();
        
        // Add text fields
        fields.put("carId", createRequestBody(carId));
        fields.put("pickUpLocation", createRequestBody(pickUpLocation));
        fields.put("pickUpTime", createRequestBody(pickUpTime));
        fields.put("dropOffTime", createRequestBody(dropOffTime));
        fields.put("paymentType", createRequestBody(paymentType));
        fields.put("isDriver", createRequestBody(String.valueOf(isDriver)));
        
        // Add driver fields only if isDriver is true
        if (isDriver) {
            fields.put("driverFullName", createRequestBody(driverFullName));
            fields.put("driverPhoneNumber", createRequestBody(driverPhoneNumber));
            fields.put("driverNationalId", createRequestBody(driverNationalId));
            fields.put("driverDob", createRequestBody(driverDob));
            fields.put("driverEmail", createRequestBody(driverEmail));
            fields.put("driverCityProvince", createRequestBody(driverCityProvince));
            fields.put("driverDistrict", createRequestBody(driverDistrict));
            fields.put("driverWard", createRequestBody(driverWard));
            fields.put("driverHouseNumberStreet", createRequestBody(driverHouseNumberStreet));
        }
        
        // Handle file upload
        // If isDriver is true: upload driver's license
        // If isDriver is false: upload renter's license if provided (otherwise backend uses account's license)
        MultipartBody.Part filePart = null;
        if (drivingLicenseFile != null && drivingLicenseFile.exists()) {
            // Determine media type based on file extension
            String fileName = drivingLicenseFile.getName().toLowerCase();
            String mediaType = "application/octet-stream"; // Default
            
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                mediaType = "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                mediaType = "image/png";
            } else if (fileName.endsWith(".pdf")) {
                mediaType = "application/pdf";
            } else if (fileName.endsWith(".doc")) {
                mediaType = "application/msword";
            } else if (fileName.endsWith(".docx")) {
                mediaType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            
            RequestBody fileRequestBody = RequestBody.create(
                    okhttp3.MediaType.parse(mediaType),
                    drivingLicenseFile
            );
            filePart = MultipartBody.Part.createFormData(
                    "driverDrivingLicense",
                    drivingLicenseFile.getName(),
                    fileRequestBody
            );
        }
        
        return apiService.createBooking(fields, filePart);
    }

    private RequestBody createRequestBody(String value) {
        return RequestBody.create(okhttp3.MediaType.parse("text/plain"), value != null ? value : "");
    }

    public Call<ApiResponse<BookingListResponse>> getMyBookings(int page, int size, String status, String sort) {
        return apiService.getMyBookings(page, size, status, sort);
    }

    public Call<ApiResponse<WalletResponse>> getWallet() {
        return apiService.getWallet();
    }

    public Call<ApiResponse<BookingResponse>> getBookingDetails(String bookingNumber) {
        return apiService.getBookingDetails(bookingNumber);
    }

    public Call<ApiResponse<BookingResponse>> cancelBooking(String bookingNumber) {
        return apiService.cancelBooking(bookingNumber);
    }

    public Call<ApiResponse<BookingResponse>> confirmPickUp(String bookingNumber) {
        return apiService.confirmPickUp(bookingNumber);
    }

    public Call<ApiResponse<BookingResponse>> returnCar(String bookingNumber) {
        return apiService.returnCar(bookingNumber);
    }

    public Call<ApiResponse<BookingResponse>> payDepositAgain(String bookingNumber) {
        return apiService.payDepositAgain(bookingNumber);
    }

    public Call<ApiResponse<BookingResponse>> payTotalPaymentAgain(String bookingNumber) {
        return apiService.payTotalPaymentAgain(bookingNumber);
    }
}

