package com.anhbhn.rentcar.data.dto.helper;

import java.util.List;
import com.google.gson.annotations.SerializedName;

/**
 * Lớp generic ánh xạ cấu trúc Page<T> từ phản hồi API Spring Boot.
 * Dùng để chứa danh sách nội dung (content) và thông tin phân trang.
 */
public class PageResponse<T> {

    // Danh sách các đối tượng thực tế (ví dụ: CarThumbnailDetails)
    @SerializedName("content")
    private List<T> content;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalElements")
    private long totalElements;

    // Trang hiện tại (page number - thường là 0-based)
    @SerializedName("number")
    private int number;

    // Kích thước trang
    @SerializedName("size")
    private int size;

    // Cờ báo hiệu trang cuối
    @SerializedName("last")
    private boolean last;

    // Cờ báo hiệu trang đầu
    @SerializedName("first")
    private boolean first;

    // Constructor mặc định (cần cho Gson)
    public PageResponse() {}

    // --- GETTERS (Cần thiết) ---

    public List<T> getContent() {
        return content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isFirst() {
        return first;
    }
}