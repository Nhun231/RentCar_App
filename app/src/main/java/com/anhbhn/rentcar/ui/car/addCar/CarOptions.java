package com.anhbhn.rentcar.ui.car.addCar;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CarOptions {

    // Phải khớp với tên mảng chính trong database.json
    @SerializedName("Brand_and_Model")
    public List<BrandModelData> brandModelDataList;
    @SerializedName("Address_list")
    public List<AddressItem> addressList;

    // Constructor mặc định cần thiết cho Gson
    public CarOptions() {}

    public List<BrandModelData> getBrandModelDataList() {
        return brandModelDataList;
    }
    public List<AddressItem> getAddressList() { return addressList; }
}