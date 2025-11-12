package com.anhbhn.rentcar.data.dto.response.transaction;

// Chú ý: Cần đảm bảo class LocalDateTime được import đúng cách (java.time.LocalDateTime)
import java.time.LocalDateTime;

public class TransactionResponse {

    private String createdAt;
    private String type;

    private String bookingNo;
    private String carName;
    private long amount;

    private String message;

    // Đã thay đổi ETransactionStatus thành String
    private String status;

    // --- Getters ---

    public String getCreatedAt() {
        return createdAt;
    }

    public String getType() {
        return type;
    }

    public String getBookingNo() {
        return bookingNo;
    }

    public String getCarName() {
        return carName;
    }

    public long getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

}