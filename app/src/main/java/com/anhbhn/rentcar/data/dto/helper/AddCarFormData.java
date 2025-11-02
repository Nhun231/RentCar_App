package com.anhbhn.rentcar.data.dto.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AddCarFormData {
    private final Map<String, RequestBody> fields;
    private final List<MultipartBody.Part> files;

    public AddCarFormData(Map<String, RequestBody> fields, List<MultipartBody.Part> files) {
        this.fields = fields;
        this.files = files != null ? files : new ArrayList<>();
    }

    public Map<String, RequestBody> getFields() {
        return fields;
    }

    public List<MultipartBody.Part> getFiles() {
        return files;
    }

    /**
     * Dùng để thêm file động sau khi khởi tạo (nếu cần)
     */
    public void addFile(MultipartBody.Part filePart) {
        if (filePart != null) {
            files.add(filePart);
        }
    }

    /**
     * Dùng để thêm nhiều file cùng lúc
     */
    public void addFiles(List<MultipartBody.Part> fileParts) {
        if (fileParts != null && !fileParts.isEmpty()) {
            files.addAll(fileParts);
        }
    }
}
