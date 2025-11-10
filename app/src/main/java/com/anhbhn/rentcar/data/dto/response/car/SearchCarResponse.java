package com.anhbhn.rentcar.data.dto.response.car;

import java.util.List;

public class SearchCarResponse {
    public int code;
    public String message;
    public Data data;

    public static class Data {
        public List<CarThumbnailResponse> content;
        public int totalElements;
        public int totalPages;
        public int size;
        public int number;
        public boolean first;
        public boolean last;
    }
}

