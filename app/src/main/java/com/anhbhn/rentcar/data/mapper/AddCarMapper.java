package com.anhbhn.rentcar.data.mapper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.Nullable;

import com.anhbhn.rentcar.data.dto.helper.AddCarFormData;
import com.anhbhn.rentcar.data.dto.request.car.AddCarRequest;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AddCarMapper {

    public static AddCarFormData toFormData(Context context, AddCarRequest request) {
        Map<String, RequestBody> fields = new HashMap<>();

        // --- Các trường text ---
        fields.put("licensePlate", createPartFromString(request.getLicensePlate()));
        fields.put("brand", createPartFromString(request.getBrand()));
        fields.put("model", createPartFromString(request.getModel()));
        fields.put("color", createPartFromString(request.getColor()));
        fields.put("numberOfSeats", createPartFromString(String.valueOf(request.getNumberOfSeats())));
        fields.put("productionYear", createPartFromString(String.valueOf(request.getProductionYear())));
        fields.put("mileage", createPartFromString(String.valueOf(request.getMileage())));
        fields.put("fuelConsumption", createPartFromString(String.valueOf(request.getFuelConsumption())));
        fields.put("basePrice", createPartFromString(String.valueOf(request.getBasePrice())));
        fields.put("deposit", createPartFromString(String.valueOf(request.getDeposit())));
        fields.put("address", createPartFromString(request.getAddress()));
        fields.put("description", createPartFromString(request.getDescription()));
        fields.put("additionalFunction", createPartFromString(request.getAdditionalFunction()));
        fields.put("termOfUse", createPartFromString(request.getTermOfUse()));
        fields.put("isAutomatic", createPartFromString(String.valueOf(request.isAutomatic())));
        fields.put("isGasoline", createPartFromString(String.valueOf(request.isGasoline())));

        // --- Các file upload ---
        List<MultipartBody.Part> files = new ArrayList<>();
        addFilePart(context, files, "registrationPaper", request.getRegistrationPaper());
        addFilePart(context, files, "certificateOfInspection", request.getCertificateOfInspection());
        addFilePart(context, files, "insurance", request.getInsurance());
        addFilePart(context, files, "carImageFront", request.getCarImageFront());
        addFilePart(context, files, "carImageBack", request.getCarImageBack());
        addFilePart(context, files, "carImageLeft", request.getCarImageLeft());
        addFilePart(context, files, "carImageRight", request.getCarImageRight());

        return new AddCarFormData(fields, files);
    }

    private static RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value == null ? "" : value);
    }

    private static void addFilePart(Context context, List<MultipartBody.Part> files, String name, @Nullable Uri uri) {
        if (uri == null) return;

        try {
            // Lấy mime type thật sự
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "application/octet-stream";

            // Lấy file name
            String fileName = getFileName(context, uri);

            // Chuyển Uri thành RequestBody
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);

            MultipartBody.Part part = MultipartBody.Part.createFormData(name, fileName, requestFile);
            files.add(part);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
