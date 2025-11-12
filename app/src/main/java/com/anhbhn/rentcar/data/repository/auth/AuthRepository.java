package com.anhbhn.rentcar.data.repository.auth;

import android.content.Context;

import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.request.auth.RegisterRequest;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.dto.response.auth.RefreshTokenResponse;
import com.anhbhn.rentcar.data.dto.response.auth.RegisterResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;

import retrofit2.Call;

public class AuthRepository {
    private final ApiService apiService;

    public AuthRepository(Context context) {
        apiService = ApiClient.getBaseClient().create(ApiService.class);
    }

    public Call<LoginResponse> login(LoginRequest request) {
        return apiService.login(request);
    }
    
    public Call<RegisterResponse> register(RegisterRequest request) {
        return apiService.register(request);
    }
    
    public Call<RefreshTokenResponse> refreshToken(){
        return apiService.refreshToken();
    }
    public Call<ApiResponse<String>> logout() {
        return apiService.logout();
    }
    public Call<ApiResponse<String>> sendForgotPasswordEmail(String email) {
        return apiService.sendForgotPasswordEmail(email);
    }
}
