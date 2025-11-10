package com.anhbhn.rentcar.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.booking.BookingThumbnailResponse;
import com.anhbhn.rentcar.databinding.ActivityMyRentalsBinding;
import com.anhbhn.rentcar.ui.booking.myRentals.BookingThumbnailAdapter;
import com.anhbhn.rentcar.ui.booking.myRentals.MyRentalsViewModel;
import com.anhbhn.rentcar.ui.booking.rentalDetails.RentalDetailsActivity;
import com.anhbhn.rentcar.ui.booking.ConfirmationDialogFragment;
import com.anhbhn.rentcar.ui.booking.ConfirmationDialogFragment.ConfirmationListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class MyRentalsFragment extends Fragment implements BookingThumbnailAdapter.OnItemClickListener, ConfirmationListener {

    private ActivityMyRentalsBinding binding;
    private MyRentalsViewModel viewModel;
    private BookingThumbnailAdapter adapter;

    private static final String DEFAULT_SORT = "updatedAt,DESC";
    private static final String STATUS_WAITING_CONFIRMED = "WAITING_CONFIRMED";
    private static final String STATUS_WAITING_RETURN = "WAITING_CONFIRMED_RETURN_CAR";

    private static final String[] STATUS_OPTIONS = {
            "ALL", "CANCELLED", "CONFIRMED", "PENDING_PAYMENT", "PENDING_DEPOSIT",
            "WAITING_CONFIRMED", "IN_PROGRESS", "COMPLETED", "WAITING_CONFIRMED_RETURN_CAR"
    };
    private static final String[] SORT_OPTIONS = {
            "Newest", "Oldest", "Price High", "Price Low"
    };
    private static final String[] SORT_VALUES = {
            "updatedAt,DESC", "updatedAt,ASC", "basePrice,DESC", "basePrice,ASC"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityMyRentalsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MyRentalsViewModel.class);
        viewModel.initializeRepository(requireContext());

        setupToolbar();
        setupRecyclerView();
        setupFiltersAndPagination();
        observeViewModel();

        if (viewModel.getBookingList().getValue() != null && viewModel.getBookingList().getValue().isEmpty()) {
            viewModel.loadBookings(requireContext(), true);
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = binding.toolbar;
        if (toolbar != null) {

            if (getActivity() instanceof AppCompatActivity) {
                ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
            }

            if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.title_my_rentals);
                // Loại bỏ nút Back/Up (vì đây là màn hình chính của Bottom Nav)
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }

            toolbar.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new BookingThumbnailAdapter(requireContext(), new ArrayList<>(), this);
        binding.rvBookingList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookingList.setAdapter(adapter);
    }

    private void setupFiltersAndPagination() {
        setupSpinner(binding.spinnerStatusFilter, R.array.status_filter_options, true);
        setupSpinner(binding.spinnerSortBy, R.array.sort_by_options, false);

        binding.btnPageNext.setOnClickListener(v -> viewModel.goToNextPage(requireContext()));
        binding.btnPagePrev.setOnClickListener(v -> viewModel.goToPreviousPage(requireContext()));
    }

    private void setupSpinner(Spinner spinner, int arrayResId, boolean isStatusFilter) {
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                requireContext(), arrayResId, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (isStatusFilter) {
                    viewModel.setStatusFilter(requireContext(), selectedItem);
                } else {
                    String sortValue = mapSortOptionToApi(selectedItem);
                    viewModel.setSort(requireContext(), sortValue);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private String mapSortOptionToApi(String selectedOption) {
        if (selectedOption.contains("Newest to Oldest")) return "updatedAt,DESC";
        if (selectedOption.contains("Oldest to Newest")) return "updatedAt,ASC";
        if (selectedOption.contains("Price: High to Low")) return "basePrice,DESC";
        if (selectedOption.contains("Price: Low to High")) return "basePrice,ASC";
        return DEFAULT_SORT;
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            updatePaginationButtonState();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toasty.error(requireContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });

        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toasty.success(requireContext(), successMessage, Toast.LENGTH_LONG).show();
                viewModel.clearSuccessMessage();
            }
        });

        viewModel.getBookingList().observe(getViewLifecycleOwner(), bookings -> {
            adapter.updateList(bookings);
            boolean isEmpty = (bookings == null || bookings.isEmpty());
            binding.rvBookingList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            updatePaginationButtonState();
        });

        viewModel.getWaitingCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvWaitingCountSummary.setText(getString(R.string.msg_waiting_count_format, count));
        });

        viewModel.getPageIndicator().observe(getViewLifecycleOwner(), indicator -> {
            binding.tvPageIndicator.setText(indicator);
            updatePaginationButtonState();
        });
    }

    private void updatePaginationButtonState() {
        boolean isLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
        boolean isFirstPage = Boolean.TRUE.equals(viewModel.getCurrentPage().getValue() == 0);
        boolean isLastPage = viewModel.isLastPage();

        binding.btnPagePrev.setEnabled(!isLoading && !isFirstPage);
        binding.btnPageNext.setEnabled(!isLoading && !isLastPage);
    }

    // --- UTILITY: HIỂN THỊ DIALOG XÁC NHẬN ---
    private void showBookingActionDialog(String bookingNumber, String actionType, String currentItemStatus) {
        String title, message, confirmText;
        int confirmColorResId;
        boolean showIcon = false;

        if (actionType.equals("REJECT")) {
            title = getString(R.string.dialog_title_are_you_sure);
            message = getString(R.string.dialog_msg_reject_booking_q);
            confirmText = getString(R.string.btn_yes_reject);
            confirmColorResId = R.color.red_action;
            showIcon = true;
        } else {
            title = getString(R.string.dialog_title_confirm_action);
            message = getString(R.string.dialog_msg_confirm_booking_q);
            confirmText = getString(R.string.btn_confirm);
            confirmColorResId = R.color.primary_green;
        }

        ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(
                bookingNumber, actionType, title, message, confirmText, confirmColorResId, showIcon
        );

        Bundle args = dialogFragment.getArguments();
        if (args != null) {
            args.putString("CURRENT_ITEM_STATUS", currentItemStatus);
        }

        dialogFragment.setConfirmationListener(this);
        dialogFragment.show(getParentFragmentManager(), "BookingActionDialog");
    }

    // --- ACTION IMPLEMENTATIONS ---


    @Override
    public void onViewDetailsClick(String bookingNumber) {
        Intent intent = new Intent(requireContext(), RentalDetailsActivity.class);
        intent.putExtra("BOOKING_NUMBER", bookingNumber);
        startActivity(intent);
    }

    @Override
    public void onApproveClick(String bookingNumber, String currentStatus) {
        showBookingActionDialog(bookingNumber, "APPROVE", currentStatus);
    }

    @Override
    public void onRejectClick(String bookingNumber, String currentStatus) {
        showBookingActionDialog(bookingNumber, "REJECT", currentStatus);
    }

    @Override
    public void onConfirmAction(String bookingNumber, String actionType) {
        ConfirmationDialogFragment dialogFragment = (ConfirmationDialogFragment) getParentFragmentManager().findFragmentByTag("BookingActionDialog");

        String currentStatus = STATUS_WAITING_CONFIRMED;

        if (dialogFragment != null && dialogFragment.getArguments() != null) {
            currentStatus = dialogFragment.getArguments().getString("CURRENT_ITEM_STATUS", "WAITING_CONFIRMED");
        }

        if (actionType.equals("APPROVE")) {
            viewModel.approveBooking(bookingNumber, currentStatus, requireContext());
        } else if (actionType.equals("REJECT")) {
            viewModel.rejectBooking(bookingNumber, currentStatus, requireContext());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}