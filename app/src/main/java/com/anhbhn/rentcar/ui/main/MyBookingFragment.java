package com.anhbhn.rentcar.ui.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingListResponse;
import com.anhbhn.rentcar.data.dto.response.booking.BookingThumbnailResponse;
import com.anhbhn.rentcar.data.repository.booking.BookingRepository;
import com.anhbhn.rentcar.ui.booking.MyBookingAdapter;
import com.anhbhn.rentcar.ui.booking.BookingInformationActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingFragment extends Fragment implements MyBookingAdapter.OnBookingActionListener {
    
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
    private View layoutFilters;
    
    private MyBookingAdapter adapter;
    private BookingRepository bookingRepository;
    
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPages = 1;
    private String currentStatusFilter = "ALL";
    private String currentSort = "updatedAt,DESC";
    
    private List<BookingThumbnailResponse> bookingList = new ArrayList<>();
    
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_booking, container, false);
        
        // Hide toolbar since we're in a fragment with bottom navigation
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupSpinners();
        setupRecyclerView();
        setupPagination();
        loadBookings();
    }
    
    private void initializeViews(View view) {
        // Hide toolbar since we're in a fragment with bottom navigation
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }
        
        // Show title when toolbar is hidden
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setVisibility(View.VISIBLE);
        }
        
        tvTotalOngoingBookings = view.findViewById(R.id.tvTotalOngoingBookings);
        spinnerStatusFilter = view.findViewById(R.id.spinnerStatusFilter);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        recyclerViewBookings = view.findViewById(R.id.recyclerViewBookings);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        layoutPagination = view.findViewById(R.id.layoutPagination);
        btnPreviousPage = view.findViewById(R.id.btnPreviousPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);
        
        bookingRepository = new BookingRepository(requireContext());
    }
    
    private void setupSpinners() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            STATUS_OPTIONS
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setSelection(0);
        
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
            }
        });
        
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SORT_OPTIONS
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(0);
        
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
            }
        });
    }
    
    private void setupRecyclerView() {
        adapter = new MyBookingAdapter(bookingList, this);
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                            
                            int totalOngoing = data.totalOnGoingBookings;
                            String ongoingText = "You have " + totalOngoing + " on-going bookings!";
                            if (totalOngoing == 0) {
                                ongoingText = "You have 0 on-going bookings!";
                            }
                            tvTotalOngoingBookings.setText(ongoingText);
                            
                            if (data.data != null && data.data.content != null) {
                                bookingList = data.data.content;
                                adapter.updateBookings(bookingList);
                                
                                totalPages = data.data.totalPages;
                                updatePaginationUI();
                                
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
                            Toasty.error(requireContext(), 
                                apiResponse.message != null ? apiResponse.message : "Failed to load bookings", 
                                Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                    } else {
                        Toasty.error(requireContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<BookingListResponse>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toasty.error(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        Toasty.info(requireContext(), "View details for booking: " + booking.bookingNumber, Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(requireContext())
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
                                    Toasty.success(requireContext(), "Pick-up confirmed successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(requireContext(), 
                                        apiResponse.message != null ? apiResponse.message : "Failed to confirm pick-up", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(requireContext(), "Failed to confirm pick-up", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void cancelBooking(BookingThumbnailResponse booking) {
        new AlertDialog.Builder(requireContext())
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
                                    Toasty.success(requireContext(), "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(requireContext(), 
                                        apiResponse.message != null ? apiResponse.message : "Failed to cancel booking", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(requireContext(), "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void payDeposit(BookingThumbnailResponse booking) {
        new AlertDialog.Builder(requireContext())
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
                                    Toasty.success(requireContext(), "Deposit paid successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(requireContext(), 
                                        apiResponse.message != null ? apiResponse.message : "Failed to pay deposit", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(requireContext(), "Failed to pay deposit", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        
        new AlertDialog.Builder(requireContext())
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
                                    Toasty.success(requireContext(), "Return car request submitted successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(requireContext(), 
                                        apiResponse.message != null ? apiResponse.message : "Failed to return car", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(requireContext(), "Failed to return car", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void completePayment(BookingThumbnailResponse booking) {
        new AlertDialog.Builder(requireContext())
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
                                    Toasty.success(requireContext(), "Payment completed successfully", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                } else {
                                    Toasty.error(requireContext(), 
                                        apiResponse.message != null ? apiResponse.message : "Failed to complete payment", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toasty.error(requireContext(), "Failed to complete payment", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<ApiResponse<com.anhbhn.rentcar.data.dto.response.booking.BookingResponse>> call, Throwable t) {
                            Toasty.error(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }
}

