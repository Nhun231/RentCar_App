package com.anhbhn.rentcar.data.repository.wallet;

import android.content.Context;

import com.anhbhn.rentcar.data.dto.request.user.EditPasswordRequest;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.transaction.ListTransactionResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;

import retrofit2.Call;

public class WalletRepository {
    private final ApiService apiService;
    private final Context context;

    public WalletRepository(Context context) {
        this.context = context;
        apiService = ApiClient.getClient(context).create(ApiService.class);
    }
    public Call<ApiResponse<ListTransactionResponse>> getAllTransactionList() {
        return apiService.getAllTransactionList(true);
    }
}
