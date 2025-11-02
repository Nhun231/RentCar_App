package com.anhbhn.rentcar.ui.car.addCar;

import com.google.gson.annotations.SerializedName;

public class BrandModelData {

    // Đảm bảo tên biến khớp với KEY trong file JSON
    @SerializedName("Brand")
    public String brand;

    @SerializedName("Model")
    public String model;

    // Constructor mặc định cần thiết cho Gson
    public BrandModelData() {}

    // Nếu bạn muốn dùng getter/setter, hãy thêm vào đây
    public String getBrand() {
        return brand;
    }
    public String getModel() {
        return model;
    }
}
