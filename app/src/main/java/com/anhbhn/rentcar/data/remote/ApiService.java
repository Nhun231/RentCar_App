package com.anhbhn.rentcar.data.remote;

import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

}
