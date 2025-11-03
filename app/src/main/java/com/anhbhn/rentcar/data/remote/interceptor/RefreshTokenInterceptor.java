package com.anhbhn.rentcar.data.remote.interceptor;

import android.content.Context;
import androidx.annotation.NonNull;

import com.anhbhn.rentcar.data.dto.response.auth.RefreshTokenResponse;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;
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

    public RefreshTokenInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String currentToken = TokenManager.getToken(context);

        // Gửi token hiện tại nếu có
        if (currentToken != null) {
            request = request.newBuilder()
                    .header("X-CSRF-TOKEN", currentToken)
                    .build();
        }

        Response response = chain.proceed(request);

        // Nếu request trả về 401 (token hết hạn)
        if (response.code() == 401) {

            if (isRefreshing.compareAndSet(false, true)) {
                try {
                    ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
                    Call<RefreshTokenResponse> refreshCall = apiService.refreshToken();
                    retrofit2.Response<RefreshTokenResponse> refreshResponse = refreshCall.execute();

                    if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                        String newToken = refreshResponse.body().data;
                        if (newToken != null && !newToken.isEmpty()) {
                            TokenManager.saveToken(context, newToken);
                            response.close();
                            // Retry request với token mới
                            Request newRequest = request.newBuilder()
                                    .header("X-CSRF-TOKEN", newToken)
                                    .build();
                            return chain.proceed(newRequest);
                        }
                    }

                    // Nếu refresh thất bại → xóa token
                    TokenManager.clearToken(context);
                    // TODO: có thể broadcast sự kiện logout tại đây
                    response.close(); // Đóng Response 401 cũ

                    return new Response.Builder()
                            .request(request)
                            .protocol(response.protocol())
                            .code(403)
                            .message("Token refresh failed. User logged out.")
                            .body(okhttp3.ResponseBody.create(null, new byte[0]))
                            .build();

                } catch (Exception e) {
                    response.close();
                    throw e;
                }
                finally {
                    isRefreshing.set(false);
                }
            }
        }

        return response;
    }
}
