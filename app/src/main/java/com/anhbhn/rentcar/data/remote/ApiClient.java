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
import java.util.concurrent.TimeUnit;

// SỬ DỤNG THƯ VIỆN BACKPORT THAY THẾ CHO java.time
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.threeten.bp.LocalDateTime; // ĐÃ SỬA
import org.threeten.bp.format.DateTimeFormatter; // ĐÃ SỬA


public class ApiClient {
    private static Retrofit retrofitWithToken;
    private static Retrofit retrofitWithoutToken;
    private static final CookieManager cookieManager = new CookieManager();

    // Khởi tạo Gson có hỗ trợ LocalDateTime
    private static final Gson GSON;

    static {
        // Cấu hình chấp nhận mọi cookie
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        // --- LOGIC XỬ LÝ LOCALDATETIME BẰNG THREETENABP ---
        // Sử dụng định dạng ISO 8601
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // Deserializer: Chuyển chuỗi JSON sang đối tượng LocalDateTime
        JsonDeserializer<LocalDateTime> localDateTimeDeserializer = (json, typeOfT, context1) ->
                LocalDateTime.parse(json.getAsString(), formatter);

        // Serializer: Chuyển đối tượng LocalDateTime sang chuỗi JSON (Dành cho PUT/POST)
        JsonSerializer<LocalDateTime> localDateTimeSerializer = (src, typeOfSrc, context1) ->
                new JsonPrimitive(src.format(formatter));

        // Xây dựng Gson instance
        GSON = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, localDateTimeDeserializer)
                .registerTypeAdapter(LocalDateTime.class, localDateTimeSerializer)
                .create();
        // --- KẾT THÚC LOGIC XỬ LÝ LOCALDATETIME ---
    }


    // Client có interceptor (dành cho API cần token)
    public static Retrofit getClient(Context context) {
        if (retrofitWithToken == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .connectTimeout(45, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .writeTimeout(45, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(new RefreshTokenInterceptor(context))
                    .build();

            retrofitWithToken = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(GSON)) // SỬ DỤNG GSON TÙY CHỈNH
                    .client(client)
                    .build();
        }
        return retrofitWithToken;
    }

    // Client không có interceptor
    public static Retrofit getBaseClient() {
        if (retrofitWithoutToken == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .build();

            retrofitWithoutToken = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(GSON)) // SỬ DỤNG GSON TÙY CHỈNH
                    .client(client)
                    .build();
        }
        return retrofitWithoutToken;
    }
}