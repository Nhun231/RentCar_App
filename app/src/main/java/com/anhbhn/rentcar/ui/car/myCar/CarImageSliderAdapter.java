package com.anhbhn.rentcar.ui.car.myCar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R; // Đảm bảo import đúng R package của bạn
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter cho ViewPager2 để hiển thị 4 ảnh của một chiếc xe (Front, Back, Left, Right).
 */
public class CarImageSliderAdapter extends RecyclerView.Adapter<CarImageSliderAdapter.ImageViewHolder> {

    private final Context context;
    // Danh sách chứa 4 URL ảnh: [Front, Back, Left, Right]
    private final List<String> imageUrls;

    public CarImageSliderAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_slider_image.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_slider_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Tải ảnh bằng Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_car_placeholder) // Icon hiển thị trong khi tải
                    .error(R.drawable.ic_car_placeholder)      // Icon hiển thị nếu lỗi
                    .into(holder.imageView);
        } else {
            // Nếu URL rỗng/null, hiển thị placeholder
            holder.imageView.setImageResource(R.drawable.ic_car_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        // Luôn trả về kích thước của danh sách URL (thường là 4)
        return imageUrls.size();
    }

    /**
     * ViewHolder chứa ImageView cho mỗi slide ảnh.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // slider_image là ID của ImageView trong item_slider_image.xml
            imageView = itemView.findViewById(R.id.slider_image);
        }
    }
}