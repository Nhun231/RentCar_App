package com.anhbhn.rentcar.data.repository.auth;

import com.anhbhn.rentcar.data.dto.request.auth.LoginRequest;
import com.anhbhn.rentcar.data.dto.response.auth.LoginResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;

import retrofit2.Call;

public class AuthRepository {
    private final ApiService apiService;

    public AuthRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public Call<LoginResponse> login(LoginRequest request) {
        return apiService.login(request);
    }
}
