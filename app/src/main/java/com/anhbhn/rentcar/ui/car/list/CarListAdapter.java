package com.anhbhn.rentcar.ui.car.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.car.CarThumbnailResponse;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CarListAdapter extends RecyclerView.Adapter<CarListAdapter.CarViewHolder> {
    
    private List<CarThumbnailResponse> carList;
    private OnCarItemClickListener listener;
    
    public interface OnCarItemClickListener {
        void onCarClick(CarThumbnailResponse car);
    }
    
    public CarListAdapter(List<CarThumbnailResponse> carList, OnCarItemClickListener listener) {
        this.carList = carList != null ? carList : new java.util.ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
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
    
    class CarViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCarImage;
        private ImageView ivQuickBooking;
        private ImageView ivFavorite;
        private TextView tvDiscount;
        private TextView tvNoDeposit;
        private TextView tvCarName;
        private TextView tvTransmission;
        private TextView tvSeats;
        private TextView tvFuelType;
        private TextView tvLocation;
        private TextView tvRating;
        private TextView tvRides;
        private TextView tvOriginalPrice;
        private TextView tvPrice;
        private TextView tvHourlyPrice;
        
        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCarImage = itemView.findViewById(R.id.ivCarImage);
            ivQuickBooking = itemView.findViewById(R.id.ivQuickBooking);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvNoDeposit = itemView.findViewById(R.id.tvNoDeposit);
            tvCarName = itemView.findViewById(R.id.tvCarName);
            tvTransmission = itemView.findViewById(R.id.tvTransmission);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvFuelType = itemView.findViewById(R.id.tvFuelType);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvRides = itemView.findViewById(R.id.tvRides);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvHourlyPrice = itemView.findViewById(R.id.tvHourlyPrice);
        }
        
        public void bind(CarThumbnailResponse car) {
            // Car name: Brand Model Year
            String brand = car.brand != null ? car.brand.toUpperCase() : "";
            String model = car.model != null ? car.model.toUpperCase() : "";
            String carName = brand + " " + model + " " + car.productionYear;
            tvCarName.setText(carName.trim());
            
            // Location
            if (car.address != null && !car.address.isEmpty()) {
                tvLocation.setText(car.address);
            } else {
                tvLocation.setText("Location not available");
            }
            
            // Price - Format as VND (e.g., "690K /day")
            NumberFormat vndFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            long price = car.basePrice;
            String priceText;
            if (price >= 1000000) {
                // Format as millions (e.g., 1.5M)
                priceText = String.format(Locale.getDefault(), "%.1fM /day", price / 1000000.0);
            } else if (price >= 1000) {
                // Format as thousands (e.g., 690K)
                priceText = String.format(Locale.getDefault(), "%.0fK /day", price / 1000.0);
            } else {
                priceText = vndFormat.format(price) + " /day";
            }
            tvPrice.setText(priceText);
            
            // Hide original price for now (can be shown if discount exists)
            tvOriginalPrice.setVisibility(View.GONE);
            
            // Rating
            if (car.averageRatingByCar > 0) {
                tvRating.setText(String.format(Locale.getDefault(), "%.1f", car.averageRatingByCar));
            } else {
                tvRating.setText("0.0"); // No ratings yet
            }
            
            // Number of rides
            tvRides.setText(String.valueOf(car.noOfRides) + " rides");
            
            // Transmission - Use actual value from API (default to false/Manual if null)
            if (car.isAutomatic == null) {
                Log.w("CarAdapter", "Car " + car.id + " - isAutomatic is NULL! Check JSON deserialization.");
            }
            boolean isAutomatic = car.isAutomatic != null ? car.isAutomatic : false;
            tvTransmission.setText(isAutomatic ? "Automatic" : "Manual");
            Log.d("CarAdapter", "Car " + car.id + " - isAutomatic: " + car.isAutomatic + ", showing: " + (isAutomatic ? "Automatic" : "Manual"));
            
            // Number of seats - Always display, use actual value if available
            int seats = (car.numberOfSeats != null && car.numberOfSeats > 0) ? car.numberOfSeats : 5; // Default to 5
            tvSeats.setText(seats + " seats");
            Log.d("CarAdapter", "Car " + car.id + " - numberOfSeats: " + car.numberOfSeats + ", showing: " + seats);
            
            // Fuel type - Use actual value from API (default to false/Diesel if null)
            if (car.isGasoline == null) {
                Log.w("CarAdapter", "Car " + car.id + " - isGasoline is NULL! Check JSON deserialization.");
            }
            boolean isGasoline = car.isGasoline != null ? car.isGasoline : false;
            tvFuelType.setText(isGasoline ? "Gasoline" : "Diesel");
            Log.d("CarAdapter", "Car " + car.id + " - isGasoline: " + car.isGasoline + ", showing: " + (isGasoline ? "Gasoline" : "Diesel"));
            
            // Hide optional badges for now
            ivQuickBooking.setVisibility(View.GONE);
            tvDiscount.setVisibility(View.GONE);
            tvNoDeposit.setVisibility(View.GONE);
            tvHourlyPrice.setVisibility(View.GONE);
            
            // Load car image with rounded corners
            String imageUrl = car.carImageFront != null ? car.carImageFront : 
                             (car.carImageRight != null ? car.carImageRight : 
                             (car.carImageLeft != null ? car.carImageLeft : car.carImageBack));
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .into(ivCarImage);
            } else {
                ivCarImage.setImageResource(android.R.drawable.ic_menu_report_image);
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCarClick(car);
                }
            });
        }
    }
}

