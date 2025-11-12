package com.anhbhn.rentcar.data.repository.user;

import android.content.Context;

import com.anhbhn.rentcar.data.dto.request.user.EditPasswordRequest;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;

import retrofit2.Call;

public class UserRepository {
    private final ApiService apiService;
    private final Context context;

    public UserRepository(Context context) {
        this.context = context;
        apiService = ApiClient.getClient(context).create(ApiService.class);
    }
    public Call<ApiResponse<String>> changePassword(EditPasswordRequest request) {
        return apiService.changePassword(request);
    }
}
