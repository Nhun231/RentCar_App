package com.anhbhn.rentcar.ui.booking;

import android.os.Bundle;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.anhbhn.rentcar.R;
import android.content.res.ColorStateList;
import com.anhbhn.rentcar.databinding.DialogConfirmBookingActionBinding;

public class ConfirmationDialogFragment extends DialogFragment {
    public interface ConfirmationListener {
        void onConfirmAction(String bookingNumber, String actionType);
    }
    // --- ARGUMENTS KEYS ---
    private static final String ARG_BOOKING_NUMBER = "bookingNumber";
    private static final String ARG_ACTION_TYPE = "actionType";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_CONFIRM_TEXT = "confirmText";
    private static final String ARG_CONFIRM_COLOR = "confirmColor";
    private static final String ARG_SHOW_ICON = "showIcon";

    private ConfirmationListener listener;
    private DialogConfirmBookingActionBinding binding;

    // Factory method để tạo instance
    public static ConfirmationDialogFragment newInstance(String bookingNumber, String actionType,
                                                         String title, String message, String confirmText,
                                                         int confirmColorResId, boolean showIcon) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOOKING_NUMBER, bookingNumber);
        args.putString(ARG_ACTION_TYPE, actionType);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_CONFIRM_TEXT, confirmText);
        args.putInt(ARG_CONFIRM_COLOR, confirmColorResId);
        args.putBoolean(ARG_SHOW_ICON, showIcon);
        fragment.setArguments(args);
        return fragment;
    }

    public void setConfirmationListener(ConfirmationListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ✅ Khởi tạo Binding
        binding = DialogConfirmBookingActionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Thiết lập nền trong suốt
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // ✅ BỔ SUNG: Cho phép Dialog hiển thị toàn bộ chiều rộng (MATCH_PARENT)
        // Phải gọi setOnShowListener để đảm bảo Window được tạo trước
        dialog.setOnShowListener(dialogInterface -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                // Thêm margin hoặc padding nếu cần tránh sát mép màn hình
            }
        });

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null) return;

        String bookingNumber = getArguments().getString(ARG_BOOKING_NUMBER);
        String actionType = getArguments().getString(ARG_ACTION_TYPE);

        // --- Cấu hình UI ---
        int confirmColorResId = getArguments().getInt(ARG_CONFIRM_COLOR);

        int confirmColorInt = ContextCompat.getColor(requireContext(), confirmColorResId);

        // Đặt nội dung và màu sắc
        binding.ivDialogIcon.setVisibility(getArguments().getBoolean(ARG_SHOW_ICON) ? View.VISIBLE : View.GONE);
        binding.tvDialogTitle.setText(getArguments().getString(ARG_TITLE));
        binding.tvDialogMessage.setText(getArguments().getString(ARG_MESSAGE));

        binding.btnConfirmAction.setText(getArguments().getString(ARG_CONFIRM_TEXT));
        // Sử dụng ContextCompat.getColorStateList để gán màu tint
        binding.btnConfirmAction.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), confirmColorResId));

        binding.btnCancelAction.setText(R.string.btn_cancel);

        // --- Listener ---
        binding.btnConfirmAction.setOnClickListener(v -> {
            if (listener != null) {
                // Gọi callback về Activity/Fragment
                listener.onConfirmAction(bookingNumber, actionType);
            }
            dismiss();
        });

        binding.btnCancelAction.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
