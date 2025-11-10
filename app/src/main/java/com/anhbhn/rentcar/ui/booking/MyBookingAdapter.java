package com.anhbhn.rentcar.ui.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.booking.BookingThumbnailResponse;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyBookingAdapter extends RecyclerView.Adapter<MyBookingAdapter.BookingViewHolder> {
    
    private List<BookingThumbnailResponse> bookingList;
    private OnBookingActionListener listener;
    
    public interface OnBookingActionListener {
        void onViewDetailsClick(BookingThumbnailResponse booking);
        void onActionClick(BookingThumbnailResponse booking, String action);
    }
    
    public MyBookingAdapter(List<BookingThumbnailResponse> bookingList, OnBookingActionListener listener) {
        this.bookingList = bookingList != null ? bookingList : new java.util.ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingThumbnailResponse booking = bookingList.get(position);
        holder.bind(booking);
    }
    
    @Override
    public int getItemCount() {
        return bookingList.size();
    }
    
    public void updateBookings(List<BookingThumbnailResponse> newBookings) {
        this.bookingList = newBookings != null ? newBookings : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }
    
    class BookingViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCarImage;
        private TextView tvCarName;
        private TextView tvPickUpTime;
        private TextView tvDropOffTime;
        private TextView tvNumberOfDays;
        private TextView tvBasePrice;
        private TextView tvTotalPrice;
        private TextView tvDeposit;
        private TextView tvBookingNumber;
        private TextView tvStatus;
        private Button btnViewDetails;
        private Button btnAction1;
        private Button btnAction2;
        private LinearLayout layoutActionButtons;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCarImage = itemView.findViewById(R.id.ivCarImage);
            tvCarName = itemView.findViewById(R.id.tvCarName);
            tvPickUpTime = itemView.findViewById(R.id.tvPickUpTime);
            tvDropOffTime = itemView.findViewById(R.id.tvDropOffTime);
            tvNumberOfDays = itemView.findViewById(R.id.tvNumberOfDays);
            tvBasePrice = itemView.findViewById(R.id.tvBasePrice);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvDeposit = itemView.findViewById(R.id.tvDeposit);
            tvBookingNumber = itemView.findViewById(R.id.tvBookingNumber);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnAction1 = itemView.findViewById(R.id.btnAction1);
            btnAction2 = itemView.findViewById(R.id.btnAction2);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
        }
        
        public void bind(BookingThumbnailResponse booking) {
            // Car name: Brand Model Year
            String brand = booking.getBrand() != null ? booking.getBrand().toUpperCase() : "";
            String model = booking.getModel() != null ? booking.getModel().toUpperCase() : "";
            String carName = brand + " " + model + " " + booking.getProductionYear();
            tvCarName.setText(carName.trim());
            
            // Pick-up time
            if (booking.getPickUpTime() != null && !booking.getPickUpTime().isEmpty()) {
                tvPickUpTime.setText("From: " + formatDateTime(booking.getPickUpTime()));
            } else {
                tvPickUpTime.setText("From: N/A");
            }
            
            // Drop-off time
            if (booking.getDropOffTime() != null && !booking.getDropOffTime().isEmpty()) {
                tvDropOffTime.setText("To: " + formatDateTime(booking.getDropOffTime()));
            } else {
                tvDropOffTime.setText("To: N/A");
            }
            
            // Number of days
            int numberOfDays = booking.getNumberOfDay() > 0 ? booking.getNumberOfDay() : 1;
            tvNumberOfDays.setText("Number of days: " + numberOfDays);
            
            // Base price
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String basePriceText = "Base Price: " + formatter.format(booking.getBasePrice()) + " VND/day";
            tvBasePrice.setText(basePriceText);
            
            // Total price
            String totalPriceText = "Total: " + formatter.format(booking.getTotalPrice()) + " VND";
            tvTotalPrice.setText(totalPriceText);
            
            // Deposit
            String depositText = "Deposit: " + formatter.format(booking.getDeposit()) + " VND";
            tvDeposit.setText(depositText);
            
            // Booking number
            if (booking.getBookingNumber() != null && !booking.getBookingNumber().isEmpty()) {
                tvBookingNumber.setText("Booking No.: " + booking.getBookingNumber());
            } else {
                tvBookingNumber.setText("Booking No.: N/A");
            }
            
            // Status with color
            if (booking.getStatus() != null && !booking.getStatus().isEmpty()) {
                tvStatus.setText(booking.getStatus());
                setStatusColor(booking.getStatus());
            } else {
                tvStatus.setText("UNKNOWN");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray, null));
            }
            
            // Load car image
            String imageUrl = booking.getCarImageFrontUrl() != null ? booking.getCarImageFrontUrl() :
                             (booking.getCarImageRightUrl() != null ? booking.getCarImageRightUrl() :
                             (booking.getCarImageLeftUrl() != null ? booking.getCarImageLeftUrl() : booking.getCarImageBackUrl()));
            
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
            
            // Setup action buttons based on status
            setupActionButtons(booking);
            
            // View Details button
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(booking);
                }
            });
        }
        
        private void setStatusColor(String status) {
            int textColor;
            int bgColor;
            
            switch (status) {
                case "CONFIRMED":
                    textColor = 0xFF05ce80; // Green
                    bgColor = 0x1A05ce80; // Light green with alpha
                    break;
                case "IN_PROGRESS":
                case "WAITING_CONFIRMED":
                case "WAITING_PAYMENT":
                case "WAITING_CONFIRMED_RETURN_CAR":
                    textColor = 0xFFFF9800; // Orange
                    bgColor = 0x1AFF9800; // Light orange with alpha
                    break;
                case "PENDING_DEPOSIT":
                case "PENDING_PAYMENT":
                    textColor = 0xFFD32F2F; // Red
                    bgColor = 0x1AD32F2F; // Light red with alpha
                    break;
                case "COMPLETED":
                    textColor = 0xFF2196F3; // Blue
                    bgColor = 0x1A2196F3; // Light blue with alpha
                    break;
                case "CANCELLED":
                    textColor = 0xFF757575; // Gray
                    bgColor = 0x1A757575; // Light gray with alpha
                    break;
                default:
                    textColor = 0xFF000000; // Black
                    bgColor = 0xFFFFFFFF; // White
                    break;
            }
            
            tvStatus.setTextColor(textColor);
            tvStatus.setBackgroundColor(bgColor);
            tvStatus.setPadding(8, 4, 8, 4);
        }
        
        private void setupActionButtons(BookingThumbnailResponse booking) {
            // Reset buttons
            btnAction1.setVisibility(View.GONE);
            btnAction2.setVisibility(View.GONE);
            
            if (booking.getStatus() == null) {
                return;
            }
            
            String status = booking.getStatus();
            
            // CONFIRMED: Show Pick-up and Cancel buttons
            if ("CONFIRMED".equals(status)) {
                btnAction1.setVisibility(View.VISIBLE);
                btnAction1.setText("Pick-up");
                btnAction1.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(android.R.color.darker_gray, null));
                btnAction1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(booking, "PICK_UP");
                    }
                });
                
                btnAction2.setVisibility(View.VISIBLE);
                btnAction2.setText("Cancel");
                btnAction2.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(android.R.color.holo_red_dark, null));
                btnAction2.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(booking, "CANCEL");
                    }
                });
            }
            // WAITING_CONFIRMED: Show Cancel button
            else if ("WAITING_CONFIRMED".equals(status)) {
                btnAction1.setVisibility(View.VISIBLE);
                btnAction1.setText("Cancel");
                btnAction1.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(android.R.color.holo_red_dark, null));
                btnAction1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(booking, "CANCEL");
                    }
                });
            }
            // PENDING_DEPOSIT with WALLET: Show Pay Deposit and Cancel buttons
            else if ("PENDING_DEPOSIT".equals(status) && "WALLET".equals(booking.getPaymentType())) {
                btnAction1.setVisibility(View.VISIBLE);
                btnAction1.setText("Pay Deposit");
                btnAction1.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(android.R.color.darker_gray, null));
                btnAction1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(booking, "PAY_DEPOSIT");
                    }
                });
                
                btnAction2.setVisibility(View.VISIBLE);
                btnAction2.setText("Cancel");
                btnAction2.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(android.R.color.holo_red_dark, null));
                btnAction2.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(booking, "CANCEL");
                    }
                });
            }
            // PENDING_DEPOSIT without WALLET: Show Cancel button
            else if ("PENDING_DEPOSIT".equals(status)) {
                btnAction1.setVisibility(View.VISIBLE);
                btnAction1.setText("Cancel");
                btnAction1.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(android.R.color.holo_red_dark, null));
                btnAction1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(booking, "CANCEL");
                    }
                });
            }
            // IN_PROGRESS: Show Return Car button
            else if ("IN_PROGRESS".equals(status)) {
                btnAction1.setVisibility(View.VISIBLE);
                btnAction1.setText("Return Car");
                btnAction1.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(android.R.color.holo_green_dark, null));
                btnAction1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(booking, "RETURN_CAR");
                    }
                });
            }
            // PENDING_PAYMENT: Show Complete Payment button
            else if ("PENDING_PAYMENT".equals(status)) {
                btnAction1.setVisibility(View.VISIBLE);
                btnAction1.setText("Complete Payment");
                btnAction1.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(android.R.color.holo_green_dark, null));
                btnAction1.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(booking, "COMPLETE_PAYMENT");
                    }
                });
            }
        }
        
        private String formatDateTime(String dateTimeStr) {
            try {
                // Try ISO format first
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                Date date = apiFormat.parse(dateTimeStr);
                if (date != null) {
                    return displayFormat.format(date);
                }
            } catch (ParseException e) {
                // Try other formats if needed
                try {
                    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                    Date date = apiFormat.parse(dateTimeStr);
                    if (date != null) {
                        return displayFormat.format(date);
                    }
                } catch (ParseException e2) {
                    // Ignore parsing errors
                }
            }
            return dateTimeStr;
        }
    }
}

