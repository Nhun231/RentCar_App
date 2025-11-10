package com.anhbhn.rentcar.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.anhbhn.rentcar.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingFinishActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvMessage;
    private TextView tvBookingNumber;
    private TextView tvPickUpTime;
    private TextView tvDropOffTime;
    private Button btnMyBookings;
    private Button btnMyWallet;
    private LinearLayout layoutButtons;
    
    private String bookingNumber;
    private String status;
    private String paymentType;
    private String pickUpTime;
    private String dropOffTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_finish);
        
        loadIntentData();
        initializeViews();
        displayBookingStatus();
        setupButtons();
    }
    
    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            bookingNumber = intent.getStringExtra("bookingNumber");
            status = intent.getStringExtra("status");
            paymentType = intent.getStringExtra("paymentType");
            pickUpTime = intent.getStringExtra("pickUpTime");
            dropOffTime = intent.getStringExtra("dropOffTime");
        }
    }
    
    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvBookingNumber = findViewById(R.id.tvBookingNumber);
        tvPickUpTime = findViewById(R.id.tvPickUpTime);
        tvDropOffTime = findViewById(R.id.tvDropOffTime);
        btnMyBookings = findViewById(R.id.btnMyBookings);
        btnMyWallet = findViewById(R.id.btnMyWallet);
        layoutButtons = findViewById(R.id.layoutButtons);
    }
    
    private void displayBookingStatus() {
        if (status != null) {
            switch (status) {
                case "PENDING_DEPOSIT":
                    if ("WALLET".equals(paymentType)) {
                        // Wallet payment but insufficient balance
                        tvTitle.setText("Booking Created");
                        tvMessage.setText("Your booking has been recorded. Please top up your wallet and complete the payment within 1 hour.");
                        tvMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
                        btnMyWallet.setVisibility(View.VISIBLE);
                        btnMyBookings.setVisibility(View.GONE);
                    } else {
                        // Cash or Bank Transfer
                        tvTitle.setText("Booking Created");
                        tvMessage.setText("Our operator will confirm your booking soon.");
                        tvMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
                        btnMyWallet.setVisibility(View.GONE);
                        btnMyBookings.setVisibility(View.VISIBLE);
                    }
                    break;
                case "WAITING_CONFIRMED":
                    tvTitle.setText("✅ Booking Confirmed!");
                    tvMessage.setText("You have successfully created your booking. Our operator will contact you with further guidance about pickup.");
                    tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                    btnMyWallet.setVisibility(View.GONE);
                    btnMyBookings.setVisibility(View.VISIBLE);
                    break;
                default:
                    tvTitle.setText("Booking Created");
                    tvMessage.setText("Your booking has been created successfully.");
                    tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                    btnMyWallet.setVisibility(View.GONE);
                    btnMyBookings.setVisibility(View.VISIBLE);
                    break;
            }
        }
        
        // Display booking number
        if (bookingNumber != null) {
            tvBookingNumber.setText("Booking Number: " + bookingNumber);
        }
        
        // Display pick-up and drop-off times
        if (pickUpTime != null) {
            tvPickUpTime.setText("From: " + formatDateTime(pickUpTime));
        }
        if (dropOffTime != null) {
            tvDropOffTime.setText("To: " + formatDateTime(dropOffTime));
        }
    }
    
    private String formatDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = apiFormat.parse(dateTimeStr);
            if (date != null) {
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            // Ignore parsing errors
        }
        return dateTimeStr;
    }
    
    private void setupButtons() {
        btnMyBookings.setOnClickListener(v -> {
            // Navigate to CustomerMainActivity with My Booking tab selected
            Intent intent = new Intent(BookingFinishActivity.this, com.anhbhn.rentcar.ui.customer.CustomerMainActivity.class);
            intent.putExtra("selectedTab", "my_booking");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        btnMyWallet.setOnClickListener(v -> {
            // TODO: Navigate to wallet screen
            // For now, just finish this activity
            finish();
        });
    }
}

