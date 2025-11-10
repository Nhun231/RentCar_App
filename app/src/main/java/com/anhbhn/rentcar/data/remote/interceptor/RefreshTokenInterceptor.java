package com.anhbhn.rentcar.data.remote.interceptor;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.anhbhn.rentcar.data.dto.response.auth.RefreshTokenResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;
import com.anhbhn.rentcar.ui.auth.LoginActivity;
import com.anhbhn.rentcar.utils.TokenManager;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

public class RefreshTokenInterceptor implements Interceptor {

    private final Context context;
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);
    private final Object lock = new Object();
    private String newAccessToken = null;
    private boolean refreshFailed = false;

    public RefreshTokenInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String requestUrl = request.url().toString();
        
        // Skip refresh token endpoint - don't intercept it (let it go through without token)
        if (requestUrl.contains("/auth/refresh-token")) {
            return chain.proceed(request);
        }

        String currentToken = TokenManager.getToken(context);

        // Add current token to request if available
        if (currentToken != null) {
            request = request.newBuilder()
                    .header("X-CSRF-TOKEN", currentToken)
                    .build();
        }

        Response response = chain.proceed(request);

        // If request returns 401 (access token expired), try to refresh
        if (response.code() == 401) {
            // Close the original 401 response
            response.close();

            // Try to refresh the access token
            synchronized (lock) {
                if (isRefreshing.compareAndSet(false, true)) {
                    // This thread will handle the refresh
                    try {
                        return refreshAccessTokenAndRetry(chain, request);
                    } finally {
                        isRefreshing.set(false);
                        newAccessToken = null;
                        refreshFailed = false;
                        lock.notifyAll();
                    }
                } else {
                    // Another thread is refreshing, wait for it
                    try {
                        while (isRefreshing.get()) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        response.close();
                        throw new IOException("Interrupted while waiting for token refresh", e);
                    }

                    // Check if refresh was successful
                    if (refreshFailed) {
                        response.close();
                        return new Response.Builder()
                                .request(request)
                                .protocol(response.protocol())
                                .code(401)
                                .message("Token refresh failed")
                                .body(okhttp3.ResponseBody.create(null, new byte[0]))
                                .build();
                    }

                    // Retry with new token
                    if (newAccessToken != null) {
                        response.close();
                        Request newRequest = request.newBuilder()
                                .header("X-CSRF-TOKEN", newAccessToken)
                                .build();
                        return chain.proceed(newRequest);
                    }
                }
            }
        }

        return response;
    }

    private Response refreshAccessTokenAndRetry(Chain chain, Request originalRequest) throws IOException {
        try {
            // Use base client (without interceptors) to avoid recursion
            ApiService apiService = ApiClient.getBaseClient().create(ApiService.class);
            Call<RefreshTokenResponse> refreshCall = apiService.refreshToken();
            retrofit2.Response<RefreshTokenResponse> refreshResponse = refreshCall.execute();

            // Check if refresh token API itself returned 401 (refresh token expired)
            if (refreshResponse.code() == 401) {
                // Refresh token has expired - forward to login
                handleRefreshTokenExpired();
                refreshFailed = true;
                return new Response.Builder()
                        .request(originalRequest)
                        .protocol(refreshResponse.raw().protocol())
                        .code(401)
                        .message("Refresh token expired")
                        .body(okhttp3.ResponseBody.create(null, new byte[0]))
                        .build();
            }

            // Check if refresh was successful
            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                String newToken = refreshResponse.body().data;
                if (newToken != null && !newToken.isEmpty()) {
                    // Save new access token
                    TokenManager.saveToken(context, newToken);
                    newAccessToken = newToken;

                    // Extract and save new JWT cookies if present
                    extractAndSaveCookies(refreshResponse);

                    // Retry original request with new token
                    Request newRequest = originalRequest.newBuilder()
                            .header("X-CSRF-TOKEN", newToken)
                            .build();
                    return chain.proceed(newRequest);
                }
            }

            // Refresh failed for other reasons
            handleRefreshTokenExpired();
            refreshFailed = true;
            return new Response.Builder()
                    .request(originalRequest)
                    .protocol(refreshResponse.raw().protocol())
                    .code(401)
                    .message("Token refresh failed")
                    .body(okhttp3.ResponseBody.create(null, new byte[0]))
                    .build();

        } catch (Exception e) {
            handleRefreshTokenExpired();
            refreshFailed = true;
            throw new IOException("Failed to refresh token", e);
        }
    }

    private void extractAndSaveCookies(retrofit2.Response<RefreshTokenResponse> response) {
        try {
            java.util.List<String> cookies = response.headers().values("Set-Cookie");
            for (String cookie : cookies) {
                if (cookie.contains("karental-jwt=") && !cookie.contains("refresh")) {
                    int startIndex = cookie.indexOf("karental-jwt=") + "karental-jwt=".length();
                    int endIndex = cookie.indexOf(";", startIndex);
                    if (endIndex == -1) endIndex = cookie.length();
                    String jwtValue = cookie.substring(startIndex, endIndex).trim();
                    TokenManager.saveJwtCookie(context, jwtValue);
                } else if (cookie.contains("karental-jwt-refresh=")) {
                    int startIndex = cookie.indexOf("karental-jwt-refresh=") + "karental-jwt-refresh=".length();
                    int endIndex = cookie.indexOf(";", startIndex);
                    if (endIndex == -1) endIndex = cookie.length();
                    String refreshValue = cookie.substring(startIndex, endIndex).trim();
                    TokenManager.saveRefreshTokenCookie(context, refreshValue);
                }
            }
        } catch (Exception e) {
            // Ignore cookie extraction errors
        }
    }

    private void handleRefreshTokenExpired() {
        // Clear all tokens
        TokenManager.clearToken(context);

        // Navigate to LoginActivity on main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
