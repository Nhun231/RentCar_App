package com.anhbhn.rentcar.data.remote;

import android.content.Context;
import com.anhbhn.rentcar.data.remote.interceptor.AuthInterceptor;
import com.anhbhn.rentcar.data.remote.interceptor.RefreshTokenInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Import cần thiết cho CookieJar
import okhttp3.JavaNetCookieJar;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class ApiClient {
    private static Retrofit retrofitWithToken;
    private static Retrofit retrofitWithoutToken;
    private static final CookieManager cookieManager = new CookieManager();
    static {
        // Cấu hình chấp nhận mọi cookie
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    // Client có interceptor (dành cho API cần token)
    public static Retrofit getClient(Context context) {
        if (retrofitWithToken == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(cookieManager))
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
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .build();

            retrofitWithoutToken = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofitWithoutToken;
    }
}
