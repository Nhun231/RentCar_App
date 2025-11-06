package com.anhbhn.rentcar.data.remote;

import com.anhbhn.rentcar.data.dto.helper.PageResponse;
import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.dto.response.auth.RefreshTokenResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.anhbhn.rentcar.data.dto.response.car.MyCarsPageResponse;

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
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    @GET("car/car-owner/my-cars")
    Call<MyCarsPageResponse> getMyCars(@Query("page") int page,
                                       @Query("size") int size,
                                       @Query("sort") String sort);
    @GET("car/car-owner/{carId}")
    Call<CarResponse> getCarDetailsForOwner(@Path("carId") String carId);
}
