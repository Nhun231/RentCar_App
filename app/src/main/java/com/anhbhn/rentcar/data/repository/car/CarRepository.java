package com.anhbhn.rentcar.data.repository.car;

import android.content.Context;

import com.anhbhn.rentcar.data.dto.helper.AddCarFormData;
import com.anhbhn.rentcar.data.dto.request.car.AddCarRequest;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.mapper.AddCarMapper;
import com.anhbhn.rentcar.data.remote.ApiClient;
import com.anhbhn.rentcar.data.remote.ApiService;

import retrofit2.Call;

public class CarRepository {
    private final ApiService apiService;
    private final Context context;

    public CarRepository(Context context) {
        this.context = context;
        apiService = ApiClient.getClient(context).create(ApiService.class);
    }
    public Call<CarResponse> addCar(AddCarRequest request) {
        AddCarFormData formData = AddCarMapper.toFormData(context, request);
        return apiService.addCar(formData.getFields(), formData.getFiles());
    }
}
