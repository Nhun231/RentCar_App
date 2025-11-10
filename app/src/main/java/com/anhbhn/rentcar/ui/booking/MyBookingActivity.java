package com.anhbhn.rentcar.ui.booking;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingListResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingThumbnailResponse;
import com.anhbhn.rentcar.data.repository.booking.BookingRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingActivity extends AppCompatActivity implements MyBookingAdapter.OnBookingActionListener {
    
    private Toolbar toolbar;
    private TextView tvTotalOngoingBookings;
    private Spinner spinnerStatusFilter;
    private Spinner spinnerSort;
    private RecyclerView recyclerViewBookings;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutPagination;
    private Button btnPreviousPage;
    private Button btnNextPage;
    private TextView tvPageInfo;
    
    private MyBookingAdapter adapter;
    private BookingRepository bookingRepository;
    
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPages = 1;
    private String currentStatusFilter = "ALL";
    private String currentSort = "updatedAt,DESC";
    
    private List<BookingThumbnailResponse> bookingList = new ArrayList<>();
    
    // Status filter options
    private static final String[] STATUS_OPTIONS = {
        "ALL",
        "CANCELLED",
        "CONFIRMED",
        "PENDING_PAYMENT",
        "PENDING_DEPOSIT",
        "WAITING_CONFIRMED",
        "IN_PROGRESS",
        "COMPLETED",
        "WAITING_CONFIRMED_RETURN_CAR"
    };
    
    // Sort options
    private static final String[] SORT_OPTIONS = {
        "Newest",
        "Oldest",
        "Price High",
        "Price Low"
    };
    
    private static final String[] SORT_VALUES = {
        "updatedAt,DESC",
        "updatedAt,ASC",
        "basePrice,DESC",
        "basePrice,ASC"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_booking);
        
        initializeViews();
        setupToolbar();
        setupSpinners();
        setupRecyclerView();
        setupPagination();
        loadBookings();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTotalOngoingBookings = findViewById(R.id.tvTotalOngoingBookings);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        spinnerSort = findViewById(R.id.spinnerSort);
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutPagination = findViewById(R.id.layoutPagination);
        btnPreviousPage = findViewById(R.id.btnPreviousPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        tvPageInfo = findViewById(R.id.tvPageInfo);
        
        bookingRepository = new BookingRepository(this);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setupSpinners() {
        // Status filter spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            STATUS_OPTIONS
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setSelection(0); // ALL
        
        spinnerStatusFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = STATUS_OPTIONS[position];
                if (!selectedStatus.equals(currentStatusFilter)) {
                    currentStatusFilter = selectedStatus;
                    currentPage = 0;
                    loadBookings();
                }
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
        
        // Sort spinner
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            SORT_OPTIONS
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(0); // Newest
        
        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedSort = SORT_VALUES[position];
                if (!selectedSort.equals(currentSort)) {
                    currentSort = selectedSort;
                    currentPage = 0;
                    loadBookings();
                }
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void setupRecyclerView() {
        adapter = new MyBookingAdapter(bookingList, this);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBookings.setAdapter(adapter);
    }
    
    private void setupPagination() {
        btnPreviousPage.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadBookings();
            }
        });
        
        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadBookings();
            }
        });
    }
    
    private void loadBookings() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        recyclerViewBookings.setVisibility(View.GONE);
        
        // Determine status parameter (null if ALL)
        String statusParam = "ALL".equals(currentStatusFilter) ? null : currentStatusFilter;
        
        bookingRepository.getMyBookings(currentPage, pageSize, statusParam, currentSort)
            .enqueue(new Callback<ApiResponse<BookingListResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<BookingListResponse>> call, Response<ApiResponse<BookingListResponse>> response) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<BookingListResponse> apiResponse = response.body();
                        if (apiResponse.code == 1000 && apiResponse.data != null) {
                            BookingListResponse data = apiResponse.data;
                            
                            // Update total ongoing bookings
                            int totalOngoing = data.totalOnGoingBookings;
                            String ongoingText = "You have " + totalOngoing + " on-going bookings!";
                            if (totalOngoing > 0) {
                                ongoingText = "You have " + totalOngoing + " on-going bookings!";
                            } else {
                                ongoingText = "You have 0 on-going bookings!";
                            }
                            tvTotalOngoingBookings.setText(ongoingText);
                            
                            // Update booking list
                            if (data.data != null && data.data.content != null) {
                                bookingList = data.data.content;
                                adapter.updateBookings(bookingList);
                                
                                // Update pagination
                                totalPages = data.data.totalPages;
                                updatePaginationUI();
                                
                                // Show/hide empty state
                                if (bookingList.isEmpty()) {
                                    layoutEmptyState.setVisibility(View.VISIBLE);
                                    recyclerViewBookings.setVisibility(View.GONE);
                                    layoutPagination.setVisibility(View.GONE);
                                } else {
                                    layoutEmptyState.setVisibility(View.GONE);
                                    recyclerViewBookings.setVisibility(View.VISIBLE);
                                    if (totalPages > 1) {
                                        layoutPagination.setVisibility(View.VISIBLE);
                                    } else {
                                        layoutPagination.setVisibility(View.GONE);
                                    }
                                }
                            } else {
                                showEmptyState();
                            }
                        } else {
                            Toasty.error(MyBookingActivity.this, 
                                apiResponse.message != null ? apiResponse.message : "Failed to load bookings", 
                                Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                    } else {
                        Toasty.error(MyBookingActivity.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<BookingListResponse>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toasty.error(MyBookingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            });
    }
    
    private void updatePaginationUI() {
        tvPageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        btnPreviousPage.setEnabled(currentPage > 0);
        btnNextPage.setEnabled(currentPage < totalPages - 1);
    }
    
    private void showEmptyState() {
        layoutEmptyState.setVisibility(View.VISIBLE);
        recyclerViewBookings.setVisibility(View.GONE);
        layoutPagination.setVisibility(View.GONE);
    }
    
    @Override
    public void onViewDetailsClick(BookingThumbnailResponse booking) {
        // Navigate to booking details
        // TODO: Create BookingDetailActivity or use existing activity
        Intent intent = new Intent(this, BookingInformationActivity.class);
        // For now, just show a toast
        Toasty.info(this, "View details for booking: " + booking.bookingNumber, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onActionClick(BookingThumbnailResponse booking, String action) {
        switch (action) {
            case "PICK_UP":
                confirmPickUp(booking);
                break;
            case "CANCEL":
                cancelBooking(booking);
                break;
            case "PAY_DEPOSIT":
                payDeposit(booking);
                break;
            case "RETURN_CAR":
                returnCar(booking);
                break;
            case "COMPLETE_PAYMENT":
                completePayment(booking);
                break;
        }
    }
    
    private void confirmPickUp(BookingThumbnailResponse booking) {
        new AlertDialog.Builder(this)
            .setTitle("Pick-up this car?")
            .setMessage("Please confirm to pick up the car.")
            .setPositiveButton("Yes", (dialog, which) -> {
                bookingRepository.confirmPickUp(booking.bookingNumber)
                    .enqueue(new Callback<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, 
                                Response<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse> apiResponse = response.body();
                                if (apiResponse.code == 1000) {
                                    Toasty.success(MyBookingActivity.this, "Pick-up confirmed successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(MyBookingActivity.this, 
                                        apiResponse.message != null ? apiResponse.message : "Failed to confirm pick-up", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(MyBookingActivity.this, "Failed to confirm pick-up", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(MyBookingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void cancelBooking(BookingThumbnailResponse booking) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel this booking?")
            .setMessage("Do you really want to cancel this booking?")
            .setPositiveButton("Yes", (dialog, which) -> {
                bookingRepository.cancelBooking(booking.bookingNumber)
                    .enqueue(new Callback<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, 
                                Response<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse> apiResponse = response.body();
                                if (apiResponse.code == 1000) {
                                    Toasty.success(MyBookingActivity.this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(MyBookingActivity.this, 
                                        apiResponse.message != null ? apiResponse.message : "Failed to cancel booking", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(MyBookingActivity.this, "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(MyBookingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void payDeposit(BookingThumbnailResponse booking) {
        new AlertDialog.Builder(this)
            .setTitle("Pay deposit car?")
            .setMessage("Please confirm to pay deposit.")
            .setPositiveButton("Yes", (dialog, which) -> {
                bookingRepository.payDepositAgain(booking.bookingNumber)
                    .enqueue(new Callback<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, 
                                Response<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse> apiResponse = response.body();
                                if (apiResponse.code == 1000) {
                                    Toasty.success(MyBookingActivity.this, "Deposit paid successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(MyBookingActivity.this, 
                                        apiResponse.message != null ? apiResponse.message : "Failed to pay deposit", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(MyBookingActivity.this, "Failed to pay deposit", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(MyBookingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void returnCar(BookingThumbnailResponse booking) {
        String message = "Please confirm to return the car.";
        if (booking.totalPrice <= booking.deposit) {
            long refundAmount = booking.deposit - booking.totalPrice;
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            message = "Please confirm to return the car. The remaining amount of " + 
                     formatter.format(refundAmount) + " VND will be returned to your wallet.";
        } else {
            long excessAmount = booking.totalPrice - booking.deposit;
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            message = "Please confirm to return the car. The exceeding amount of " + 
                     formatter.format(excessAmount) + " VND will be deducted from your wallet. " +
                     "The car owner must confirm your early return request. If declined, you must keep the car until the original return time.";
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Return car?")
            .setMessage(message)
            .setPositiveButton("Yes", (dialog, which) -> {
                bookingRepository.returnCar(booking.bookingNumber)
                    .enqueue(new Callback<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, 
                                Response<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse> apiResponse = response.body();
                                if (apiResponse.code == 1000) {
                                    Toasty.success(MyBookingActivity.this, "Return car request submitted successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(MyBookingActivity.this, 
                                        apiResponse.message != null ? apiResponse.message : "Failed to return car", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(MyBookingActivity.this, "Failed to return car", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(MyBookingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void completePayment(BookingThumbnailResponse booking) {
        new AlertDialog.Builder(this)
            .setTitle("Pay total fee car?")
            .setMessage("Please confirm to pay total fee.")
            .setPositiveButton("Yes", (dialog, which) -> {
                bookingRepository.payTotalPaymentAgain(booking.bookingNumber)
                    .enqueue(new Callback<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, 
                                Response<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse> apiResponse = response.body();
                                if (apiResponse.code == 1000) {
                                    Toasty.success(MyBookingActivity.this, "Payment completed successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(MyBookingActivity.this, 
                                        apiResponse.message != null ? apiResponse.message : "Failed to complete payment", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(MyBookingActivity.this, "Failed to complete payment", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(MyBookingActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
}

