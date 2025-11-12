package com.anhbhn.rentcar.ui.wallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anhbhn.rentcar.R;
// Import các DTO/Repository cần thiết (Bạn cần đảm bảo các class này tồn tại)
import com.anhbhn.rentcar.data.dto.response.ApiResponse;
import com.anhbhn.rentcar.data.dto.response.transaction.ListTransactionResponse;
import com.anhbhn.rentcar.data.repository.wallet.WalletRepository;
// import com.anhbhn.rentcar.data.dto.response.WalletBalanceResponse; // Ví dụ DTO chứa số dư
// import com.anhbhn.rentcar.data.repository.user.WalletRepository; // Ví dụ Repository

import java.text.NumberFormat;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyWalletActivity extends AppCompatActivity {

    private TextView tvCurrentBalance;
    private TextView linkViewHistory;
    private TextView tvWebHint; // Biến cho hint mới

    private WalletRepository walletRepository;

    private String walletHistoryUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        initializeViews();
        walletRepository = new WalletRepository(this);

        // Lấy URL từ resources (sẽ là http://10.0.2.2:3000/my-wallet/transactions)
        walletHistoryUrl = getString(R.string.wallet_history_url);

        loadWalletBalance();
        setupListeners();
    }

    private void initializeViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        linkViewHistory = findViewById(R.id.linkViewHistory);
        tvWebHint = findViewById(R.id.tvWebHint); // Ánh xạ TextView mới

        // Không cần ánh xạ btnTopUp, btnWithdraw nữa
    }

    private void setupListeners() {
        // Chuyển hướng đến trang web lịch sử giao dịch (bao gồm cả TOP-UP/WITHDRAW)
        linkViewHistory.setOnClickListener(v -> openWebPage(walletHistoryUrl));
    }

    private void loadWalletBalance() {

        // Bắt đầu với trạng thái loading
        tvCurrentBalance.setText("Loading...");

        // Gọi phương thức từ WalletRepository
        walletRepository.getAllTransactionList().enqueue(new Callback<ApiResponse<ListTransactionResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ListTransactionResponse>> call, Response<ApiResponse<ListTransactionResponse>> response) {

                // --- 1. KIỂM TRA PHẢN HỒI HTTP (SUCCESS/FAILURE) ---
                if (response.isSuccessful()) {

                    // --- 2. KIỂM TRA NULL BODY (NullPointerException Guard) ---
                    ApiResponse<ListTransactionResponse> apiResponse = response.body();

                    if (apiResponse != null) {

                        // --- 3. KIỂM TRA LỖI LOGIC TỪ SERVER (code) ---
                        if (apiResponse.code == 1000) {

                            // --- 4. KIỂM TRA DỮ LIỆU DATA NULL ---
                            if (apiResponse.data != null) {
                                ListTransactionResponse transactionData = apiResponse.data;
                                long balance = transactionData.getBalance();

                                displayBalance(balance);
                            } else {
                                // code 1000 nhưng data null
                                displayBalance(0L);
                                showToast("Error: Empty data returned.", false);
                            }
                        } else {
                            // Lỗi logic từ Server (ví dụ: code khác 1000)
                            displayBalance(0L);
                            showToast("Failed to load balance: " + apiResponse.message, false);
                        }
                    } else {
                        // Xử lý trường hợp HTTP 204 (No Content)
                        displayBalance(0L);
                        showToast("Server returned no content.", false);
                    }
                } else {
                    // --- XỬ LÝ LỖI HTTP (4xx, 5xx) ---
                    displayBalance(0L);
                    showToast("Server error (" + response.code() + "). Failed to load wallet.", false);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ListTransactionResponse>> call, Throwable t) {
                // Xử lý lỗi mạng
                displayBalance(0L);
                showToast("Network error: " + t.getMessage(), false);
            }
        });
    }

    private void displayBalance(long balance) {
        // Định dạng tiền tệ VND (Locale Việt Nam)
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String formattedBalance = "VND " + formatter.format(balance);
        tvCurrentBalance.setText(formattedBalance);
    }

    private void openWebPage(String url) {
        if (url == null || url.isEmpty()) {
            showToast("Error: Wallet URL is missing in resources.", false);
            return;
        }

        // Gửi Intent để mở trình duyệt hoặc WebView
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
        } catch (Exception e) {
            showToast("Cannot open web page. Please ensure a browser is installed.", false);
            android.util.Log.e("WalletActivity", "Error opening URL: " + e.getMessage());
        }
    }

    private void showToast(String message, boolean success) {
        if (success) {
            Toasty.success(this, message, Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.error(this, message, Toast.LENGTH_SHORT, true).show();
        }
    }
}