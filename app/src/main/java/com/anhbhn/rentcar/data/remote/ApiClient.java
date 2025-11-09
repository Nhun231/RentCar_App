package com.anhbhn.rentcar.data.remote;

import android.content.Context;
import android.util.Log;
import com.anhbhn.rentcar.data.remote.interceptor.AuthInterceptor;
import com.anhbhn.rentcar.data.remote.interceptor.RefreshTokenInterceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
    private static final JavaNetCookieJar cookieJar;
    static {
        // Cấu hình chấp nhận mọi cookie
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        // Create a single shared cookie jar instance
        cookieJar = new JavaNetCookieJar(cookieManager);
    }

    // Get CookieManager for cookie restoration on app startup
    public static CookieManager getCookieManager() {
        return cookieManager;
    }

    // Client có interceptor (dành cho API cần token)
    public static Retrofit getClient(Context context) {
        if (retrofitWithToken == null) {
            // Add logging interceptor to debug authenticated requests
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                Log.d("API_REQUEST", message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(new RefreshTokenInterceptor(context))
                    .addInterceptor(loggingInterceptor)
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
            // Configure Gson to serialize properly - match backend Jackson expectations
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            // Add logging interceptor to debug requests
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                Log.d("API_REQUEST", message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofitWithoutToken = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofitWithoutToken;
    }
}
