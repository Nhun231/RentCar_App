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
import com.anhbhn.rentcar.databinding.ItemMyCarBinding; // Đảm bảo View Binding được bật
import com.anhbhn.rentcar.ui.car.carDetail.CarDetailActivity;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

// Đã loại bỏ import TabLayoutMediator và CarImageSliderAdapter

public class MyCarsAdapter extends RecyclerView.Adapter<MyCarsAdapter.CarViewHolder> {

    private final List<CarThumbnailResponse> carList;
    private final Context context;

    public MyCarsAdapter(Context context, List<CarThumbnailResponse> carList) {
        this.context = context;
        this.carList = carList != null ? carList : new ArrayList<>();
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyCarBinding binding = ItemMyCarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CarViewHolder(binding);
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

        // --- LOGIC CHUYỂN ẢNH BẰNG NÚT BẤM ---
        private int currentImageIndex = 0;
        private List<String> imageUrls = new ArrayList<>();
        // ------------------------------------

        public CarViewHolder(ItemMyCarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // THIẾT LẬP LISTENER CHO NÚT BẤM (Prev/Next)
            binding.btnNextImage.setOnClickListener(v -> changeImage(1));
            binding.btnPrevImage.setOnClickListener(v -> changeImage(-1));
        }

        private void changeImage(int direction) {
            int newIndex = currentImageIndex + direction;
            int total = imageUrls.size();

            if (total == 0) return;

            // Xử lý vòng lặp (wrap around)
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
                        .into(binding.imgCarDisplay); // imgCarDisplay là ImageView hiển thị
            } else {
                binding.imgCarDisplay.setImageResource(R.drawable.ic_car_placeholder);
            }
        }

        private void updateImageCounter(int total) {
            // Cập nhật TextView: Vị trí hiện tại + 1 / Tổng số ảnh
            binding.tvImageCounter.setText(String.format("%d/%d", currentImageIndex + 1, total));
        }
        // ------------------------------------


        public void bind(CarThumbnailResponse car) {

            // --- 1. Basic Info Binding (Giữ nguyên) ---
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

            // --- 2. Image Slider Setup (MỚI) ---

            // 2a. Khởi tạo danh sách ảnh (4 URL)
            imageUrls = Arrays.asList(
                    car.getCarImageFront(),
                    car.getCarImageBack(),
                    car.getCarImageLeft(),
                    car.getCarImageRight()
            );
            currentImageIndex = 0; // Reset về ảnh đầu tiên khi binding

            // 2b. Tải ảnh đầu tiên và cập nhật số đếm
            loadImage(imageUrls.get(currentImageIndex));
            updateImageCounter(imageUrls.size());

            // --- 3. Click Listeners (Giữ nguyên) ---
            binding.btnViewMore.setOnClickListener(v -> {
                Toast.makeText(context, "Viewing details for: " + car.getModel(), Toast.LENGTH_SHORT).show();
            });

            binding.btnMenu.setOnClickListener(v -> {
                Toast.makeText(context, "Showing menu for: " + car.getModel(), Toast.LENGTH_SHORT).show();
            });
            binding.btnViewMore.setOnClickListener(v -> {
                // Tạo Intent và truyền ID xe
                Intent intent = new Intent(context, CarDetailActivity.class);
                // Đảm bảo CarThumbnailResponse có getId()
                intent.putExtra(CarDetailActivity.EXTRA_CAR_ID, car.getId());
                context.startActivity(intent);
            });
        }
    }
}