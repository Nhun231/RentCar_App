package com.anhbhn.rentcar.data.remote;

import android.content.Context;

import com.anhbhn.rentcar.data.remote.interceptor.AuthInterceptor;
import com.anhbhn.rentcar.data.remote.interceptor.RefreshTokenInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofitWithToken;
    private static Retrofit retrofitWithoutToken;

    // Client có interceptor (dành cho API cần token)
    public static Retrofit getClient(Context context) {
        if (retrofitWithToken == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(new RefreshTokenInterceptor(context))
                    .build();

            retrofitWithToken = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofitWithToken;
    }

    // Client không có interceptor (dành cho login, refresh token, v.v.)
    public static Retrofit getBaseClient() {
        if (retrofitWithoutToken == null) {
            retrofitWithoutToken = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitWithoutToken;
    }
}
