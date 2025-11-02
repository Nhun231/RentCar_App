package com.anhbhn.rentcar.ui.car.addCar;

import com.google.gson.annotations.SerializedName;

public class AddressItem {

    @SerializedName("City_Province")
    public String cityProvince;

    @SerializedName("Disctrict")
    public String district; // Lỗi chính tả trong JSON của bạn (Disctrict)

    @SerializedName("Ward")
    public String ward;

    public String getCityProvince() { return cityProvince; }
    public String getDistrict() { return district; }
    public String getWard() { return ward; }
}