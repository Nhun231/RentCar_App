package com.anhbhn.rentcar.ui.car.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.car.CarDetailResponse;
import com.anhbhn.rentcar.data.repository.car.CarRepository;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPagerCarImages;
    private ViewPager2 viewPagerTabs;
    private TabLayout tabLayout;
    private TextView tvCarName;
    private TextView tvRating;
    private TextView tvNoOfRides;
    private TextView tvPrice;
    private TextView tvLocation;
    private TextView tvStatus;
    private TextView tvPickUpTime;
    private TextView tvDropOffTime;
    private LinearLayout layoutAvailability;
    private TextView tvAvailabilityMessage;
    private ImageView ivAvailabilityIcon;
    private Button btnRentNow;
    private ImageButton btnPreviousImage;
    private ImageButton btnNextImage;
    private LinearLayout layoutImageIndicators;

    private CarDetailResponse carDetail;
    private String carId;
    private String pickUpTime;
    private String dropOffTime;
    private List<String> carImageUrls = new ArrayList<>();
    private int currentImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail_customer);

        loadIntentData();
        initializeViews();
        setupTabs();
        loadCarDetail();
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            carId = intent.getStringExtra("carId");
            pickUpTime = intent.getStringExtra("pickUpTime");
            dropOffTime = intent.getStringExtra("dropOffTime");
        }
    }

    private void initializeViews() {
        viewPagerCarImages = findViewById(R.id.viewPagerCarImages);
        viewPagerTabs = findViewById(R.id.viewPagerTabs);
        tabLayout = findViewById(R.id.tabLayout);
        tvCarName = findViewById(R.id.tvCarName);
        tvRating = findViewById(R.id.tvRating);
        tvNoOfRides = findViewById(R.id.tvNoOfRides);
        tvPrice = findViewById(R.id.tvPrice);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        tvPickUpTime = findViewById(R.id.tvPickUpTime);
        tvDropOffTime = findViewById(R.id.tvDropOffTime);
        layoutAvailability = findViewById(R.id.layoutAvailability);
        tvAvailabilityMessage = findViewById(R.id.tvAvailabilityMessage);
        ivAvailabilityIcon = findViewById(R.id.ivAvailabilityIcon);
        btnRentNow = findViewById(R.id.btnRentNow);
        btnPreviousImage = findViewById(R.id.btnPreviousImage);
        btnNextImage = findViewById(R.id.btnNextImage);
        layoutImageIndicators = findViewById(R.id.layoutImageIndicators);

        // Format and display pick-up/drop-off times
        if (pickUpTime != null && dropOffTime != null) {
            tvPickUpTime.setText(formatDateTime(pickUpTime));
            tvDropOffTime.setText(formatDateTime(dropOffTime));
        }

        btnRentNow.setOnClickListener(v -> {
            if (carDetail != null && carDetail.isAvailable != null && carDetail.isAvailable && "VERIFIED".equals(carDetail.status)) {
                // Navigate to booking information screen
                Intent intent = new Intent(this, com.anhbhn.rentcar.ui.booking.BookingInformationActivity.class);
                intent.putExtra("carId", carId);
                intent.putExtra("pickUpTime", pickUpTime);
                intent.putExtra("dropOffTime", dropOffTime);
                // Note: Backend constructs pickUpLocation from car entity, so we just pass a placeholder
                // Backend will override this with the full address from the car (City, District, Ward, House number)
                intent.putExtra("pickUpLocation", "");
                startActivity(intent);
            } else {
                Toasty.warning(this, "This car is not available for booking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = apiFormat.parse(dateTimeStr);
            if (date != null) {
                return displayFormat.format(date);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return dateTimeStr;
    }

    private void setupTabs() {
        CarDetailPagerAdapter adapter = new CarDetailPagerAdapter(this);
        viewPagerTabs.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPagerTabs, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("BASIC INFORMATION");
                    break;
                case 1:
                    tab.setText("DETAILS");
                    break;
                case 2:
                    tab.setText("TERM OF USE");
                    break;
                case 3:
                    tab.setText("FEEDBACK LIST");
                    break;
            }
        }).attach();
    }

    private void loadCarDetail() {
        if (carId == null || pickUpTime == null || dropOffTime == null) {
            Toasty.error(this, "Missing car information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CarRepository repository = new CarRepository(this);
        repository.getCarDetail(carId, pickUpTime, dropOffTime)
                .enqueue(new Callback<ApiResponse<CarDetailResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CarDetailResponse>> call, Response<ApiResponse<CarDetailResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<CarDetailResponse> apiResponse = response.body();
                            if (apiResponse.code == 1000 && apiResponse.data != null) {
                                carDetail = apiResponse.data;
                                displayCarDetail();
                                updateFragments();
                            } else {
                                Toasty.error(CarDetailActivity.this, apiResponse.message != null ? apiResponse.message : "Failed to load car details", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Try to parse error response for better error message
                            String errorMessage = "Failed to load car details";
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    try {
                                        com.google.gson.Gson gson = new com.google.gson.Gson();
                                        com.google.gson.JsonObject jsonError = gson.fromJson(errorBody, com.google.gson.JsonObject.class);
                                        if (jsonError.has("message")) {
                                            errorMessage = jsonError.get("message").getAsString();
                                        }
                                    } catch (Exception e) {
                                        // If JSON parsing fails, use error body as is
                                        if (!errorBody.isEmpty()) {
                                            errorMessage = errorBody;
                                        }
                                    }
                                }
                            } catch (java.io.IOException e) {
                                // Use default error message if we can't read error body
                            }
                            Toasty.error(CarDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CarDetailResponse>> call, Throwable t) {
                        Toasty.error(CarDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayCarDetail() {
        if (carDetail == null) return;

        // Car Name
        String carName = (carDetail.brand != null ? carDetail.brand.toUpperCase() : "") + " " +
                (carDetail.model != null ? carDetail.model.toUpperCase() : "");
        tvCarName.setText(carName.trim());

        // Rating
        if (carDetail.averageRatingByCar > 0) {
            tvRating.setText(String.format(Locale.getDefault(), "%.1f", carDetail.averageRatingByCar));
        } else {
            tvRating.setText("0.0");
        }

        // Number of rides
        tvNoOfRides.setText(String.valueOf(carDetail.noOfRides) + " rides");

        // Price
        long price = carDetail.basePrice;
        String priceText;
        if (price >= 1000000) {
            priceText = String.format(Locale.getDefault(), "%.1fM /day", price / 1000000.0);
        } else if (price >= 1000) {
            priceText = String.format(Locale.getDefault(), "%.0fK /day", price / 1000.0);
        } else {
            priceText = NumberFormat.getNumberInstance(Locale.getDefault()).format(price) + " /day";
        }
        tvPrice.setText(priceText);

        // Location
        if (carDetail.address != null && !carDetail.address.isEmpty()) {
            tvLocation.setText(carDetail.address);
        } else {
            tvLocation.setText("Location not available");
        }

        // Status - Display car verification status
        if (carDetail.status != null) {
            String statusText = carDetail.status;
            int statusColor;
            int statusBgColor;

            switch (carDetail.status) {
                case "VERIFIED":
                    statusText = "VERIFIED";
                    statusColor = getResources().getColor(android.R.color.holo_green_dark, null);
                    statusBgColor = getResources().getColor(android.R.color.holo_green_light, null);
                    break;
                case "NOT_VERIFIED":
                    statusText = "NOT VERIFIED";
                    statusColor = getResources().getColor(android.R.color.holo_orange_dark, null);
                    statusBgColor = getResources().getColor(android.R.color.holo_orange_light, null);
                    break;
                case "STOPPED":
                    statusText = "STOPPED";
                    statusColor = getResources().getColor(android.R.color.darker_gray, null);
                    statusBgColor = getResources().getColor(android.R.color.darker_gray, null);
                    break;
                default:
                    statusColor = getResources().getColor(android.R.color.darker_gray, null);
                    statusBgColor = getResources().getColor(android.R.color.transparent, null);
            }

            tvStatus.setText(statusText);
            tvStatus.setTextColor(statusColor);
            tvStatus.setBackgroundColor(statusBgColor);
        }

        // Availability - Display availability for the selected time range
        if (carDetail.isAvailable != null && carDetail.isAvailable) {
            if (carDetail.isBooked != null && carDetail.isBooked) {
                // Car is available but already booked by current user
                layoutAvailability.setVisibility(View.VISIBLE);
                tvAvailabilityMessage.setText("You have already booked this car for this period");
                tvAvailabilityMessage.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));
                layoutAvailability.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light, null));
                ivAvailabilityIcon.setColorFilter(getResources().getColor(android.R.color.holo_blue_dark, null));
                btnRentNow.setEnabled(false);
                btnRentNow.setText("ALREADY BOOKED");
            } else if ("VERIFIED".equals(carDetail.status)) {
                // Car is available and verified
                layoutAvailability.setVisibility(View.VISIBLE);
                tvAvailabilityMessage.setText("This car is available to rent for the selected period");
                tvAvailabilityMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                layoutAvailability.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, null));
                ivAvailabilityIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_dark, null));
                btnRentNow.setEnabled(true);
                btnRentNow.setText("RENT NOW");
            } else {
                // Car is not verified
                layoutAvailability.setVisibility(View.VISIBLE);
                tvAvailabilityMessage.setText("This car is not verified yet");
                tvAvailabilityMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
                layoutAvailability.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light, null));
                ivAvailabilityIcon.setColorFilter(getResources().getColor(android.R.color.holo_orange_dark, null));
                btnRentNow.setEnabled(false);
                btnRentNow.setText("NOT AVAILABLE");
            }
        } else {
            // Car is not available for the selected time range
            layoutAvailability.setVisibility(View.VISIBLE);
            tvAvailabilityMessage.setText("This car is not available for the selected time period");
            tvAvailabilityMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            layoutAvailability.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light, null));
            ivAvailabilityIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark, null));
            btnRentNow.setEnabled(false);
            btnRentNow.setText("NOT AVAILABLE");
        }

        // Setup car images
        setupCarImages();
    }

    private void setupCarImages() {
        carImageUrls.clear();
        if (carDetail.carImageFront != null && !carDetail.carImageFront.isEmpty()) {
            carImageUrls.add(carDetail.carImageFront);
        }
        if (carDetail.carImageRight != null && !carDetail.carImageRight.isEmpty()) {
            carImageUrls.add(carDetail.carImageRight);
        }
        if (carDetail.carImageLeft != null && !carDetail.carImageLeft.isEmpty()) {
            carImageUrls.add(carDetail.carImageLeft);
        }
        if (carDetail.carImageBack != null && !carDetail.carImageBack.isEmpty()) {
            carImageUrls.add(carDetail.carImageBack);
        }

        if (carImageUrls.isEmpty()) {
            // Use placeholder
            carImageUrls.add("");
        }

        // Setup image carousel
        CarImagePagerAdapter imageAdapter = new CarImagePagerAdapter(carImageUrls);
        viewPagerCarImages.setAdapter(imageAdapter);

        // Setup image navigation and indicators
        setupImageNavigation();

        if (carImageUrls.size() > 1) {
            setupImageIndicators();
        } else {
            btnPreviousImage.setVisibility(View.GONE);
            btnNextImage.setVisibility(View.GONE);
            layoutImageIndicators.setVisibility(View.GONE);
        }
    }

    private void setupImageIndicators() {
        layoutImageIndicators.removeAllViews();
        for (int i = 0; i < carImageUrls.size(); i++) {
            ImageView indicator = new ImageView(this);
            indicator.setImageResource(android.R.drawable.star_big_off);
            indicator.setColorFilter(i == 0 ? getResources().getColor(android.R.color.holo_green_dark, null) :
                getResources().getColor(android.R.color.darker_gray, null));
            indicator.setPadding(8, 0, 8, 0);
            layoutImageIndicators.addView(indicator);
        }
        layoutImageIndicators.setVisibility(View.VISIBLE);
    }

    private void setupImageNavigation() {
        btnPreviousImage.setOnClickListener(v -> {
            int current = viewPagerCarImages.getCurrentItem();
            if (current > 0) {
                viewPagerCarImages.setCurrentItem(current - 1, true);
            }
        });

        btnNextImage.setOnClickListener(v -> {
            int current = viewPagerCarImages.getCurrentItem();
            if (current < carImageUrls.size() - 1) {
                viewPagerCarImages.setCurrentItem(current + 1, true);
            }
        });

        viewPagerCarImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentImageIndex = position;
                if (carImageUrls.size() > 1) {
                    updateImageIndicators(position);
                    btnPreviousImage.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
                    btnNextImage.setVisibility(position < carImageUrls.size() - 1 ? View.VISIBLE : View.GONE);
                }
            }
        });

        // Set initial button visibility
        if (carImageUrls.size() > 1) {
            btnPreviousImage.setVisibility(View.GONE);
            btnNextImage.setVisibility(carImageUrls.size() > 1 ? View.VISIBLE : View.GONE);
        }
    }

    private void updateImageIndicators(int position) {
        for (int i = 0; i < layoutImageIndicators.getChildCount(); i++) {
            ImageView indicator = (ImageView) layoutImageIndicators.getChildAt(i);
            indicator.setColorFilter(i == position ?
                getResources().getColor(android.R.color.holo_green_dark, null) :
                getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    private void updateFragments() {
        // Notify all fragments to update with car detail data
        if (carDetail == null) return;

        // Fragments will get data from getCarDetail() when they need it
        // Refresh the adapter to trigger fragment updates
        viewPagerTabs.post(() -> {
            // Force fragments to refresh by notifying them
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof CarDetailFragmentBase && fragment.isAdded()) {
                    ((CarDetailFragmentBase) fragment).updateCarDetail(carDetail);
                }
            }
        });
    }

    public CarDetailResponse getCarDetail() {
        return carDetail;
    }

    // Pager Adapter for Tabs
    private static class CarDetailPagerAdapter extends FragmentStateAdapter {
        public CarDetailPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new BasicInfoFragment();
                case 1:
                    return new DetailsFragment();
                case 2:
                    return new TermOfUseFragment();
                case 3:
                    return new FeedbackListFragment();
                default:
                    return new BasicInfoFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    // Image Pager Adapter
    private class CarImagePagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<CarImagePagerAdapter.ImageViewHolder> {
        private List<String> imageUrls;

        public CarImagePagerAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            try {
                if (position < 0 || position >= imageUrls.size()) {
                    holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                    return;
                }

                String imageUrl = imageUrls.get(position);
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    // Handle relative URLs by prepending base URL if needed
                    String fullUrl = imageUrl;
                    if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                        // If relative URL, prepend base URL (remove trailing slash from base, add leading slash to path)
                        String baseUrl = com.anhbhn.rentcar.data.remote.Config.BASE_URL;
                        if (baseUrl.endsWith("/")) {
                            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                        }
                        if (!imageUrl.startsWith("/")) {
                            imageUrl = "/" + imageUrl;
                        }
                        fullUrl = baseUrl + imageUrl;
                    }

                    Glide.with(holder.imageView.getContext())
                            .load(fullUrl)
                            .placeholder(android.R.drawable.ic_menu_report_image)
                            .error(android.R.drawable.ic_menu_report_image)
                            .centerCrop()
                            .into(holder.imageView);
                } else {
                    holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } catch (Exception e) {
                // Fallback to placeholder on any error
                holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        class ImageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(ImageView imageView) {
                super(imageView);
                this.imageView = imageView;
            }
        }
    }

    // Base fragment interface
    public interface CarDetailFragmentBase {
        void updateCarDetail(CarDetailResponse carDetail);
    }
}

