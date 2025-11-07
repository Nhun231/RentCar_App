package com.anhbhn.rentcar.data.repository.car;

import android.content.Context;

import com.anhbhn.rentcar.data.dto.helper.AddCarFormData;
import com.anhbhn.rentcar.data.dto.request.car.AddCarRequest;
import com.anhbhn.rentcar.data.dto.request.car.EditCarRequest;
import com.anhbhn.rentcar.data.dto.response.car.CarResponse;
import com.anhbhn.rentcar.data.dto.response.car.MyCarsPageResponse;
import com.anhbhn.rentcar.data.mapper.AddCarMapper;
import com.anhbhn.rentcar.data.mapper.EditCarMapper;
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
    public Call<MyCarsPageResponse> getMyCars(int page, int size, String sort) {
        // Gán giá trị mặc định cho sort nếu null hoặc rỗng
        if (sort == null || sort.isEmpty()) {
            sort = "productionYear,DESC";
        }

        // Gọi phương thức API Service tương ứng
        return apiService.getMyCars(page, size, sort);
    }
    public Call<CarResponse> getCarDetailsForOwner(String carId) {
        return apiService.getCarDetailsForOwner(carId);
    }
    public Call<CarResponse> editCar(String carId, EditCarRequest request) {
        AddCarFormData formData = EditCarMapper.toFormData(context, request);

        return apiService.editCar(
                carId,
                formData.getFields(),
                formData.getFiles()
        );
    }
}
