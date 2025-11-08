package com.anhbhn.rentcar.ui.booking.myRentals;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.booking.BookingThumbnailResponse;
import com.anhbhn.rentcar.databinding.ItemRentalCardBinding;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookingThumbnailAdapter extends RecyclerView.Adapter<BookingThumbnailAdapter.BookingViewHolder> {

    private final List<BookingThumbnailResponse> bookingList;
    private final OnItemClickListener listener;
    private final Context context;

    public interface OnItemClickListener {
        void onViewDetailsClick(String bookingNumber);
        void onApproveClick(String bookingNumber);
        void onRejectClick(String bookingNumber);
    }

    public BookingThumbnailAdapter(Context context, List<BookingThumbnailResponse> bookingList, OnItemClickListener listener) {
        this.context = context;
        this.bookingList = bookingList != null ? bookingList : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * ✅ Bổ sung: Cập nhật danh sách và thông báo thay đổi.
     */
    public void updateList(List<BookingThumbnailResponse> newList) {
        this.bookingList.clear();
        if (newList != null) {
            this.bookingList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRentalCardBinding binding = ItemRentalCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding, context, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingThumbnailResponse item = bookingList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return bookingList.size(); // ✅ Sửa: Trả về kích thước danh sách thực tế
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemRentalCardBinding binding;
        private final Context context;
        private final OnItemClickListener listener;

        private int currentImageIndex = 0;
        private List<String> imageUrls = new ArrayList<>();

        public BookingViewHolder(ItemRentalCardBinding binding, Context context, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = context;
            this.listener = listener;

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

        public void bind(BookingThumbnailResponse item) {

            // --- 1. DỮ LIỆU TEXT ---
            binding.tvCarName.setText(String.format("%s %s (%d)", item.getBrand(), item.getModel(), item.getProductionYear()));

            // Cần format thời gian (Tạm thời dùng chuỗi thô)
            binding.tvRentalPeriod.setText(String.format("%s - %s", item.getPickUpTime(), item.getDropOffTime()));

            // Các trường giá và trạng thái
            binding.tvNumberOfDays.setText(context.getString(R.string.label_number_of_days_format, item.getNumberOfDay()));
            binding.tvBasePrice.setText(context.getString(R.string.label_base_price_format, String.valueOf(item.getBasePrice())));
            binding.tvDeposit.setText(context.getString(R.string.label_deposit_format, String.valueOf(item.getDeposit())));
            binding.tvBookingStatus.setText(context.getString(R.string.label_booking_status_format, item.getStatus()));

            // --- 2. Image Slider Setup ---
            imageUrls = Arrays.asList(
                    item.getCarImageFrontUrl(),
                    item.getCarImageBackUrl(),
                    item.getCarImageLeftUrl(),
                    item.getCarImageRightUrl()
            );
            currentImageIndex = 0;
            loadImage(imageUrls.get(currentImageIndex));
            updateImageCounter(imageUrls.size());

            // --- 3. Click Listeners (Hành động Booking) ---
            binding.btnActionDetails.setOnClickListener(v -> listener.onViewDetailsClick(item.getBookingNumber()));

            // ✅ LOGIC HIỂN THỊ NÚT HÀNH ĐỘNG
            if ("WAITING_CONFIRMED".equals(item.getStatus()) || "WAITING_CONFIRMED_RETURN_CAR".equals(item.getStatus())) {
                binding.btnActionApprove.setVisibility(View.VISIBLE);
                binding.btnActionReject.setVisibility(View.VISIBLE);

                binding.btnActionApprove.setOnClickListener(v -> listener.onApproveClick(item.getBookingNumber()));
                binding.btnActionReject.setOnClickListener(v -> listener.onRejectClick(item.getBookingNumber()));
            } else {
                // Ẩn các nút hành động nếu trạng thái không phải chờ xác nhận
                binding.btnActionApprove.setVisibility(View.GONE);
                binding.btnActionReject.setVisibility(View.GONE);
            }
        }
    }
}