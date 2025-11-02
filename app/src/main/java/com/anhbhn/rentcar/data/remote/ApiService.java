package com.anhbhn.rentcar.data.remote;

import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.dto.response.auth.RefreshTokenResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface ApiService {

    //Auth
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @GET("auth/refresh-token")
    Call<RefreshTokenResponse> refreshToken();

    //Car
    @Multipart
    @POST("car/car-owner/add-car")
    Call<CarResponse> addCar(
            @PartMap Map<String, RequestBody> fields,
            @Part List<MultipartBody.Part> files
    );
}
