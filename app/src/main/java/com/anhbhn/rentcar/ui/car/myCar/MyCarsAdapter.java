package com.anhbhn.rentcar.ui.car.myCar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.anhbhn.rentcar.databinding.ItemMyCarBinding;
import com.anhbhn.rentcar.ui.car.carDetail.CarDetailActivity;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class MyCarsAdapter extends RecyclerView.Adapter<MyCarsAdapter.CarViewHolder> {

    // 1. INTERFACE: Giao tiếp ngược với Fragment/Activity
    public interface OnCarActionListener {
        void onCarAction(String carId, String actionType);
    }

    private final List<CarThumbnailResponse> carList;
    private final Context context;
    private final OnCarActionListener listener; // ✅ Listener đã được khai báo

    public MyCarsAdapter(Context context, List<CarThumbnailResponse> carList, OnCarActionListener listener) {
        this.context = context;
        this.carList = carList != null ? carList : new ArrayList<>();
        this.listener = listener; // Gán Listener
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyCarBinding binding = ItemMyCarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        // ✅ FIX: TRUYỀN CONTEXT VÀ LISTENER VÀO VIEWHOLDER
        return new CarViewHolder(binding, context, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        CarThumbnailResponse car = carList.get(position);
        holder.bind(car);
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    /**
     * Updates the RecyclerView data set and notifies the adapter.
     */
    public void updateData(List<CarThumbnailResponse> newCarList) {
        carList.clear();
        if (newCarList != null) {
            carList.addAll(newCarList);
        }
        notifyDataSetChanged();
    }


    public class CarViewHolder extends RecyclerView.ViewHolder {
        private final ItemMyCarBinding binding;
        private final Context context;
        private final OnCarActionListener listener; // ✅ Listener được lưu

        private int currentImageIndex = 0;
        private List<String> imageUrls = new ArrayList<>();

        // ✅ FIX: CẬP NHẬT CONSTRUCTOR VIEWHOLDER
        public CarViewHolder(ItemMyCarBinding binding, Context context, OnCarActionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = context;
            this.listener = listener; // Lưu Listener

            // THIẾT LẬP LISTENER CHO NÚT BẤM (Slider)
            binding.btnNextImage.setOnClickListener(v -> changeImage(1));
            binding.btnPrevImage.setOnClickListener(v -> changeImage(-1));
        }

        private void changeImage(int direction) {
            int newIndex = currentImageIndex + direction;
            int total = imageUrls.size();

            if (total == 0) return;

            if (newIndex >= total) {
                newIndex = 0;
            } else if (newIndex < 0) {
                newIndex = total - 1;
            }

            currentImageIndex = newIndex;
            loadImage(imageUrls.get(currentImageIndex));
            updateImageCounter(total);
        }

        private void loadImage(String url) {
            if (url != null && !url.isEmpty()) {
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .into(binding.imgCarDisplay);
            } else {
                binding.imgCarDisplay.setImageResource(R.drawable.ic_car_placeholder);
            }
        }

        private void updateImageCounter(int total) {
            binding.tvImageCounter.setText(String.format("%d/%d", currentImageIndex + 1, total));
        }


        public void bind(CarThumbnailResponse car) {

            // --- 1. Basic Info Binding ---
            binding.tvCarName.setText(String.format("%s %s", car.getBrand(), car.getModel()));

            String fullAddress = car.getAddress() != null ? car.getAddress() : "";
            String shortAddress = fullAddress.split(",")[0].trim();

            String details = context.getString(
                    R.string.car_item_details_format,
                    car.getProductionYear(),
                    car.getStatus(),
                    shortAddress
            );
            binding.tvCarDetails.setText(details);

            binding.tvPrice.setText(
                    context.getString(R.string.car_item_price_format, car.getBasePrice())
            );

            // --- 2. Image Slider Setup ---
            imageUrls = Arrays.asList(
                    car.getCarImageFront(),
                    car.getCarImageBack(),
                    car.getCarImageLeft(),
                    car.getCarImageRight()
            );
            currentImageIndex = 0;
            loadImage(imageUrls.get(currentImageIndex));
            updateImageCounter(imageUrls.size());

            // --- 3. Click Listeners (SỬ DỤNG INTERFACE ĐỂ ĐIỀU HƯỚNG) ---

            // 🛑 FIX LỖI: Loại bỏ Listener StartActivity trực tiếp và gọi qua Interface
            binding.btnViewMore.setOnClickListener(v -> {
                // Điều hướng qua Fragment/Activity chính
                listener.onCarAction(car.getId(), "VIEW_DETAILS");
            });

            // Gán Listener cho nút Menu
            binding.btnMenu.setOnClickListener(v -> {
                listener.onCarAction(car.getId(), "SHOW_MENU");
            });

            // 🛑 LƯU Ý: Nút ViewMore có 2 Listener trong mã gốc (đã xóa 1)
            // Nếu bạn muốn giữ Toast tạm thời cho menu, chỉ để nó trong Activity/Fragment.
            // Ở đây, tôi giữ lại logic duy nhất là gọi Interface.
        }
    }
}