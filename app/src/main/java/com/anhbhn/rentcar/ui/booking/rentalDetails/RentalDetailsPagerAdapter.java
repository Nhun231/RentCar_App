package com.anhbhn.rentcar.ui.booking.rentalDetails;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RentalDetailsPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 2;

    public RentalDetailsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Fragment 1: Thông tin tài xế, địa chỉ, bằng lái
                return new RentalInformationFragment(); // ⬅️ Cần tạo class này
            case 1:
                // Fragment 2: Thông số xe, tài liệu, giá
                return new CarInformationFragment();   // ⬅️ Cần tạo class này
            default:
                // Trả về Fragment mặc định nếu có lỗi
                return new RentalInformationFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}