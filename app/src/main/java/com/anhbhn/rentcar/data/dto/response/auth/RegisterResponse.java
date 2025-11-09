package com.anhbhn.rentcar.data.dto.response.auth;

public class RegisterResponse {
    public int code;
    public String message;
    public Data data;

    public static class Data {
        public String fullName;
        public String email;
        public String phoneNumber;
        public String role;
    }
}

