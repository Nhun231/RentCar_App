package com.anhbhn.rentcar;

public class LoginResponse {
    public int code;
    public String message;
    public Data data;

    public static class Data {
        public String userRole;
        public String fullName;
        public String csrfToken;
    }
}
