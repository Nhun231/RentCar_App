package com.anhbhn.rentcar.data.mapper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.annotation.Nullable;

import com.anhbhn.rentcar.data.dto.helper.AddCarFormData; // Reusing AddCarFormData helper DTO
import com.anhbhn.rentcar.data.dto.request.car.EditCarRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditCarMapper {

    /**
     * Chuyển đổi EditCarRequest thành MultipartBody.Part cho Retrofit.
     * Lưu ý: Trong Edit, các trường text phải được gửi là RequestBody/String.
     *
     * @param context Context để truy cập ContentResolver.
     * @param request Dữ liệu chỉnh sửa xe.
     * @return AddCarFormData (chứa fields và files).
     */
    public static AddCarFormData toFormData(Context context, EditCarRequest request) {
        Map<String, RequestBody> fields = new HashMap<>();


        fields.put("mileage", createPartFromString(String.valueOf(request.getMileage())));
        fields.put("fuelConsumption", createPartFromString(String.valueOf(request.getFuelConsumption())));
        fields.put("basePrice", createPartFromString(String.valueOf(request.getBasePrice())));
        fields.put("deposit", createPartFromString(String.valueOf(request.getDeposit())));
        fields.put("address", createPartFromString(request.getAddress()));
        fields.put("description", createPartFromString(request.getDescription()));
        fields.put("additionalFunction", createPartFromString(request.getAdditionalFunction()));
        fields.put("termOfUse", createPartFromString(request.getTermOfUse()));

        // Trường trạng thái
        fields.put("status", createPartFromString(request.getStatus()));

        List<MultipartBody.Part> files = new ArrayList<>();

        // Tên trường phải khớp với tên @SerializedName trong EditCarRequest
        // (Ví dụ: "carImageFront", "carImageBack")
        addFilePart(context, files, "carImageFront", request.getCarImageFront());
        addFilePart(context, files, "carImageBack", request.getCarImageBack());
        addFilePart(context, files, "carImageLeft", request.getCarImageLeft());
        addFilePart(context, files, "carImageRight", request.getCarImageRight());

        return new AddCarFormData(fields, files);
    }

    private static RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value == null ? "" : value);
    }

    /**
     * Thêm file vào danh sách Multipart. Nếu Uri là String (URL cũ), nó sẽ được bỏ qua
     * vì chúng ta chỉ xử lý file mới (Uri Android).
     */
    private static void addFilePart(Context context, List<MultipartBody.Part> files, String name, @Nullable Uri uri) {
        // Chỉ xử lý nếu đây là một Uri Android (file mới được chọn)
        if (uri == null || uri.getScheme() == null || !uri.getScheme().equals("content")) return;

        try {
            // 1. Lấy MIME Type và File Name
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "application/octet-stream";
            String fileName = getFileName(context, uri);

            // 2. Đọc toàn bộ dữ liệu từ Uri vào mảng byte
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            if (inputStream == null) return;

            // Đọc tất cả byte từ InputStream (Tốt nhất cho các file kích thước vừa và nhỏ)
            byte[] bytes;
            try {
                bytes = readBytes(inputStream);
            } finally {
                inputStream.close();
            }

            // 3. Tạo RequestBody bằng mảng byte (Không cần InputStreamRequestBody)
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);

            // 4. Tạo MultipartBody.Part
            MultipartBody.Part part = MultipartBody.Part.createFormData(name, fileName, requestFile);
            files.add(part);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm Utility để đọc toàn bộ InputStream vào mảng byte
    private static byte[] readBytes(InputStream inputStream) throws IOException {
        // Sử dụng ByteArrayOutputStream để đọc toàn bộ byte
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // Hàm lấy file name từ Uri
    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    // Hàm lấy kích thước file (Tùy chọn)
    private static long getFileSize(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }
}