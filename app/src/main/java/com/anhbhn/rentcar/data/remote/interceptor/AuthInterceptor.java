package com.anhbhn.rentcar.data.remote.interceptor;

import android.content.Context;
import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import com.anhbhn.rentcar.utils.TokenManager;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String csrfToken = TokenManager.getToken(context);
        Request.Builder builder = original.newBuilder();
        if (csrfToken != null) {
            builder.header("X-CSRF-TOKEN", csrfToken);
        }
        return chain.proceed(builder.build());
    }
}
