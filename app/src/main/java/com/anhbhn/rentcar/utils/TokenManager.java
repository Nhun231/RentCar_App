package com.anhbhn.rentcar.utils;
import android.content.Context;
import android.content.SharedPreferences;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;

public class TokenManager {

    private static final String PREF_NAME = "tokenPrefs";
    private static final String KEY_TOKEN = "csrfToken";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_JWT_COOKIE = "jwtCookie";
    private static final String KEY_REFRESH_TOKEN_COOKIE = "refreshTokenCookie";

    public static void saveToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public static void saveUserRole(Context context, String userRole) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ROLE, userRole);
        editor.apply();
    }

    public static String getUserRole(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_ROLE, null);
    }

    // Save JWT cookie value for persistence
    public static void saveJwtCookie(Context context, String jwtCookieValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_JWT_COOKIE, jwtCookieValue);
        editor.apply();
    }

    public static String getJwtCookie(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_JWT_COOKIE, null);
    }

    // Save refresh token cookie value for persistence
    public static void saveRefreshTokenCookie(Context context, String refreshTokenCookieValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_REFRESH_TOKEN_COOKIE, refreshTokenCookieValue);
        editor.apply();
    }

    public static String getRefreshTokenCookie(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_REFRESH_TOKEN_COOKIE, null);
    }

    // Restore cookies to CookieManager on app startup
    public static void restoreCookies(Context context, CookieManager cookieManager) {
        String jwtCookieValue = getJwtCookie(context);
        String refreshTokenCookieValue = getRefreshTokenCookie(context);
        
        if (jwtCookieValue != null || refreshTokenCookieValue != null) {
            try {
                // Use the base URL to restore cookies
                URI baseUri = URI.create("http://10.0.2.2:8080/karental");
                
                if (jwtCookieValue != null) {
                    HttpCookie jwtCookie = new HttpCookie("karental-jwt", jwtCookieValue);
                    jwtCookie.setPath("/karental");
                    jwtCookie.setDomain("localhost");
                    // Set a long expiration (1 year from now)
                    jwtCookie.setMaxAge(31536000L);
                    cookieManager.getCookieStore().add(baseUri, jwtCookie);
                }
                
                if (refreshTokenCookieValue != null) {
                    HttpCookie refreshCookie = new HttpCookie("karental-jwt-refresh", refreshTokenCookieValue);
                    refreshCookie.setPath("/karental/auth/refresh-token");
                    refreshCookie.setDomain("localhost");
                    // Set a long expiration (3 months from now)
                    refreshCookie.setMaxAge(7776000L);
                    cookieManager.getCookieStore().add(baseUri, refreshCookie);
                }
            } catch (Exception e) {
                // Ignore cookie restoration errors
            }
        }
    }

    public static void clearToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_JWT_COOKIE);
        editor.remove(KEY_REFRESH_TOKEN_COOKIE);
        editor.apply();
    }
}
