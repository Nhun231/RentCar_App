package com.anhbhn.rentcar.data.dto.response.car;

import com.anhbhn.rentcar.data.dto.helper.PageResponse;

public class MyCarsPageResponse {
    private int code;
    private String message;
    private PageResponse<CarThumbnailResponse> data;

    public MyCarsPageResponse(int code, String message, PageResponse<CarThumbnailResponse> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public MyCarsPageResponse() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PageResponse<CarThumbnailResponse> getData() {
        return data;
    }

    public void setData(PageResponse<CarThumbnailResponse> data) {
        this.data = data;
    }
}
