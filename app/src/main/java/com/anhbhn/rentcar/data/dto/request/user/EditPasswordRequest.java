package com.anhbhn.rentcar.data.dto.request.user;

public class EditPasswordRequest {
    String currentPassword;

    String newPassword;

    public EditPasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
